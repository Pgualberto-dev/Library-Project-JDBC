package repository.jdbc;

import database.ConnFactory;
import model.Book;
import model.Loan;
import model.LoanStatus;
import model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.Optional;

public class LoanJdbcRepoTest {

    public static void main(String[] args) throws Exception {
        try (Connection conn = ConnFactory.getConnection()) {
            UserJdbcRepo userRepo = new UserJdbcRepo(conn);
            BookJdbcRepo bookRepo = new BookJdbcRepo(conn);
            LoanJdbcRepo loanRepo = new LoanJdbcRepo(conn);

            // 0. cenário: usuário e livro reais (por causa das chaves estrangeiras)
            long stamp = System.currentTimeMillis();
            User user = userRepo.addUser(new User("Leitor Teste", "loan" + stamp + "@mail.com"));
            Book book = bookRepo.addBook(new Book("Livro Loan " + stamp, 3));
            Integer loanId = null;

            try {
                LocalDate loanDate = LocalDate.now();
                LocalDate dueDate = loanDate.plusDays(14);

                // 1. addLoan
                Loan saved = loanRepo.addLoan(new Loan(user.getId(), book.getId(), loanDate, dueDate));
                loanId = saved.getId();
                check("addLoan devolve id gerado", saved.getId() != null && saved.getId() > 0);
                check("empréstimo novo nasce ACTIVE", saved.getStatus() == LoanStatus.ACTIVE);

                // 2. findById: prova datas e enum nos dois sentidos (gravar e ler)
                Optional<Loan> found = loanRepo.findById(saved.getId());
                check("findById encontra", found.isPresent());
                check("datas voltam iguais",
                        found.isPresent()
                                && found.get().getLoanDate().equals(loanDate)
                                && found.get().getDueDate().equals(dueDate));
                check("status volta ACTIVE (dbValue/fromDb)",
                        found.isPresent() && found.get().getStatus() == LoanStatus.ACTIVE);
                check("ids de usuário e livro voltam iguais",
                        found.isPresent()
                                && found.get().getUserId().equals(user.getId())
                                && found.get().getBookId().equals(book.getId()));

                // 3. listas
                check("findAllByUserId contém",
                        loanRepo.findAllByUserId(user.getId()).stream()
                                .anyMatch(l -> l.getId().equals(saved.getId())));
                check("findAllByBookId contém",
                        loanRepo.findAllByBookId(book.getId()).stream()
                                .anyMatch(l -> l.getId().equals(saved.getId())));
                check("findAll contém",
                        loanRepo.findAll().stream()
                                .anyMatch(l -> l.getId().equals(saved.getId())));

                // 4. contagem de indisponíveis
                check("count = 1 com empréstimo ACTIVE", loanRepo.countUnreturnedByBookId(book.getId()) == 1);

                // 5. update: vencimento + OVERDUE (continua contando)
                saved.setDueDate(dueDate.plusDays(7));
                saved.setStatus(LoanStatus.OVERDUE);
                check("updateLoan (OVERDUE) retorna true", loanRepo.updateLoan(saved));
                Optional<Loan> afterOverdue = loanRepo.findById(saved.getId());
                check("banco refletiu OVERDUE e novo vencimento",
                        afterOverdue.isPresent()
                                && afterOverdue.get().getStatus() == LoanStatus.OVERDUE
                                && afterOverdue.get().getDueDate().equals(dueDate.plusDays(7)));
                check("count continua 1 com OVERDUE", loanRepo.countUnreturnedByBookId(book.getId()) == 1);

                // 6. update: RETURNED (deixa de contar)
                saved.setStatus(LoanStatus.RETURNED);
                check("updateLoan (RETURNED) retorna true", loanRepo.updateLoan(saved));
                Optional<Loan> afterReturned = loanRepo.findById(saved.getId());
                check("banco refletiu RETURNED",
                        afterReturned.isPresent() && afterReturned.get().getStatus() == LoanStatus.RETURNED);
                check("count cai para 0 com RETURNED", loanRepo.countUnreturnedByBookId(book.getId()) == 0);

                // 7. caminhos tristes
                check("findById inexistente vem vazio", loanRepo.findById(-1).isEmpty());
                check("updateLoan inexistente retorna false",
                        !loanRepo.updateLoan(new Loan(-1, user.getId(), book.getId(), loanDate, dueDate, LoanStatus.ACTIVE)));
                check("count de livro inexistente é 0", loanRepo.countUnreturnedByBookId(-1) == 0);

                try {
                    loanRepo.addLoan(new Loan(-1, book.getId(), loanDate, dueDate));
                    check("usuário inexistente deve lançar exceção", false);
                } catch (RuntimeException e) {
                    check("usuário inexistente lançou exceção (FK)", true);
                    System.out.println("   causa: " + e.getCause().getClass().getName());
                }

            } finally {
                // limpeza: o Loan não tem delete no repositório (histórico), então só o teste apaga por SQL direto
                if (loanId != null) {
                    deleteLoan(conn, loanId);
                }
                userRepo.deleteById(user.getId());
                bookRepo.deleteById(book.getId());
            }
        }
    }

    private static void deleteLoan(Connection conn, int loanId) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM loan WHERE loan_id = ?")) {
            stmt.setInt(1, loanId);
            stmt.executeUpdate();
        }
    }

    private static void check(String description, boolean condition) {
        System.out.println((condition ? "✅ " : "❌ ") + description);
    }
}
package services;

import database.ConnFactory;
import exception.BookNotFoundException;
import exception.LoanNotFoundException;
import exception.NoCopiesAvailableException;
import exception.UserNotFoundException;
import model.Book;
import model.Loan;
import model.LoanStatus;
import model.User;
import repository.jdbc.BookJdbcRepo;
import repository.jdbc.LoanJdbcRepo;
import repository.jdbc.UserJdbcRepo;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.UUID;

public class LoanServiceTest {

    public static void main(String[] args) throws Exception {
        try (Connection conn = ConnFactory.getConnection()) {
            conn.setAutoCommit(false);

            try {
                UserJdbcRepo userRepo = new UserJdbcRepo(conn);
                BookJdbcRepo bookRepo = new BookJdbcRepo(conn);
                LoanJdbcRepo loanRepo = new LoanJdbcRepo(conn);

                UserService userService = new UserService(userRepo, loanRepo);
                BookService bookService = new BookService(bookRepo, loanRepo);
                LoanService loanService = new LoanService(
                        loanRepo,
                        userService,
                        bookService
                );

                User user = userService.registerUser(
                        "Loan Test User",
                        "loan." + UUID.randomUUID() + "@example.com"
                );

                Book book = bookService.registerBook(
                        "Loan Test Book " + UUID.randomUUID(),
                        1
                );

                // 1. empréstimo válido
                Loan loan = loanService.lendBook(user.getId(), book.getId());

                check("lendBook devolve id gerado",
                        loan.getId() != null && loan.getId() > 0);

                check("lendBook salva o userId correto",
                        loan.getUserId().equals(user.getId()));

                check("lendBook salva o bookId correto",
                        loan.getBookId().equals(book.getId()));

                check("lendBook inicia com status ACTIVE",
                        loan.getStatus() == LoanStatus.ACTIVE);

                check("dueDate fica 14 dias depois de loanDate",
                        loan.getDueDate().equals(loan.getLoanDate().plusDays(14)));

                // 2. buscar empréstimo
                Loan foundLoan = loanService.findLoanById(loan.getId());

                check("findLoanById encontra o empréstimo",
                        foundLoan.getId().equals(loan.getId()));

                check("findLoanById inexistente lança LoanNotFoundException",
                        throwsException(LoanNotFoundException.class,
                                () -> loanService.findLoanById(-1)));

                // 3. listagens
                check("findAllLoans contém o empréstimo criado",
                        loanService.findAllLoans().stream()
                                .anyMatch(item -> item.getId().equals(loan.getId())));

                check("findAllLoansByUserId devolve empréstimo do usuário",
                        loanService.findAllLoansByUserId(user.getId()).stream()
                                .anyMatch(item -> item.getId().equals(loan.getId())));

                check("findAllLoansByBookId devolve empréstimo do livro",
                        loanService.findAllLoansByBookId(book.getId()).stream()
                                .anyMatch(item -> item.getId().equals(loan.getId())));

                check("findAllLoansByUserId com usuário inexistente lança UserNotFoundException",
                        throwsException(UserNotFoundException.class,
                                () -> loanService.findAllLoansByUserId(-1)));

                check("findAllLoansByBookId com livro inexistente lança BookNotFoundException",
                        throwsException(BookNotFoundException.class,
                                () -> loanService.findAllLoansByBookId(-1)));

                // 4. não pode emprestar além das cópias disponíveis
                check("lendBook bloqueia quando todas as cópias estão emprestadas",
                        throwsException(NoCopiesAvailableException.class,
                                () -> loanService.lendBook(user.getId(), book.getId())));

                // 5. devolução libera a cópia
                loanService.returnLoan(loan.getId());

                check("returnLoan muda o status para RETURNED",
                        loanService.findLoanById(loan.getId()).getStatus()
                                == LoanStatus.RETURNED);

                check("lendBook permite novo empréstimo após devolução",
                        !throwsException(Exception.class,
                                () -> loanService.lendBook(user.getId(), book.getId())));

                check("returnLoan inexistente lança LoanNotFoundException",
                        throwsException(LoanNotFoundException.class,
                                () -> loanService.returnLoan(-1)));

                // 6. usuário e livro inexistentes
                check("lendBook com usuário inexistente lança UserNotFoundException",
                        throwsException(UserNotFoundException.class,
                                () -> loanService.lendBook(-1, book.getId())));

                check("lendBook com livro inexistente lança BookNotFoundException",
                        throwsException(BookNotFoundException.class,
                                () -> loanService.lendBook(user.getId(), -1)));

                // 7. atraso
                User overdueUser = userService.registerUser(
                        "Overdue User",
                        "overdue." + UUID.randomUUID() + "@example.com"
                );

                Book overdueBook = bookService.registerBook(
                        "Overdue Book " + UUID.randomUUID(),
                        1
                );

                LocalDate today = LocalDate.now();

                Loan overdueLoan = loanRepo.addLoan(new Loan(
                        overdueUser.getId(),
                        overdueBook.getId(),
                        today.minusDays(15),
                        today.minusDays(1)
                ));

                loanService.updateOverdueLoans();

                check("updateOverdueLoans muda empréstimo vencido para OVERDUE",
                        loanService.findLoanById(overdueLoan.getId()).getStatus()
                                == LoanStatus.OVERDUE);

            } finally {
                conn.rollback();
            }
        }
    }

    private static boolean throwsException(
            Class<? extends Exception> type,
            Runnable action
    ) {
        try {
            action.run();
            return false;
        } catch (Exception e) {
            return type.isInstance(e);
        }
    }

    private static void check(String description, boolean condition) {
        System.out.println((condition ? "✅ " : "❌ ") + description);
    }
}

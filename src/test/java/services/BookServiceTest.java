package services;

import database.ConnFactory;
import exception.BookHasLoansException;
import exception.BookNotFoundException;
import exception.InvalidBookDataException;
import model.Book;
import model.Loan;
import model.LoanStatus;
import model.User;
import repository.jdbc.BookJdbcRepo;
import repository.jdbc.LoanJdbcRepo;
import repository.jdbc.UserJdbcRepo;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public class BookServiceTest {

    public static void main(String[] args) throws Exception {
        try (Connection conn = ConnFactory.getConnection()) {
            conn.setAutoCommit(false);

            try {
                BookJdbcRepo bookRepo = new BookJdbcRepo(conn);
                LoanJdbcRepo loanRepo = new LoanJdbcRepo(conn);
                UserJdbcRepo userRepo = new UserJdbcRepo(conn);

                BookService bookService = new BookService(bookRepo, loanRepo);
                UserService userService = new UserService(userRepo, loanRepo);

                // 1. registerBook
                String rawTitle = "  Livro Teste " + UUID.randomUUID() + "  ";
                Book saved = bookService.registerBook(rawTitle, 5);

                check("registerBook devolve id gerado",
                        saved.getId() != null && saved.getId() > 0);

                Optional<Book> fromDb = bookRepo.findById(saved.getId());

                check("título salvo sem espaços nas pontas",
                        fromDb.isPresent()
                                && fromDb.get().getTitle().equals(rawTitle.strip()));

                check("total de cópias salvo",
                        fromDb.isPresent()
                                && fromDb.get().getTotalCopies().equals(5));

                Book zero = bookService.registerBook(
                        "Livro Zero " + UUID.randomUUID(),
                        0
                );

                Optional<Book> zeroFromDb = bookRepo.findById(zero.getId());

                check("registerBook aceita 0 cópias",
                        zeroFromDb.isPresent()
                                && zeroFromDb.get().getTotalCopies().equals(0));

                check("título null lança InvalidBookDataException",
                        throwsException(InvalidBookDataException.class,
                                () -> bookService.registerBook(null, 5)));

                check("título vazio lança InvalidBookDataException",
                        throwsException(InvalidBookDataException.class,
                                () -> bookService.registerBook("", 5)));

                check("título só com espaços lança InvalidBookDataException",
                        throwsException(InvalidBookDataException.class,
                                () -> bookService.registerBook("   ", 5)));

                check("total de cópias null lança InvalidBookDataException",
                        throwsException(InvalidBookDataException.class,
                                () -> bookService.registerBook("Livro", null)));

                check("total de cópias negativo lança InvalidBookDataException",
                        throwsException(InvalidBookDataException.class,
                                () -> bookService.registerBook("Livro", -1)));

                // 2. findBookById
                check("findBookById encontra o livro",
                        bookService.findBookById(saved.getId())
                                .getId()
                                .equals(saved.getId()));

                check("findBookById inexistente lança BookNotFoundException",
                        throwsException(BookNotFoundException.class,
                                () -> bookService.findBookById(-1)));

                // 3. findAllBooks
                check("findAllBooks contém o livro cadastrado",
                        bookService.findAllBooks().stream()
                                .anyMatch(book -> book.getId().equals(saved.getId())));

                // 4. updateBook
                Book toUpdate = bookService.registerBook(
                        "Livro Antigo " + UUID.randomUUID(),
                        3
                );

                Integer updateId = toUpdate.getId();
                String newTitle = "Livro Novo " + UUID.randomUUID();

                bookService.updateBook(updateId, "  " + newTitle + "  ", 8);

                Book afterUpdate = bookService.findBookById(updateId);

                check("updateBook altera e limpa o título",
                        afterUpdate.getTitle().equals(newTitle));

                check("updateBook altera o total de cópias",
                        afterUpdate.getTotalCopies().equals(8));

                check("updateBook com id inexistente lança BookNotFoundException",
                        throwsException(BookNotFoundException.class,
                                () -> bookService.updateBook(-1, "Titulo", 5)));

                check("updateBook título em branco lança InvalidBookDataException",
                        throwsException(InvalidBookDataException.class,
                                () -> bookService.updateBook(updateId, "   ", 5)));

                check("updateBook total negativo lança InvalidBookDataException",
                        throwsException(InvalidBookDataException.class,
                                () -> bookService.updateBook(updateId, "Titulo", -1)));

                check("updateBook total null lança InvalidBookDataException",
                        throwsException(InvalidBookDataException.class,
                                () -> bookService.updateBook(updateId, "Titulo", null)));

                check("updateBook com dados inválidos não altera o banco",
                        bookService.findBookById(updateId)
                                .getTitle()
                                .equals(newTitle));

                // 5. deleteBook sem empréstimos
                Book toDelete = bookService.registerBook(
                        "Livro Para Apagar " + UUID.randomUUID(),
                        1
                );

                Integer deleteId = toDelete.getId();

                bookService.deleteBook(deleteId);

                check("deleteBook remove o livro do banco",
                        bookRepo.findById(deleteId).isEmpty());

                check("deleteBook de livro já removido lança BookNotFoundException",
                        throwsException(BookNotFoundException.class,
                                () -> bookService.deleteBook(deleteId)));

                check("deleteBook com id inexistente lança BookNotFoundException",
                        throwsException(BookNotFoundException.class,
                                () -> bookService.deleteBook(-1)));

                // 6. updateBook não reduz abaixo de empréstimos não devolvidos
                User updateUser = userService.registerUser(
                        "User Update",
                        "update." + UUID.randomUUID() + "@example.com"
                );

                Book bookWithOpenLoan = bookService.registerBook(
                        "Book With Open Loan " + UUID.randomUUID(),
                        2
                );

                LocalDate today = LocalDate.now();

                loanRepo.addLoan(new Loan(
                        updateUser.getId(),
                        bookWithOpenLoan.getId(),
                        today,
                        today.plusDays(14)
                ));

                check("updateBook não reduz cópias abaixo de empréstimos não devolvidos",
                        throwsException(InvalidBookDataException.class,
                                () -> bookService.updateBook(
                                        bookWithOpenLoan.getId(),
                                        bookWithOpenLoan.getTitle(),
                                        0
                                )));

                // 7. deleteBook bloqueia histórico, inclusive devolvido
                User deleteUser = userService.registerUser(
                        "User Delete",
                        "delete." + UUID.randomUUID() + "@example.com"
                );

                Book bookWithHistory = bookService.registerBook(
                        "Book With History " + UUID.randomUUID(),
                        1
                );

                Loan returnedLoan = loanRepo.addLoan(new Loan(
                        deleteUser.getId(),
                        bookWithHistory.getId(),
                        today,
                        today.plusDays(14)
                ));

                returnedLoan.setStatus(LoanStatus.RETURNED);
                loanRepo.updateLoan(returnedLoan);

                check("deleteBook bloqueia livro com histórico de empréstimos",
                        throwsException(BookHasLoansException.class,
                                () -> bookService.deleteBook(bookWithHistory.getId())));

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

package services;

import database.ConnFactory;
import exception.EmailAlreadyExistException;
import exception.InvalidUserDataException;
import exception.UserHasLoansException;
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
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class UserServiceTest {

    public static void main(String[] args) throws Exception {
        try (Connection conn = ConnFactory.getConnection()) {
            conn.setAutoCommit(false);

            try {
                UserJdbcRepo userRepo = new UserJdbcRepo(conn);
                LoanJdbcRepo loanRepo = new LoanJdbcRepo(conn);
                BookJdbcRepo bookRepo = new BookJdbcRepo(conn);

                UserService service = new UserService(userRepo, loanRepo);

                // 1. cadastro válido, com espaços e maiúsculas
                String rawEmail = "  ANA." + UUID.randomUUID() + "@Gmail.COM  ";

                User saved = service.registerUser("  Ana Silva  ", rawEmail);

                check("registerUser devolve id gerado",
                        saved.getId() != null && saved.getId() > 0);

                Optional<User> fromDb = userRepo.findById(saved.getId());

                check("nome salvo sem espaços nas pontas e sem mudar maiúsculas",
                        fromDb.isPresent()
                                && fromDb.get().getName().equals("Ana Silva"));

                check("e-mail salvo normalizado",
                        fromDb.isPresent()
                                && fromDb.get().getEmail()
                                .equals(rawEmail.strip().toLowerCase(Locale.ROOT)));

                // 2. nome inválido
                check("nome null lança InvalidUserDataException",
                        throwsException(InvalidUserDataException.class,
                                () -> service.registerUser(null, uniqueEmail())));

                check("nome vazio lança InvalidUserDataException",
                        throwsException(InvalidUserDataException.class,
                                () -> service.registerUser("", uniqueEmail())));

                check("nome só com espaços lança InvalidUserDataException",
                        throwsException(InvalidUserDataException.class,
                                () -> service.registerUser("   ", uniqueEmail())));

                // 3. e-mail inválido
                check("e-mail null lança InvalidUserDataException",
                        throwsException(InvalidUserDataException.class,
                                () -> service.registerUser("Ana", null)));

                check("e-mail em branco lança InvalidUserDataException",
                        throwsException(InvalidUserDataException.class,
                                () -> service.registerUser("Ana", "   ")));

                check("e-mail sem domínio lança InvalidUserDataException",
                        throwsException(InvalidUserDataException.class,
                                () -> service.registerUser("Ana", "ana@")));

                check("e-mail sem usuário lança InvalidUserDataException",
                        throwsException(InvalidUserDataException.class,
                                () -> service.registerUser("Ana", "@gmail.com")));

                // 4. e-mail repetido, inclusive com maiúsculas diferentes
                String email = uniqueEmail();

                service.registerUser("Ana", email);

                check("e-mail repetido lança EmailAlreadyExistException",
                        throwsException(EmailAlreadyExistException.class,
                                () -> service.registerUser("Outra Ana", email)));

                check("e-mail repetido com outra caixa também lança",
                        throwsException(EmailAlreadyExistException.class,
                                () -> service.registerUser(
                                        "Outra Ana",
                                        email.toUpperCase(Locale.ROOT)
                                )));

                // 5. findUserById
                check("findUserById encontra o usuário",
                        service.findUserById(saved.getId())
                                .getId()
                                .equals(saved.getId()));

                check("findUserById inexistente lança UserNotFoundException",
                        throwsException(UserNotFoundException.class,
                                () -> service.findUserById(-1)));

                // 6. findUserByEmail
                check("findUserByEmail encontra ignorando espaços e maiúsculas",
                        service.findUserByEmail(rawEmail)
                                .getId()
                                .equals(saved.getId()));

                check("findUserByEmail inexistente lança UserNotFoundException",
                        throwsException(UserNotFoundException.class,
                                () -> service.findUserByEmail(uniqueEmail())));

                check("findUserByEmail null lança InvalidUserDataException",
                        throwsException(InvalidUserDataException.class,
                                () -> service.findUserByEmail(null)));

                check("findUserByEmail em branco lança InvalidUserDataException",
                        throwsException(InvalidUserDataException.class,
                                () -> service.findUserByEmail("   ")));

                // 7. findAllUsers
                check("findAllUsers contém o usuário cadastrado",
                        service.findAllUsers().stream()
                                .anyMatch(user -> user.getId().equals(saved.getId())));

                // 8. updateUser
                String oldEmail = uniqueEmail();

                User toUpdate = service.registerUser("Nome Antigo", oldEmail);
                Integer updateId = toUpdate.getId();

                String newEmail = "  NOVO." + UUID.randomUUID() + "@Gmail.COM  ";

                service.updateUser(updateId, "  Nome Novo  ", newEmail);

                User afterUpdate = service.findUserById(updateId);

                check("updateUser altera e limpa o nome",
                        afterUpdate.getName().equals("Nome Novo"));

                check("updateUser altera e normaliza o e-mail",
                        afterUpdate.getEmail()
                                .equals(newEmail.strip().toLowerCase(Locale.ROOT)));

                check("updateUser mantendo o próprio e-mail não lança erro",
                        !throwsException(Exception.class,
                                () -> service.updateUser(
                                        updateId,
                                        "Só o Nome",
                                        afterUpdate.getEmail()
                                )));

                check("updateUser mudou só o nome",
                        service.findUserById(updateId)
                                .getName()
                                .equals("Só o Nome"));

                User other = service.registerUser("Outra Pessoa", uniqueEmail());

                check("updateUser com e-mail de outra pessoa lança EmailAlreadyExistException",
                        throwsException(EmailAlreadyExistException.class,
                                () -> service.updateUser(
                                        updateId,
                                        "Nome",
                                        other.getEmail()
                                )));

                check("updateUser com id inexistente lança UserNotFoundException",
                        throwsException(UserNotFoundException.class,
                                () -> service.updateUser(-1, "Nome", uniqueEmail())));

                check("updateUser nome em branco lança InvalidUserDataException",
                        throwsException(InvalidUserDataException.class,
                                () -> service.updateUser(updateId, "   ", uniqueEmail())));

                check("updateUser e-mail sem domínio lança InvalidUserDataException",
                        throwsException(InvalidUserDataException.class,
                                () -> service.updateUser(updateId, "Nome", "ana@")));

                check("updateUser com dados inválidos não altera o banco",
                        service.findUserById(updateId)
                                .getEmail()
                                .equals(afterUpdate.getEmail()));

                // 9. deleteUser sem histórico
                User toDelete = service.registerUser("Para Apagar", uniqueEmail());
                Integer deleteId = toDelete.getId();

                service.deleteUser(deleteId);

                check("deleteUser remove o usuário do banco",
                        userRepo.findById(deleteId).isEmpty());

                check("deleteUser de usuário já removido lança UserNotFoundException",
                        throwsException(UserNotFoundException.class,
                                () -> service.deleteUser(deleteId)));

                check("deleteUser com id inexistente lança UserNotFoundException",
                        throwsException(UserNotFoundException.class,
                                () -> service.deleteUser(-1)));

                // 10. deleteUser bloqueia usuário com histórico de empréstimos
                User userWithHistory = service.registerUser(
                        "User With History",
                        "history." + UUID.randomUUID() + "@example.com"
                );

                Book book = bookRepo.addBook(new Book(
                        "Book For User History " + UUID.randomUUID(),
                        1
                ));

                LocalDate today = LocalDate.now();

                Loan returnedLoan = loanRepo.addLoan(new Loan(
                        userWithHistory.getId(),
                        book.getId(),
                        today,
                        today.plusDays(14)
                ));

                returnedLoan.setStatus(LoanStatus.RETURNED);
                loanRepo.updateLoan(returnedLoan);

                check("deleteUser bloqueia usuário com histórico de empréstimos",
                        throwsException(UserHasLoansException.class,
                                () -> service.deleteUser(userWithHistory.getId())));

            } finally {
                conn.rollback();
            }
        }
    }

    private static String uniqueEmail() {
        return "test." + UUID.randomUUID() + "@example.com";
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

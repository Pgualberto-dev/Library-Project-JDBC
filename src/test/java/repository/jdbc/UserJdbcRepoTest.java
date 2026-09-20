package repository.jdbc;
import database.ConnFactory;
import model.User;
import java.sql.Connection;
import java.util.Optional;

    public class UserJdbcRepoTest {

        public static void main(String[] args) throws Exception {
            try (Connection conn = ConnFactory.getConnection()) {
                UserJdbcRepo repo = new UserJdbcRepo(conn);
                String email = "teste" + System.currentTimeMillis() + "@mail.com";

                // 1. addUser
                User saved = repo.addUser(new User("Pablo Teste", email));
                check("addUser devolve id gerado", saved.getId() > 0);

                // 2. findById e findByEmail
                Optional<User> byId = repo.findById(saved.getId());
                check("findById encontra", byId.isPresent() && byId.get().getEmail().equals(email));
                Optional<User> byEmail = repo.findByEmail(email);
                check("findByEmail encontra", byEmail.isPresent() && byEmail.get().getId() == saved.getId());

                // 3. updateUser + confirmação no banco
                String newEmail = "novo" + email;
                boolean updated = repo.updateUser(new User(saved.getId(), "Nome Novo", newEmail));
                check("updateUser retorna true", updated);
                Optional<User> afterUpdate = repo.findById(saved.getId());
                check("banco refletiu o update",
                        afterUpdate.isPresent()
                                && afterUpdate.get().getName().equals("Nome Novo")
                                && afterUpdate.get().getEmail().equals(newEmail));

                // 4. findAll
                boolean inList = repo.findAll().stream().anyMatch(u -> u.getId() == saved.getId());
                check("findAll contém o usuário", inList);

                // 5. caminhos tristes
                check("findById inexistente vem vazio", repo.findById(-1).isEmpty());
                check("updateUser inexistente retorna false",
                        !repo.updateUser(new User(-1, "X", "x" + email)));
                check("deleteById inexistente retorna false", !repo.deleteById(-1));

                try {
                    repo.addUser(new User("Duplicado", newEmail));
                    check("e-mail duplicado deve lançar exceção", false);
                } catch (RuntimeException e) {
                    check("e-mail duplicado lançou exceção", true);
                    System.out.println("   mensagem: " + e.getMessage());
                    System.out.println("   causa: " + e.getCause().getClass().getName());
                }

                // 6. deleteById + confirmação
                check("deleteById retorna true", repo.deleteById(saved.getId()));
                check("após delete, findById vem vazio", repo.findById(saved.getId()).isEmpty());
            }
        }

        private static void check(String description, boolean condition) {
            System.out.println((condition ? "✅ " : "❌ ") + description);
        }
    }


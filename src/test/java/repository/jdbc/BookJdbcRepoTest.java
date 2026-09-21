package repository.jdbc;

import database.ConnFactory;
import model.Book;

import java.sql.Connection;
import java.util.Optional;

public class BookJdbcRepoTest {

    public static void main(String[] args) throws Exception {
        try (Connection conn = ConnFactory.getConnection()) {
            BookJdbcRepo repo = new BookJdbcRepo(conn);
            String title = "Livro Teste " + System.currentTimeMillis();

            // 1. addBook
            Book saved = repo.addBook(new Book(title, 5));
            check("addBook devolve id gerado", saved.getId() > 0);

            // 2. findById
            Optional<Book> byId = repo.findById(saved.getId());
            check("findById encontra",
                    byId.isPresent()
                            && byId.get().getTitle().equals(title)
                            && byId.get().getTotalCopies() == 5);

            // 3. updateBooks + confirmação no banco
            String newTitle = "Novo " + title;
            boolean updated = repo.updateBooks(new Book(saved.getId(), newTitle, 8));
            check("updateBooks retorna true", updated);
            Optional<Book> afterUpdate = repo.findById(saved.getId());
            check("banco refletiu o update",
                    afterUpdate.isPresent()
                            && afterUpdate.get().getTitle().equals(newTitle)
                            && afterUpdate.get().getTotalCopies() == 8);

            // 4. findAll
            boolean inList = repo.findAll().stream().anyMatch(b -> b.getId().equals(saved.getId()));
            check("findAll contém o livro", inList);

            // 5. caminhos tristes
            check("findById inexistente vem vazio", repo.findById(-1).isEmpty());
            check("updateBooks inexistente retorna false",
                    !repo.updateBooks(new Book(-1, "X", 1)));
            check("deleteById inexistente retorna false", !repo.deleteById(-1));

            // 6. deleteById + confirmação
            check("deleteById retorna true", repo.deleteById(saved.getId()));
            check("após delete, findById vem vazio", repo.findById(saved.getId()).isEmpty());
        }
    }

    private static void check(String description, boolean condition) {
        System.out.println((condition ? "✅ " : "❌ ") + description);
    }
}

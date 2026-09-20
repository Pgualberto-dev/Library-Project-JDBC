package repository;

import model.Book;
import java.util.List;
import java.util.Optional;

public interface BookRepository {

    Book addBook(Book obj);
    boolean updateBooks(Book obj);
    boolean deleteById(Integer id);
    Optional <Book> findById(Integer id);
    List<Book> findAll();
}

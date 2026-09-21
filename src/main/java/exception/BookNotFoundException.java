package exception;

import java.io.Serial;

public class BookNotFoundException extends LibraryException {

    public BookNotFoundException(Integer id) {
        super("Book with id " + id + " not found");
    }
}

package exception;

public class NoCopiesAvailableException extends LibraryException {
    public NoCopiesAvailableException(Integer id) {
        super("No copies available for Book with id " + id);
    }
}

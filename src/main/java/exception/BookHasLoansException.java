package exception;

public class BookHasLoansException extends LibraryException {
    public BookHasLoansException(Integer bookId) {
        super("Book with id " + bookId + " has loan history and cannot be deleted");
    }
}

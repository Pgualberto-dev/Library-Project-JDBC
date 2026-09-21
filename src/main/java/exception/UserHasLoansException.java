package exception;

public class UserHasLoansException extends LibraryException {
    public UserHasLoansException(Integer userId) {
        super("User with id " + userId + " has loan history and cannot be deleted");
    }
}

package exception;

public class UserNotFoundException extends LibraryException {
    public UserNotFoundException(int id) {
        super("User with id " + id + " not found");
    }
    public UserNotFoundException(String email) {
        super("User with e-mail " + email + " not found");
    }
}

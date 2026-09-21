package exception;

import java.io.Serial;

public class EmailAlreadyExistException extends LibraryException {

    public EmailAlreadyExistException(String email) {
        super("The email " + email + " is already registered.");
    }
}

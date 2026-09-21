package exception;

import java.io.Serial;

public class InvalidBookDataException extends LibraryException {

    public InvalidBookDataException(String message) {
        super(message);
    }
}

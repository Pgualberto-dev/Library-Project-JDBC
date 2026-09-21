package exception;

public class LoanNotFoundException extends LibraryException {
    public LoanNotFoundException(Integer id) {
        super("Loan with id " + id + " not found");
    }
}

package repository;

import model.Loan;
import java.util.List;
import java.util.Optional;

public interface LoanRepository {

    Loan addLoan(Loan obj);
    boolean updateLoan(Loan obj);
    Optional<Loan> findById(Integer id);
    List<Loan> findAllByBookId(Integer bookId);
    List<Loan> findAllByUserId(Integer userId);
    List<Loan> findAll();
    int countUnreturnedByBookId(Integer bookId);
}

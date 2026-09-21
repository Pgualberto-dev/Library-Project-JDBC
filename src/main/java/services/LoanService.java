package services;

import exception.LoanNotFoundException;
import exception.NoCopiesAvailableException;
import model.Book;
import model.Loan;
import model.LoanStatus;
import repository.LoanRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class LoanService {

    private final LoanRepository loanRepository;
    private final UserService userService;
    private final BookService bookService;

    public LoanService(LoanRepository loanRepository, UserService userService, BookService bookService) {
        this.loanRepository = loanRepository;
        this.userService = userService;
        this.bookService = bookService;
    }

    public Loan lendBook(Integer userId, Integer bookId) {
        userService.findUserById(userId);
        Book book = bookService.findBookById(bookId);
        int count = loanRepository.countUnreturnedByBookId(book.getId());
        int availableCopies = book.getTotalCopies() - count;
        if (availableCopies <= 0) {
            throw new NoCopiesAvailableException(bookId);
        }
        LocalDate loanDate = LocalDate.now();
        LocalDate dueDate = loanDate.plusDays(14);
        return loanRepository.addLoan(new Loan(userId, bookId, loanDate, dueDate));
    }

    public Loan findLoanById(Integer loanId) {
        Optional<Loan> loan = loanRepository.findById(loanId);
        if (loan.isEmpty()) {
            throw new LoanNotFoundException(loanId);
        }
        return loan.get();
    }

    public void returnLoan(Integer loanId) {
       Loan loan =  findLoanById(loanId);
       loan.setStatus(LoanStatus.RETURNED);
        boolean returning = loanRepository.updateLoan(loan);
        if (!returning) {
          throw new LoanNotFoundException(loanId);
        }
    }

    public List<Loan> findAllLoans() {
        return loanRepository.findAll();
    }

    public List<Loan> findAllLoansByUserId(Integer userId) {
        userService.findUserById(userId);
        return loanRepository.findAllByUserId(userId);
    }
    public List<Loan> findAllLoansByBookId(Integer bookId) {
        bookService.findBookById(bookId);
       return loanRepository.findAllByBookId(bookId);
    }

    public void updateOverdueLoans() {
        List<Loan> loans = loanRepository.findAll();
        LocalDate today = LocalDate.now();
        for (Loan loan : loans) {
            if (loan.getStatus() == LoanStatus.ACTIVE && loan.getDueDate().isBefore(today)) {
                loan.setStatus(LoanStatus.OVERDUE);
                boolean updated = loanRepository.updateLoan(loan);
                if (!updated){
                    throw new LoanNotFoundException(loan.getId());
                }
            }
        }

    }
}

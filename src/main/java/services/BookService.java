package services;

import exception.BookHasLoansException;
import exception.BookNotFoundException;
import exception.InvalidBookDataException;
import model.Book;
import repository.BookRepository;
import repository.LoanRepository;

import java.util.List;
import java.util.Optional;

public class BookService {

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;

    public BookService(BookRepository bookRepository, LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
    }

    public Book registerBook(String title,Integer totalCopies ) {
        String titleTemp = validateTitle(title);
        Integer totalCopiesTemp = validateTotalCopies(totalCopies);
        return bookRepository.addBook(new Book(titleTemp, totalCopiesTemp));
    }

    public Book findBookById(Integer bookId){
        Optional<Book> book = bookRepository.findById(bookId);
        return book.orElseThrow(() -> new BookNotFoundException(bookId));
    }

    public List<Book> findAllBooks(){
        return bookRepository.findAll();
    }

    public void updateBook(Integer bookId,String title,Integer totalCopies){
        findBookById(bookId);
        String titleTemp = validateTitle(title);
        Integer totalCopiesTemp = validateTotalCopies(totalCopies);
        int unreturnedLoans = loanRepository.countUnreturnedByBookId(bookId);
        if (unreturnedLoans > totalCopiesTemp){
            throw new InvalidBookDataException("Total copies cannot be less than unreturned loans");
        }
        Book book = new Book(bookId, titleTemp, totalCopiesTemp);
        boolean update = bookRepository.updateBooks(book);
        if (!update){
            throw  new BookNotFoundException(bookId);
        }
    }

    public void deleteBook(Integer bookId){
        if(!loanRepository.findAllByBookId(bookId).isEmpty()){
            throw new BookHasLoansException(bookId);
        }
       boolean delete = bookRepository.deleteById(bookId);
       if (!delete){
            throw  new BookNotFoundException(bookId);
       }
    }
    private static String validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new InvalidBookDataException("Name cannot be null");
        }
        return title.strip();
    }

    private static Integer validateTotalCopies(Integer totalCopies) {
        if (totalCopies == null || totalCopies < 0) {
            throw new InvalidBookDataException("Total copies cannot be null or negative");
        }
        return totalCopies;
    }
}

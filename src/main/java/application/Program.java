package application;

import database.ConnFactory;
import repository.BookRepository;
import repository.LoanRepository;
import repository.UserRepository;
import repository.jdbc.BookJdbcRepo;
import repository.jdbc.LoanJdbcRepo;
import repository.jdbc.UserJdbcRepo;
import services.BookService;
import services.LoanService;
import services.UserService;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class Program {
    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in);
             Connection conn = ConnFactory.getConnection()) {
            UserRepository userRepo = new UserJdbcRepo(conn);
            BookRepository bookRepo = new BookJdbcRepo(conn);
            LoanRepository loanRepo = new LoanJdbcRepo(conn);
            UserService userService = new UserService(userRepo, loanRepo);
            BookService bookService = new BookService(bookRepo, loanRepo);
            LoanService loanService = new LoanService(loanRepo, userService, bookService);
            ConsoleMenu consoleMenu = new ConsoleMenu();
            consoleMenu.start(sc, userService, bookService, loanService);

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Application error: " + e.getMessage());
        }
    }
}

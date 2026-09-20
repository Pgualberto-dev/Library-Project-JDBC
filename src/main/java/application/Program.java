package application;

import database.ConnFactory;
import model.Book;
import model.User;
import repository.BookRepository;
import repository.UserRepository;
import repository.jdbc.BookJdbcRepo;
import repository.jdbc.UserJdbcRepo;
import java.sql.Connection;
import java.sql.SQLException;

public class Program {
    public static void main(String[] args) {


//        try (Connection conn = ConnFactory.getConnection()) {
//            User user = new User("Alan", "Al@gmail.com");
//            UserRepository addUser = new UserJdbcRepo(conn);
//            User addedUser = addUser.addUser(user);
//            System.out.println("Added user: " + addedUser);
//        }catch (SQLException e){
//            System.out.println("Error: " + e.getMessage());
//        }

        try (Connection conn = ConnFactory.getConnection()) {
            Book book = new Book("Alma morta", 15);
            BookRepository addBook = new BookJdbcRepo(conn);
            Book addedBook = addBook.addBook(book);
            System.out.println("Added Book: " + addedBook);
        }catch (SQLException e){
            System.out.println("Error: " + e.getMessage());
        }
    }
}

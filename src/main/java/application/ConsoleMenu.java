package application;

import exception.LibraryException;
import model.Book;
import model.Loan;
import model.User;
import services.BookService;
import services.LoanService;
import services.UserService;

import java.util.List;
import java.util.Scanner;

public class ConsoleMenu {

    public void start(Scanner scanner, UserService userService, BookService bookService, LoanService loanService) {
        boolean running = true;

        while (running) {
            System.out.println("\n=== LIBRARY MANAGEMENT SYSTEM ===");
            System.out.println("[1] - Users");
            System.out.println("[2] - Books");
            System.out.println("[3] - Loans");
            System.out.println("[0] - Exit");

            switch (readInt(scanner, "Enter number: ")) {
                case 1 -> userMenu(scanner, userService);
                case 2 -> bookMenu(scanner, bookService);
                case 3 -> loanMenu(scanner, loanService);
                case 0 -> {
                    System.out.println("Exiting the Library Management System...");
                    running = false;
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void userMenu(Scanner scanner, UserService userService) {
        boolean running = true;

        while (running) {
            System.out.println("\n--- USER MENU ---");
            System.out.println("[1] - Add User");
            System.out.println("[2] - Find User by ID");
            System.out.println("[3] - Find User by Email");
            System.out.println("[4] - List All Users");
            System.out.println("[5] - Update User");
            System.out.println("[6] - Delete User");
            System.out.println("[0] - Back to Main Menu");
            try {
                switch (readInt(scanner, "Enter number: ")) {
                    case 1 -> addUser(scanner, userService);
                    case 2 -> System.out.println(userService.findUserById(readInt(scanner, "Enter user ID: ")));
                    case 3 -> System.out.println(userService.findUserByEmail(readText(scanner, "Enter user email: ")));
                    case 4 -> printUsers(userService.findAllUsers());
                    case 5 -> updateUser(scanner, userService);
                    case 6 -> deleteUser(scanner, userService);
                    case 0 -> running = false;
                    default -> System.out.println("Invalid option. Please try again.");
                }
            } catch (LibraryException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
    private void bookMenu(Scanner scanner, BookService bookService) {
        boolean running = true;

        while (running) {
            System.out.println("\n--- BOOK MENU ---");
            System.out.println("[1] - Add Book");
            System.out.println("[2] - Find Book by ID");
            System.out.println("[3] - List All Books");
            System.out.println("[4] - Update Book");
            System.out.println("[5] - Delete Book");
            System.out.println("[0] - Back to Main Menu");

            try {
                switch (readInt(scanner, "Enter number: ")) {
                    case 1 -> addBook(scanner, bookService);
                    case 2 -> System.out.println(bookService.findBookById(readInt(scanner, "Enter book ID: ")));
                    case 3 -> printBooks(bookService.findAllBooks());
                    case 4 -> updateBook(scanner, bookService);
                    case 5 -> deleteBook(scanner, bookService);
                    case 0 -> running = false;
                    default -> System.out.println("Invalid option. Please try again.");
                }
            } catch (LibraryException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
    private void loanMenu(Scanner scanner, LoanService loanService) {
        boolean running = true;

        while (running) {
            System.out.println("\n--- LOAN MENU ---");
            System.out.println("[1] - Lend Book");
            System.out.println("[2] - Return Book");
            System.out.println("[3] - List All Loans");
            System.out.println("[4] - Find Loan by ID");
            System.out.println("[5] - List Loans by User ID");
            System.out.println("[6] - List Loans by Book ID");
            System.out.println("[7] - Update Overdue Loans");
            System.out.println("[0] - Back to Main Menu");

            try {
                switch (readInt(scanner, "Enter number: ")) {
                    case 1 -> lendBook(scanner, loanService);
                    case 2 -> returnBook(scanner, loanService);
                    case 3 -> printLoans(loanService.findAllLoans());
                    case 4 -> System.out.println(loanService.findLoanById(readInt(scanner, "Enter loan ID: ")));
                    case 5 -> printLoans(loanService.findAllLoansByUserId(readInt(scanner, "Enter user ID: ")));
                    case 6 -> printLoans(loanService.findAllLoansByBookId(readInt(scanner, "Enter book ID: ")));
                    case 7 -> {
                        loanService.updateOverdueLoans();
                        System.out.println("Overdue loans updated successfully.");
                    }
                    case 0 -> running = false;
                    default -> System.out.println("Invalid option. Please try again.");
                }
            } catch (LibraryException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void addUser(Scanner scanner, UserService userService) {
        User user = userService.registerUser(
                readText(scanner, "Enter user name: "),
                readText(scanner, "Enter user email: ")
        );
        System.out.println("User registered successfully: " + user);
    }

    private void updateUser(Scanner scanner, UserService userService) {
        int id = readInt(scanner, "Enter user ID: ");
        String name = readText(scanner, "Enter new user name: ");
        String email = readText(scanner, "Enter new user email: ");
        userService.updateUser(id, name, email);
        System.out.println("User updated successfully.");
    }

    private void deleteUser(Scanner scanner, UserService userService) {
        userService.deleteUser(readInt(scanner, "Enter user ID: "));
        System.out.println("User deleted successfully.");
    }

    private void addBook(Scanner scanner, BookService bookService) {
        Book book = bookService.registerBook(
                readText(scanner, "Enter book title: "),
                readInt(scanner, "Enter total copies: ")
        );
        System.out.println("Book registered successfully: " + book);
    }

    private void updateBook(Scanner scanner, BookService bookService) {
        int id = readInt(scanner, "Enter book ID: ");
        String title = readText(scanner, "Enter new book title: ");
        int totalCopies = readInt(scanner, "Enter new total copies: ");
        bookService.updateBook(id, title, totalCopies);
        System.out.println("Book updated successfully.");
    }

    private void deleteBook(Scanner scanner, BookService bookService) {
        bookService.deleteBook(readInt(scanner, "Enter book ID: "));
        System.out.println("Book deleted successfully.");
    }

    private void lendBook(Scanner scanner, LoanService loanService) {
        Loan loan = loanService.lendBook(
                readInt(scanner, "Enter user ID: "),
                readInt(scanner, "Enter book ID: ")
        );
        System.out.println("Book lent successfully: " + loan);
    }

    private void returnBook(Scanner scanner, LoanService loanService) {
        loanService.returnLoan(readInt(scanner, "Enter loan ID: "));
        System.out.println("Book returned successfully.");
    }

    private int readInt(Scanner scanner, String message) {
        while (true) {
            String value = readText(scanner, message);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid whole number.");
            }
        }
    }

    private String readText(Scanner scanner, String message) {
        System.out.print(message);
        return scanner.nextLine();
    }

    private void printUsers(List<User> users) {
        printList(users, "No users registered.");
    }

    private void printBooks(List<Book> books) {
        printList(books, "No books registered.");
    }

    private void printLoans(List<Loan> loans) {
        printList(loans, "No loans registered.");
    }

    private void printList(List<?> items, String emptyMessage) {
        if (items.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        items.forEach(System.out::println);
    }
}

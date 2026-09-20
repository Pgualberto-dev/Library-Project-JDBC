package repository.jdbc;

import model.Book;
import repository.BookRepository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookJdbcRepo implements BookRepository{

    private final Connection conn;
    public BookJdbcRepo(Connection conn) {
        this.conn = conn;
    }

    private Book mapBook(ResultSet rs) throws SQLException {
        int id = rs.getInt("book_id");
        String name = rs.getString("title");
        int totalCopies = rs.getInt("total_copies");
        return new Book(id, name, totalCopies);
    }

    @Override
    public Book addBook(Book obj) {
        try (PreparedStatement stmt = conn.prepareStatement("INSERT  INTO books (title, total_copies) VALUES (?, ?)",  Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1,obj.getTitle());
            stmt.setInt(2,obj.getTotalCopies());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows != 1) {
                throw new RuntimeException("Failed to insert book");
            }
            try (ResultSet rs = stmt.getGeneratedKeys()){
                if(rs.next()){
                    int id = rs.getInt(1);
                    return new Book(id, obj.getTitle(), obj.getTotalCopies());
                }else {
                    throw new RuntimeException("Error inserting book");
                }
            }
        }catch (SQLException e){
            throw new RuntimeException("Error adding book: " + e.getMessage(), e);
        }
    }
    @Override
    public boolean updateBooks(Book obj) {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE books SET title = ?, total_copies = ? WHERE  book_id = ?")){
            stmt.setString(1, obj.getTitle());
            stmt.setInt(2, obj.getTotalCopies());
            stmt.setInt(3, obj.getId());
            int affectedRows = stmt.executeUpdate();
            return affectedRows == 1;
        }catch (SQLException e){
            throw new RuntimeException("Error updating book: " + e.getMessage(), e);
        }
    }
    @Override
    public boolean deleteById(Integer id) {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE  FROM books WHERE books.book_id =?")){
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows == 1;
        } catch (SQLException e){
            throw new RuntimeException("Error deleting book: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Book> findById(Integer id) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT books.book_id, title, total_copies FROM books WHERE book_id = ?")){
            stmt.setInt(1,id);
            try (ResultSet rs = stmt.executeQuery()){
                if (rs.next()){
                    return Optional.of(mapBook(rs));
                }
            }
            return Optional.empty();
        }catch (SQLException e){
            throw new RuntimeException("Error finding book: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Book> findAll() {
        List<Book> books = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT books.book_id, title, total_copies FROM books ORDER BY title")){
            try (ResultSet rs = stmt.executeQuery()){
                while (rs.next()){
                    books.add(mapBook(rs));
                }
            }
            return books;
        }catch (SQLException e){
            throw new RuntimeException("Error finding books: " + e.getMessage(), e);
        }
    }
}

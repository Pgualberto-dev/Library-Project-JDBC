package repository.jdbc;

import model.Loan;
import model.LoanStatus;
import repository.LoanRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoanJdbcRepo implements LoanRepository {

    private final Connection conn;

    public LoanJdbcRepo(Connection conn) {
        this.conn = conn;
    }

    private Loan mapLoan(ResultSet rs) throws SQLException {
        int id = rs.getInt("loan_id");
        int userId = rs.getInt("user_id");
        int bookId = rs.getInt("book_id");
        LocalDate loanDate = rs.getDate("loan_date").toLocalDate();
        LocalDate dueDate = rs.getDate("due_date").toLocalDate();
        LoanStatus status = LoanStatus.fromDb(rs.getString("status"));
        ;
        return new Loan(id, userId, bookId, loanDate, dueDate, status);
    }

    @Override
    public Loan addLoan(Loan obj) {
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO loan (user_id, book_id, loan_date, due_date, status) VALUES (?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, obj.getUserId());
            stmt.setInt(2, obj.getBookId());
            stmt.setDate(3, java.sql.Date.valueOf(obj.getLoanDate()));
            stmt.setDate(4, java.sql.Date.valueOf(obj.getDueDate()));
            stmt.setString(5, obj.getStatus().getDbValue());
            ;
            int affectedRows = stmt.executeUpdate();
            if (affectedRows != 1) {
                throw new SQLException("Creating loan failed, no rows affected.");
            }
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return new Loan(id, obj.getUserId(), obj.getBookId(), obj.getLoanDate(), obj.getDueDate(), obj.getStatus());
                } else {
                    throw new SQLException("Creating loan failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error adding loan: " + e.getMessage(), e);

        }
    }

    @Override
    public boolean updateLoan(Loan obj) {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE loan SET due_date = ?, status = ?  WHERE loan_id = ?")) {
            stmt.setDate(1, java.sql.Date.valueOf(obj.getDueDate()));
            stmt.setString(2, obj.getStatus().getDbValue());
            stmt.setInt(3, obj.getId());
            int affectedRows = stmt.executeUpdate();
            return affectedRows == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating loan: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Loan> findById(Integer id) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT loan_id, user_id, book_id, loan_date, due_date, status  FROM loan WHERE loan_id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapLoan(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding loan by id: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Loan> findAllByBookId(Integer bookId) {
        List<Loan> loans = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT loan_id, user_id, book_id, loan_date, due_date, status FROM loan WHERE book_id = ? ORDER BY loan_date DESC ")) {
            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    loans.add(mapLoan(rs));
                }
            }
            return loans;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding loans by books id: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Loan> findAllByUserId(Integer userId) {
        List<Loan> loans = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT loan_id, user_id, book_id, loan_date, due_date, status FROM loan WHERE user_id = ? ORDER BY loan_date DESC ")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    loans.add(mapLoan(rs));
                }
            }
            return loans;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding loan by users id: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Loan> findAll() {
        List<Loan> loans = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT loan_id, user_id, book_id, loan_date, due_date, status FROM loan ORDER BY loan_date DESC ")) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    loans.add(mapLoan(rs));
                }
            }
            return loans;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding loans: " + e.getMessage(), e);
        }
    }

    @Override
    public int countUnreturnedByBookId(Integer bookId) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM loan WHERE book_id = ? AND status <> 'returned'")) {
            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }

            }
            return 0;
        }catch (SQLException e){
            throw new RuntimeException("Error counting unreturned loans by book id: " + e.getMessage(), e);
        }
    }
}



package repository.jdbc;

import model.User;
import repository.UserRepository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserJdbcRepo implements UserRepository {

    private final Connection conn;

    public UserJdbcRepo(Connection conn) {
        this.conn = conn;
    }

    @Override
    public User addUser(User obj) {

        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (name, email) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, obj.getName());
            stmt.setString(2, obj.getEmail());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows != 1) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return new User(id, obj.getName(), obj.getEmail());
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }catch (SQLException e){
            throw new RuntimeException("Error adding user: " + e.getMessage(), e);
        }
    }
    @Override
    public boolean updateUser(User obj) {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE users SET name = ?, email = ? WHERE user_id = ?")) {
            stmt.setString(1, obj.getName());
            stmt.setString(2, obj.getEmail());
            stmt.setInt(3, obj.getId());
            int affectedRows = stmt.executeUpdate();
            return affectedRows == 1;
        } catch (SQLException e){
            throw new RuntimeException("Error updating user: " + e.getMessage(), e);
        }
    }
    @Override
    public boolean deleteById(Integer id) {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE users.user_id = ?")){
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user: " + e.getMessage(), e);
        }
    }
    @Override
    public Optional<User> findById(Integer id) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT user_id, name, email FROM users WHERE user_id = ?")){
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()){
                if (rs.next()){
                    return Optional.of(mapUser(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by id: " + e.getMessage(), e);
        }
    }
    @Override
    public Optional<User> findByEmail(String email) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT user_id, name, email FROM users WHERE email = ?")){
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()){
                if (rs.next()){
                    return Optional.of(mapUser(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by email: " + e.getMessage(), e);
        }
    }
    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT user_id, name, email FROM users ORDER BY user_id")){
            try (ResultSet rs = stmt.executeQuery()){
                while (rs.next()){
                    users.add(mapUser(rs));
                }
            }
            return users;
        }catch (SQLException e) {
            throw new RuntimeException("Error finding users: " + e.getMessage(), e);
        }
    }
    private User mapUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("user_id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        return new User(id, name, email);
    }
}
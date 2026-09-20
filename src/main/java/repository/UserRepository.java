package repository;

import model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User addUser(User obj);
    boolean updateUser(User obj);
    boolean deleteById(Integer id);
    Optional <User>findById(Integer id);
    Optional <User> findByEmail(String email);
    List<User> findAll();

}

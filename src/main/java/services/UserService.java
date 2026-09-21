package services;

import exception.EmailAlreadyExistException;
import exception.InvalidUserDataException;
import exception.UserHasLoansException;
import exception.UserNotFoundException;
import model.User;
import repository.LoanRepository;
import repository.UserRepository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public class UserService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserRepository userRepository;
    private final LoanRepository loanRepository;

    public UserService(UserRepository userRepository, LoanRepository loanRepository) {
        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
    }

    public User registerUser(String name, String email) {
        String nameTemp = validateName(name);
        String emailTemp = validateEmail(email);
        if (userRepository.findByEmail(emailTemp).isPresent()) {
            throw new EmailAlreadyExistException(emailTemp);
        }
        return userRepository.addUser(new User(nameTemp, emailTemp));
    }

    public User findUserById(Integer id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new UserNotFoundException(id);
        }
        return user.get();
    }

    public User findUserByEmail(String email) {
        String emailTemp = normalizeEmail(email);
        Optional<User> user = userRepository.findByEmail(emailTemp);
        if (user.isEmpty()) {
            throw new UserNotFoundException(emailTemp);
        }
        return user.get();
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public void updateUser(Integer id, String name, String email) {
        findUserById(id);
        String nameTemp = validateName(name);
        String emailTemp = validateEmail(email);
        Optional<User> owner = userRepository.findByEmail(emailTemp);
        if (owner.isPresent() && !owner.get().getId().equals(id)) {
            throw new EmailAlreadyExistException(emailTemp);
        }
        User user = new User(id, nameTemp, emailTemp);
        boolean update = userRepository.updateUser(user);
        if (!update) {
            throw new UserNotFoundException(id);
        }
    }

    public void deleteUser(Integer id) {
        if (!loanRepository.findAllByUserId(id).isEmpty()) {
            throw new UserHasLoansException(id);
        }
        boolean delete = userRepository.deleteById(id);
        if (!delete) {
            throw new UserNotFoundException(id);
        }
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidUserDataException("Name cannot be null");
        }
        return name.strip();
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidUserDataException("Email cannot be null");
        }
        return email.strip().toLowerCase(Locale.ROOT);
    }

    private static String validateEmail(String email) {
        String emailTemp = normalizeEmail(email);
        if (!isValidEmail(emailTemp)) {
            throw new InvalidUserDataException("Invalid email");
        }
        return emailTemp;
    }

    private static boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
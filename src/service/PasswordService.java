package service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordService {

    private static final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder();

    public static String hashPassword(String password) {
        return encoder.encode(password);
    }
    
    public static boolean matches(String passwordToCheck, String password) {
        return encoder.matches(passwordToCheck, password);
    }
}

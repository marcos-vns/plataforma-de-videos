package service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordService {

    private static final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder();

    public static String hashPassword(String senha) {
        return encoder.encode(senha);
    }
}

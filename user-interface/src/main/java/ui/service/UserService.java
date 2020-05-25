package ui.service;

import exception.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ui.model.UserEntity;
import ui.repository.UserRepository;
import ui.security.JwtTokenProvider;

import javax.servlet.http.HttpServletRequest;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    public String signin(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            String token = jwtTokenProvider.createToken(username, userRepository.findByUsername(username).getRole());
            logger.info(String.format("User [%s] is signed up with token=[%s]", username, token));
            return token;
        } catch (AuthenticationException e) {
            throw new CustomException("Invalid username/password supplied", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public String signup(UserEntity user) {
        if (!userRepository.existsByUsername(user.getUsername())) {
            if (user.getUsername().contains(" ")) {
                throw new CustomException("Username contains invalid character(s).", HttpStatus.UNPROCESSABLE_ENTITY);
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            String token = jwtTokenProvider.createToken(user.getUsername(), user.getRole());
            logger.info(String.format("User [%s] is signed in with token=[%s]", user.toString(), token));
            return token;
        } else {
            throw new CustomException("Username is already in use", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public void delete(String username) {
        userRepository.deleteByUsername(username);
        logger.info(String.format("User [%s] was deleted successfully.", username));
    }

    public UserEntity search(String username) {
        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            throw new CustomException("The user doesn't exist", HttpStatus.NOT_FOUND);
        }
        logger.info(String.format("User [%s] was retrieved successfully.", user.toString()));
        return user;
    }

    public UserEntity whoami(HttpServletRequest req) {
        UserEntity userEntity = userRepository.findByUsername(jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(req)));
        logger.info(String.format("User entity [%s] retrieved successfully.", userEntity));
        return userEntity;
    }

    public String refresh(String username) {
        String token = jwtTokenProvider.createToken(username, userRepository.findByUsername(username).getRole());
        logger.info(String.format("User [%s] refreshed their token successfully to [%s].", username, token));
        return token;
    }
}

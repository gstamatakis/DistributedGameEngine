package ui;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import model.Role;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import ui.model.UserEntity;
import ui.service.UserService;

@SpringBootApplication
@EnableWebSecurity
public class UserInterfaceApplication implements CommandLineRunner {

    @Autowired
    UserService userService;

    public static void main(String[] args) {
        SpringApplication.run(UserInterfaceApplication.class, args);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Override
    public void run(String... params) {
        //Register 3 users with 3 different Roles
        UserEntity admin = new UserEntity();
        admin.setUsername("admin");
        admin.setPassword("admin");
        admin.setEmail("admin@email.com");
        admin.setRole(Role.ROLE_ADMIN);
        userService.signup(admin);

        UserEntity official = new UserEntity();
        official.setUsername("official");
        official.setPassword("official");
        official.setEmail("official@email.com");
        official.setRole(Role.ROLE_OFFICIAL);
        userService.signup(official);

        UserEntity client1 = new UserEntity();
        client1.setUsername("client1");
        client1.setPassword("client1");
        client1.setEmail("client1@email.com");
        client1.setRole(Role.ROLE_CLIENT);
        userService.signup(client1);

        UserEntity client2 = new UserEntity();
        client2.setUsername("client2");
        client2.setPassword("client2");
        client2.setEmail("client2@email.com");
        client2.setRole(Role.ROLE_CLIENT);
        userService.signup(client2);
    }

}

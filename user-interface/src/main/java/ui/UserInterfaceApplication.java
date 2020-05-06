package ui;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import model.Role;
import ui.model.UserEntity;
import ui.service.UserService;

@SpringBootApplication
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
        //Register 2 important users
        UserEntity admin = new UserEntity();
        admin.setUsername("admin");
        admin.setPassword("admin");
        admin.setEmail("admin@email.com");
        admin.setRole(Role.ROLE_ADMIN);
        userService.signup(admin);

        UserEntity client = new UserEntity();
        client.setUsername("client");
        client.setPassword("client");
        client.setEmail("client@email.com");
        client.setRole(Role.ROLE_CLIENT);
        userService.signup(client);
    }

}

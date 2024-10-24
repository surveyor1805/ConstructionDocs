package ITMO.ConstructionDocs.security;

import ITMO.ConstructionDocs.model.db.repository.UserRepository;
import ITMO.ConstructionDocs.model.dto.request.UserReq;
import ITMO.ConstructionDocs.model.enums.Role;
import ITMO.ConstructionDocs.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final UserRepository userRepository;

    public DataInitializer(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByEmailIgnoreCase("admin@admin.com").isEmpty()) {
            UserReq adminRequest = new UserReq();
            adminRequest.setEmail("admin@admin.com");
            adminRequest.setPassword("admin");
            adminRequest.setFirstName("admin");
            adminRequest.setRole(Role.ADMIN);

            userService.createUser(adminRequest);
            System.out.println("Admin user created");
        } else {
            System.out.println("Admin user already exists");
        }
    }
}
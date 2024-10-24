package ITMO.ConstructionDocs.security;

import ITMO.ConstructionDocs.exceptions.CustomException;
import ITMO.ConstructionDocs.model.db.entity.User;
import ITMO.ConstructionDocs.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws CustomException {

        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new CustomException("User not found with email: " + email, HttpStatus.NOT_FOUND));

        if (user.getRole() == null) {
            throw new CustomException("User role is null", HttpStatus.BAD_REQUEST);
        }

        return user;
    }
}

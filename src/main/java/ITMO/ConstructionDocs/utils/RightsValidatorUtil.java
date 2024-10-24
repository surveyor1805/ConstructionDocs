package ITMO.ConstructionDocs.utils;

import ITMO.ConstructionDocs.exceptions.CustomException;
import ITMO.ConstructionDocs.model.db.entity.User;
import ITMO.ConstructionDocs.model.enums.Role;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;

@UtilityClass
public class RightsValidatorUtil {
    public static void validateUser(User createdBy, User currentUser) {
        if (createdBy.equals(currentUser)) {
            return;
        }
        if (currentUser.getRole() != Role.ADMIN) {
            throw new CustomException("You do not have permission (rights) for this operation", HttpStatus.FORBIDDEN);
        }
    }
}

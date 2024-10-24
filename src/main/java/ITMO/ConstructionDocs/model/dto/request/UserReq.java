package ITMO.ConstructionDocs.model.dto.request;

import ITMO.ConstructionDocs.model.enums.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserReq {
    @NotEmpty
    String email;
    String firstName;
    String lastName;
    String position;
    String phoneNumber;
    Role role;
    String password;
}

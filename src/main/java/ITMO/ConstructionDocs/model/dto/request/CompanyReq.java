package ITMO.ConstructionDocs.model.dto.request;

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
public class CompanyReq {
    @NotEmpty
    String name;
    String address;
    String description;
    String registrationNumber;
    String taxpayerIdentificationNumber;
}

package ITMO.ConstructionDocs.model.dto.request;

import ITMO.ConstructionDocs.model.enums.DesignCategory;
import ITMO.ConstructionDocs.model.enums.DocStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectDocReq {
    @NotBlank
    String fileName;
    String description;
    DocStatus docStatus;
    DesignCategory designCategory;
}

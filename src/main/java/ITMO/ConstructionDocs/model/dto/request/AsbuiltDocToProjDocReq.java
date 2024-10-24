package ITMO.ConstructionDocs.model.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AsbuiltDocToProjDocReq {
    @NotNull
    UUID asbuiltDocId;
    @NotNull
    UUID projectDocId;
}

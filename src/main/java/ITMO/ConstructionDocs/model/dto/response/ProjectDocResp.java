package ITMO.ConstructionDocs.model.dto.response;

import ITMO.ConstructionDocs.model.dto.request.ProjectDocReq;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDocResp extends ProjectDocReq {
    String fileFormat;
    Long fileSize;
    String fileAddress;
    UUID id;
}

package ITMO.ConstructionDocs.model.dto.response;

import ITMO.ConstructionDocs.model.dto.request.CompanyReq;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyResp extends CompanyReq {
    Long id;
}

package ITMO.ConstructionDocs.model.dto.request;

import lombok.Data;

@Data
public class AuthReq {
    private String email;
    private String password;
}

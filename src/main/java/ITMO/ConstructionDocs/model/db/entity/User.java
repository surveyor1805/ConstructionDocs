package ITMO.ConstructionDocs.model.db.entity;

import ITMO.ConstructionDocs.model.enums.CommonStatus;
import ITMO.ConstructionDocs.model.enums.Role;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "email")
    String email;

    @Column(name = "password")
    String password;

    @Column(name = "first_name", columnDefinition = "VARCHAR(20)")
    String firstName;

    @Column(name = "last_name", columnDefinition = "VARCHAR(20)")
    String lastName;

    @Column(name = "position")
    String position;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    Role role;

    @Column(name = "phone_number")
    String phoneNumber;

    @ManyToOne
    @JoinColumn(name = "company_id")
    @JsonBackReference("companyToUser")
    Company company;

    @OneToMany(mappedBy = "createdBy")
    @JsonManagedReference("userCreatedABD")
    List<AsbuiltDoc> asbuiltDocs;

    @OneToMany(mappedBy = "createdBy")
    @JsonManagedReference("userCreatedProjDoc")
    List<ProjectDoc> projectDocs;

    @OneToMany(mappedBy = "updatedBy")
    @JsonManagedReference("userUpdatedABD")
    List<AsbuiltDoc> updatedAsbuiltDocs;

    @OneToMany(mappedBy = "updatedBy")
    @JsonManagedReference("userUpdatedProjDoc")
    List<ProjectDoc> updatedProjectDocs;

    @OneToMany(mappedBy = "createdBy")
    @JsonManagedReference("userCreatedComm")
    List<Comment> comments;

    @OneToMany(mappedBy = "updatedBy")
    @JsonManagedReference("userUpdatedComm")
    List<Comment> updatedComments;

    @Column(name = "created_at")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @CreationTimestamp
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @UpdateTimestamp
    LocalDateTime updatedAt;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    CommonStatus status;

    @EqualsAndHashCode.Include
    public Long getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList("ROLE_" + this.role.name());
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getPassword() {
        return this.password;
    }


}

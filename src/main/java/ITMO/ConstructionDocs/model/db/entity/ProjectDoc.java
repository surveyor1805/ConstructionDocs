package ITMO.ConstructionDocs.model.db.entity;

import ITMO.ConstructionDocs.model.enums.DesignCategory;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "project_docs")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectDoc extends Doc {
    @Column(name = "category")
    DesignCategory designCategory;

    @ManyToOne
    @JoinColumn(name = "project_id")
    @JsonBackReference("projToProjDocs")
    Project project;

    @OneToMany(mappedBy = "projectDoc")
    @JsonManagedReference("projDocToComm")
    List<Comment> comments;

    @OneToMany(mappedBy = "projectDoc")
    @JsonManagedReference("projDocToABD")
    List<AsbuiltDoc> asbuiltDocs;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    @JsonBackReference("userCreatedProjDoc")
    User createdBy;

    @ManyToOne
    @JoinColumn(name = "updated_by_user_id")
    @JsonBackReference("userUpdatedProjDoc")
    User updatedBy;

}

package ITMO.ConstructionDocs.model.db.entity;

import ITMO.ConstructionDocs.model.enums.AsbuiltCategory;
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
@Table(name = "asbuilt_docs")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AsbuiltDoc extends Doc {

    @Column(name = "category")
    AsbuiltCategory asbuiltCategory;

    @ManyToOne
    @JoinColumn(name = "project_doc_id")
    @JsonBackReference("projDocToABD")
    ProjectDoc projectDoc;

    @ManyToOne
    @JoinColumn(name = "company_id")
    @JsonBackReference("companyToABD")
    Company company;

    @OneToMany(mappedBy = "asbuiltDoc")
    @JsonManagedReference("asbuiltDocToComm")
    List<Comment> comments;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    @JsonBackReference("userCreatedABD")
    User createdBy;

    @ManyToOne
    @JoinColumn(name = "updated_by_user_id")
    @JsonBackReference("userUpdatedABD")
    User updatedBy;
}

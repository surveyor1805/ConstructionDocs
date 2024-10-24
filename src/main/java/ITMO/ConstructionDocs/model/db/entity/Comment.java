package ITMO.ConstructionDocs.model.db.entity;

import ITMO.ConstructionDocs.model.enums.CommonStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "comments")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "title")
    String title;

    @Column(name = "text")
    String text;

    @ManyToOne
    @JoinColumn(name = "asbuilt_doc_id")
    @JsonBackReference("asbuiltDocToComm")
    AsbuiltDoc asbuiltDoc;

    @ManyToOne
    @JoinColumn(name = "project_doc_id")
    @JsonBackReference("projDocToComm")
    ProjectDoc projectDoc;

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

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    @JsonBackReference("userCreatedComm")
    User createdBy;

    @ManyToOne
    @JoinColumn(name = "updated_by_user_id")
    @JsonBackReference("userUpdatedComm")
    User updatedBy;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    CommonStatus status;
}

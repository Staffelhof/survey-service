package ru.survey.survey_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "surveys", indexes = {
        @Index(name = "idx_surveys_public_id", columnList = "public_id", unique = true),
        @Index(name = "idx_surveys_owner_id", columnList = "owner_id")
})
public class Survey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, updatable = false)
    private UUID publicId = UUID.randomUUID();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false, foreignKey = @ForeignKey(name = "fk_surveys_owner"))
    private User owner;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false, length = 1000)
    private String description = "";

    @Column(name = "is_published", nullable = false)
    private boolean published = false;

    @Column(name = "is_active", nullable = false)
    private boolean active = false;

    @Column(name = "is_anonymous", nullable = false)
    private boolean anonymous = true;

    @Column(name = "single_submission", nullable = false)
    private boolean singleSubmission = false;

    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<Question> questions = new ArrayList<>();

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }
}


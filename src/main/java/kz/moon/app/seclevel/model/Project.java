package kz.moon.app.seclevel.model;

import jakarta.persistence.*;
import kz.moon.app.seclevel.domain.User;
import lombok.*;
import jakarta.persistence.Id;


import java.time.LocalDate;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "ml-project")
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // <= Важно
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // <= включаем только id в hashCode
    private Long id;

    private String name; // краткое название
    private String description; // краткая характеристика
    @Column(nullable = false, updatable = false)
    private LocalDate createdDate;

    private String industry; // к какой отрасли относится

    @ManyToOne
    private User createdBy;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private Set<ProjectUserAssignment> assignments;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private Set<Classifier> classifiers;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private Set<Image> images;
}

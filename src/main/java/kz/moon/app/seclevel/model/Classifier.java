package kz.moon.app.seclevel.model;

import jakarta.persistence.*;
import kz.moon.app.seclevel.domain.User;
import lombok.*;
import jakarta.persistence.Id;


import java.util.Set;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "classifier")
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // <= Важно
public class Classifier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // <= включаем только id в hashCode
    private Long id;

    private String name;
    private String description;

    @ManyToOne
    private Project project;

    @OneToMany(mappedBy = "classifier", cascade = CascadeType.ALL)
    private Set<ClassifierCategory> categories;

    @ManyToOne
    private User createdBy;

}

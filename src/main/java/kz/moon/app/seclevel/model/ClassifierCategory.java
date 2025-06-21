package kz.moon.app.seclevel.model;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "classifier_category")
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // <= Важно
public class ClassifierCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // <= включаем только id в hashCode
    private Long id;

    private String name; // например "dog", "car", "tumor"

    @ManyToOne
    private Classifier classifier;

    private String instructions; // описание для пользователей (например, как размечать)
}

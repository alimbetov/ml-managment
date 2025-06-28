package kz.moon.app.seclevel.model;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "image_annotation_data")
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // <= Важно
public class ImageAnnotation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // <= включаем только id в hashCode
    private Long id;

    @ManyToOne
    private ImageData image;

    @ManyToOne
    private ClassifierCategory category;

    private String annotationJson; // JSON с box, polygon и т.п.

    private boolean validated;
    private Instant createdAt;
    private Instant validatedAt;
}

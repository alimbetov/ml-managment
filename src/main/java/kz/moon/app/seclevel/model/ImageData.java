package kz.moon.app.seclevel.model;

import jakarta.persistence.*;
import kz.moon.app.seclevel.domain.User;
import kz.moon.app.seclevel.repository.ImageStatus;
import lombok.*;
import jakarta.persistence.Id;


import java.time.Instant;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "image_data")
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // <= Важно
public class ImageData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // <= включаем только id в hashCode
    private Long id;

    private String filename;
    private String fileHash;

    @ManyToOne
    private Project project;

    @ManyToOne
    private User uploadedBy;

    private Instant uploadDate;

    @Enumerated(EnumType.STRING)
    private ImageStatus status; // Статус: загружен, размечен, проверен


    // === Привязка к оригинальному изображению ===
    @ManyToOne
    @JoinColumn(name = "parent_image_id")
    private ImageData parentImage;

    private String parentFilename;

    // === Привязка к категории ===
    @ManyToOne
    private ClassifierCategory classifierCategory;

    @OneToMany(mappedBy = "image", cascade = CascadeType.ALL)
    private Set<ImageAnnotation> annotations;
}

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
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // <= включаем только id в hashCode
    private Long id;

    private String filename; // путь/имя файла
    private String fileHash; // хэш файла (чтобы избежать дублей)

    @ManyToOne
    private Project project;

    @ManyToOne
    private User uploadedBy;

    private Instant uploadDate;

    @Enumerated(EnumType.STRING)
    private ImageStatus status; // Статус: загружен, размечен, проверен

    @OneToMany(mappedBy = "image", cascade = CascadeType.ALL)
    private Set<ImageAnnotation> annotations;
}

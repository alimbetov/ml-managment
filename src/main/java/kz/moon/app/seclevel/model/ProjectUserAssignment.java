package kz.moon.app.seclevel.model;

import jakarta.persistence.*;
import kz.moon.app.seclevel.domain.RolesEnum;
import kz.moon.app.seclevel.domain.User;
import lombok.*;
import jakarta.persistence.Id;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "ml_project_user_assignment")
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // <= Важно
public class ProjectUserAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // <= включаем только id в hashCode
    private Long id;

    @ManyToOne
    private Project project;

    @ManyToOne
    private User user;

    @Enumerated(EnumType.STRING)
    private RolesEnum role;

}

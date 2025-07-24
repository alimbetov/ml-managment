package kz.moon.app.seclevel.repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum ImageStatus {
    UPLOADED,
    IN_PROGRESS,
    MARKED,
    REVIEWED,
    APPROVED,
    REJECTED;

    /**
     * Получает перечисление ImageStatus по его имени (безопасно).
     *
     * @param name имя статуса (например, "UPLOADED")
     * @return соответствующее перечисление
     * @throws IllegalArgumentException если имя некорректное
     */
    public static Optional<ImageStatus> fromStringSafe(String name) {
        for (ImageStatus status : ImageStatus.values()) {
            if (status.name().equalsIgnoreCase(name)) {
                return Optional.of(status);
            }
        }
        return Optional.empty();
    }

    public static List<String> allNamesList() {
        return Arrays.stream(ImageStatus.values())
                .map(Enum::name)
                .toList(); // Java 16+, для более старых — .collect(Collectors.toList())
    }

}

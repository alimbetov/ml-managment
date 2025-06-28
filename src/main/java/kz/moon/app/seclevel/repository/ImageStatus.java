package kz.moon.app.seclevel.repository;

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

}

package kz.moon.app.seclevel.dto;

public record ProjectStatRow(
        String project,
        String status,
        String classifierName,
        String categoryName,
        long count
) {}
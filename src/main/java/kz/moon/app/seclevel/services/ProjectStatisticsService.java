package kz.moon.app.seclevel.services;



import kz.moon.app.seclevel.dto.ProjectStatRow;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectStatisticsService {

    private final JdbcTemplate jdbcTemplate;

    public List<ProjectStatRow> getProjectStats(Long projectId, String status) {
        String sql = """
                SELECT 
                    pn.name AS project, 
                    idat.status, 
                    cl.name AS classname, 
                    cla.name AS categoryname, 
                    COUNT(*) AS n
                FROM public.image_data AS idat
                JOIN public."ml-project" pn ON pn.id = idat.project_id
                LEFT JOIN public.classifier_category cla ON cla.id = idat.classifier_category_id
                LEFT JOIN public.classifier cl ON cl.id = cla.classifier_id
                WHERE (:projectId IS NULL OR pn.id = :projectId)
                  AND (:status IS NULL OR idat.status = :status)
                GROUP BY  pn.name, idat.status, cl.name, cla.name
                ORDER BY pn.name, idat.status, cl.name, cla.name
                """;

        var params = new MapSqlParameterSource();
        params.addValue("projectId", projectId);
        params.addValue("status", status);

        NamedParameterJdbcTemplate namedJdbc = new NamedParameterJdbcTemplate(jdbcTemplate);
        return namedJdbc.query(sql, params, (rs, rowNum) ->
                new ProjectStatRow(
                        rs.getString("project"),
                        rs.getString("status"),
                        rs.getString("classname"),
                        rs.getString("categoryname"),
                        rs.getLong("n")
                )
        );
    }
}

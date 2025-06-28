package kz.moon.app.seclevel.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import kz.moon.app.seclevel.dto.ProjectStatRow;
import kz.moon.app.seclevel.model.Project;
import kz.moon.app.seclevel.repository.ImageStatus;
import kz.moon.app.seclevel.services.ProjectService;
import kz.moon.app.seclevel.services.ProjectStatisticsService;
import org.springframework.security.access.annotation.Secured;

import java.util.List;

@Secured({"ROLE_USER", "ROLE_MANAGER", "ROLE_ADMIN"})
@Route("project-statistics")
@PageTitle("Project Statistics")
@PermitAll
public class ProjectStatisticsView extends VerticalLayout {

    private final Div chartContainer = new Div();

    private final ProjectStatisticsService statisticsService;
    private final ProjectService projectService;

    private final ComboBox<Project> projectFilter = new ComboBox<>("Project");
    private final ComboBox<ImageStatus> statusFilter = new ComboBox<>("Status");

    private final Button loadButton = new Button("Load Statistics");

    private final Grid<ProjectStatRow> statGrid = new Grid<>(ProjectStatRow.class, false);

    public ProjectStatisticsView(ProjectStatisticsService statisticsService, ProjectService projectService) {
        this.statisticsService = statisticsService;
        this.projectService = projectService;

        projectFilter.setItems(projectService.findAllProjects());
        projectFilter.setItemLabelGenerator(Project::getName);
        projectFilter.setPlaceholder("Select project");

        statusFilter.setItems(ImageStatus.values());
        statusFilter.setPlaceholder("Select status");

        loadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loadButton.addClickListener(event -> loadStatistics());

        configureGrid();

        chartContainer.setId("chartDiv");
        chartContainer.getStyle().set("width", "800px").set("height", "500px");

        add(new kz.moon.app.base.ui.component.ViewToolbar("Filters",
                kz.moon.app.base.ui.component.ViewToolbar.group(projectFilter, statusFilter, loadButton)));
        add(statGrid);

        add(new Span("Category Count Bar Chart (with %)"));
        add(chartContainer);

        setSizeFull();
    }

    private void configureGrid() {
        statGrid.addColumn(ProjectStatRow::project).setHeader("Project").setAutoWidth(true);
        statGrid.addColumn(ProjectStatRow::status).setHeader("Status").setAutoWidth(true);
        statGrid.addColumn(ProjectStatRow::classifierName).setHeader("Classifier").setAutoWidth(true);
        statGrid.addColumn(ProjectStatRow::categoryName).setHeader("Category").setAutoWidth(true);
        statGrid.addColumn(ProjectStatRow::count).setHeader("Count").setAutoWidth(true);

        statGrid.setSizeFull();
    }

    private void loadStatistics() {
        Long projectId = (projectFilter.getValue() != null) ? projectFilter.getValue().getId() : null;
        String status = (statusFilter.getValue() != null) ? statusFilter.getValue().name() : null;

        List<ProjectStatRow> stats = statisticsService.getProjectStats(projectId, status);
        statGrid.setItems(stats);

        String labelsJson = "[" + stats.stream()
                .map(row -> "\"" + row.categoryName() + "\"")
                .reduce((a, b) -> a + "," + b)
                .orElse("") + "]";

        String dataJson = "[" + stats.stream()
                .map(row -> String.valueOf(row.count()))
                .reduce((a, b) -> a + "," + b)
                .orElse("") + "]";

        chartContainer.getElement().executeJs("""
        if (!window.chartjsLoaded) {
            var script = document.createElement('script');
            script.src = 'https://cdn.jsdelivr.net/npm/chart.js';
            script.onload = function() { 
                window.chartjsLoaded = true; 
                console.log('Chart.js loaded');
                window.renderProjectStatisticsChart($0, $1); 
            }
            document.head.appendChild(script);
        } else {
            window.renderProjectStatisticsChart($0, $1);
        }

        // function definition
        if (!window.renderProjectStatisticsChart) {
            window.renderProjectStatisticsChart = function(labelsJson, dataJson) {
                const ctxId = 'chartCanvas';
                let canvas = document.getElementById(ctxId);
                if (!canvas) {
                    canvas = document.createElement('canvas');
                    canvas.id = ctxId;
                    document.getElementById('chartDiv').innerHTML = '';
                    document.getElementById('chartDiv').appendChild(canvas);
                }
                const ctx = canvas.getContext('2d');
                if (window.myChart) {
                    window.myChart.destroy();
                }
                window.myChart = new Chart(ctx, {
                    type: 'bar',
                    data: {
                        labels: JSON.parse(labelsJson),
                        datasets: [{
                            label: 'Category Count',
                            data: JSON.parse(dataJson),
                            backgroundColor: 'rgba(54, 162, 235, 0.7)'
                        }]
                    },
                    options: {
                        responsive: true,
                        scales: {
                            y: {
                                beginAtZero: true,
                                ticks: {
                                    precision: 0
                                }
                            }
                        },
                        plugins: {
                            legend: { display: false },
                            title: {
                                display: true,
                                text: 'Category Count by Project'
                            },
                            tooltip: {
                                callbacks: {
                                    label: function(context) {
                                        const value = context.parsed.y;
                                        const total = context.chart.data.datasets[0].data.reduce((sum, val) => sum + val, 0);
                                        const percentage = total > 0 ? (value / total * 100).toFixed(1) : 0;
                                        return `${context.label}: ${value} (${percentage}%)`;
                                    }
                                }
                            }
                        }
                    }
                });
            };
        }
    """, labelsJson, dataJson);
    }
}

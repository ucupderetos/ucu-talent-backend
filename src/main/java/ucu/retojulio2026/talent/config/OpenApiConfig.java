package ucu.retojulio2026.talent.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    private static final String ACTUATOR_TAG = "Actuator";
    private static final String HEALTH_TAG = "Health";

    private static final List<String> TAG_ORDER = List.of(
            "Usuarios",
            "Alumnos",
            "Empresas",
            "Educacion",
            "Experiencia laboral",
            "Autenticacion",
            "Puestos",
            HEALTH_TAG
    );

    @Bean
    public OpenAPI talentOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Talent API")
                        .description("API del Portal Laboral.")
                        .version("v0.1"));
    }

    @Bean
    public OpenApiCustomizer tagOrderCustomizer() {
        return openApi -> {
            renameActuatorTag(openApi);
            if (openApi.getTags() == null) {
                return;
            }
            Map<String, Tag> byName = new LinkedHashMap<>();
            for (Tag tag : openApi.getTags()) {
                Tag existing = byName.get(tag.getName());
                if (existing == null || (existing.getDescription() == null && tag.getDescription() != null)) {
                    byName.put(tag.getName(), tag);
                }
            }
            List<Tag> ordenados = new ArrayList<>(byName.values());
            ordenados.sort(Comparator.comparingInt(t -> {
                int i = TAG_ORDER.indexOf(t.getName());
                return i == -1 ? Integer.MAX_VALUE : i;
            }));
            openApi.setTags(ordenados);
        };
    }

    private void renameActuatorTag(OpenAPI openApi) {
        if (openApi.getTags() != null) {
            for (Tag tag : openApi.getTags()) {
                if (ACTUATOR_TAG.equals(tag.getName())) {
                    tag.setName(HEALTH_TAG);
                    tag.setDescription("Estado de salud de la aplicacion (Actuator)");
                }
            }
        }
        if (openApi.getPaths() == null) {
            return;
        }
        for (PathItem pathItem : openApi.getPaths().values()) {
            for (Operation operation : pathItem.readOperations()) {
                if (operation.getTags() != null) {
                    operation.setTags(operation.getTags().stream()
                            .map(t -> ACTUATOR_TAG.equals(t) ? HEALTH_TAG : t)
                            .toList());
                }
            }
        }
    }
}

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

    // El tag "Actuator" lo genera solo springdoc (springdoc.show-actuator=true), no un @Tag nuestro.
    // Lo renombramos a "Health" aca porque no hay forma de anotarlo directamente.
    private static final String ACTUATOR_TAG = "Actuator";
    private static final String HEALTH_TAG = "Health";

    private static final List<String> TAG_ORDER = List.of(
            "Usuarios",             // 1 - User
            "Alumnos",              // 2 - StudentProfile
            "Empresas",             // 3 - Company
            "Educacion",            // 4 - Education
            "Experiencia laboral",  // 5 - WorkExperience
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

    // springdoc arma la lista de tags juntando los @Tag de cada controller (con descripcion) con los
    // globales, dejando duplicados y en orden de escaneo. Este customizer los deduplica (quedandose con
    // la version que tenga descripcion) y los ordena segun TAG_ORDER, que es lo que respeta Swagger UI.
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

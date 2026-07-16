package ucu.retojulio2026.talent.config;

import io.swagger.v3.oas.models.OpenAPI;
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


    private static final List<String> TAG_ORDER = List.of(
            "Usuarios",             // 1 - User
            "Alumnos",              // 2 - StudentProfile
            "Empresas",             // 3 - Company
            "Educacion",            // 4 - Education
            "Experiencia laboral",  // 5 - WorkExperience
            "Autenticacion"
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
}

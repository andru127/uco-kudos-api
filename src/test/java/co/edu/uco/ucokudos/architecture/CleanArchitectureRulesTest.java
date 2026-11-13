package co.edu.uco.ucokudos.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Hace cumplir la Regla de Dependencia de Clean Architecture en el pipeline:
 * dominio y servicios no pueden depender de frameworks ni de la capa de infraestructura.
 */
class CleanArchitectureRulesTest {

    private static final Path BASE = Path.of("src/main/java/co/edu/uco/ucokudos");

    @Test
    void elDominioNoDependeDeNingunaOtraCapaNiFramework() throws IOException {
        assertThat(violations(BASE.resolve("domain"),
                List.of("import org.springframework", "import jakarta.persistence", ".services.", ".infrastructure.")))
                .isEmpty();
    }

    @Test
    void losCasosDeUsoNoDependenDeSpringNiDeInfraestructura() throws IOException {
        assertThat(violations(BASE.resolve("services"),
                List.of("import org.springframework", "import jakarta.persistence", ".infrastructure.")))
                .isEmpty();
    }

    private static List<String> violations(Path layer, List<String> forbidden) throws IOException {
        try (Stream<Path> files = Files.walk(layer)) {
            return files.filter(path -> path.toString().endsWith(".java"))
                    .flatMap(path -> forbiddenImports(path, forbidden))
                    .toList();
        }
    }

    private static Stream<String> forbiddenImports(Path file, List<String> forbidden) {
        try {
            return Files.readAllLines(file).stream()
                    .filter(line -> line.startsWith("import "))
                    .filter(line -> forbidden.stream().anyMatch(line::contains))
                    .map(line -> file.getFileName() + " -> " + line);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}

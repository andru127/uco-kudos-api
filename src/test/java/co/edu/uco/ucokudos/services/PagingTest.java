package co.edu.uco.ucokudos.services;

import co.edu.uco.ucokudos.services.input.PageQuery;
import co.edu.uco.ucokudos.services.input.PageResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PagingTest {

    @Test
    void acotaPaginaYTamanoInvalidos() {
        var query = new PageQuery(-3, 1000);

        assertThat(query.page()).isZero();
        assertThat(query.size()).isEqualTo(PageQuery.MAX_SIZE);
        assertThat(new PageQuery(0, 0).size()).isEqualTo(1);
    }

    @Test
    void indicaSiHayMasPaginas() {
        assertThat(new PageResult<>(List.of("a", "b"), 0, 2, 5).hasNext()).isTrue();
        assertThat(new PageResult<>(List.of("e"), 2, 2, 5).hasNext()).isFalse();
    }
}

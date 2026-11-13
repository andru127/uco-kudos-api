package co.edu.uco.ucokudos.infrastructure.input.rest.dto;

import co.edu.uco.ucokudos.services.input.PageResult;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(List<T> items, int page, int size, long totalItems, boolean hasNext) {

    public static <D, T> PageResponse<T> from(PageResult<D> result, Function<D, T> mapper) {
        return new PageResponse<>(result.items().stream().map(mapper).toList(), result.page(), result.size(),
                result.totalItems(), result.hasNext());
    }
}

package co.edu.uco.ucokudos.services.input;

import java.util.List;

public record PageResult<T>(List<T> items, int page, int size, long totalItems) {

    public PageResult {
        items = List.copyOf(items);
    }

    public boolean hasNext() {
        return (long) (page + 1) * size < totalItems;
    }
}

package co.edu.uco.ucokudos.services.input;

public record PageQuery(int page, int size) {

    public static final int MAX_SIZE = 50;

    public PageQuery {
        page = Math.max(page, 0);
        size = Math.min(Math.max(size, 1), MAX_SIZE);
    }
}

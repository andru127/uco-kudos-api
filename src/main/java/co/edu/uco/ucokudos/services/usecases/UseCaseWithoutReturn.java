package co.edu.uco.ucokudos.services.usecases;

public interface UseCaseWithoutReturn<I> {
    void execute(I data);
}

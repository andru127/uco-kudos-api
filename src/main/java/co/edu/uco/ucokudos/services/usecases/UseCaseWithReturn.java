package co.edu.uco.ucokudos.services.usecases;

public interface UseCaseWithReturn<I, O> {
    O execute(I data);
}

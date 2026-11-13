package co.edu.uco.ucokudos.services.usecases.kudo;

import co.edu.uco.ucokudos.domain.exception.ResourceNotFoundException;
import co.edu.uco.ucokudos.domain.kudo.KudoDomain;
import co.edu.uco.ucokudos.services.output.ports.repositories.KudoRepository;
import co.edu.uco.ucokudos.services.usecases.UseCaseWithReturn;

import java.util.UUID;

public class GetKudoInteractor implements UseCaseWithReturn<UUID, KudoDomain> {

    private final KudoRepository kudoRepository;

    public GetKudoInteractor(KudoRepository kudoRepository) {
        this.kudoRepository = kudoRepository;
    }

    @Override
    public KudoDomain execute(UUID kudoId) {
        return kudoRepository.findById(kudoId).orElseThrow(() -> new ResourceNotFoundException("Kudo", kudoId));
    }
}

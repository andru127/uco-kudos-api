package co.edu.uco.ucokudos.services.usecases.kudo;

import co.edu.uco.ucokudos.domain.exception.ResourceNotFoundException;
import co.edu.uco.ucokudos.domain.kudo.AttachmentDomain;
import co.edu.uco.ucokudos.services.input.AttachmentQuery;
import co.edu.uco.ucokudos.services.output.ports.repositories.KudoRepository;
import co.edu.uco.ucokudos.services.usecases.UseCaseWithReturn;

public class GetAttachmentInteractor implements UseCaseWithReturn<AttachmentQuery, AttachmentDomain> {

    private final KudoRepository kudoRepository;

    public GetAttachmentInteractor(KudoRepository kudoRepository) {
        this.kudoRepository = kudoRepository;
    }

    @Override
    public AttachmentDomain execute(AttachmentQuery query) {
        return kudoRepository.findAttachment(query.kudoId(), query.attachmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Adjunto", query.attachmentId()));
    }
}

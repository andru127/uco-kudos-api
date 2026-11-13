package co.edu.uco.ucokudos.services.usecases.kudo;

import co.edu.uco.ucokudos.domain.kudo.KudoDomain;
import co.edu.uco.ucokudos.services.input.PageQuery;
import co.edu.uco.ucokudos.services.input.PageResult;
import co.edu.uco.ucokudos.services.output.ports.repositories.KudoRepository;
import co.edu.uco.ucokudos.services.usecases.UseCaseWithReturn;

public class ListKudoFeedInteractor implements UseCaseWithReturn<PageQuery, PageResult<KudoDomain>> {

    private final KudoRepository kudoRepository;

    public ListKudoFeedInteractor(KudoRepository kudoRepository) {
        this.kudoRepository = kudoRepository;
    }

    @Override
    public PageResult<KudoDomain> execute(PageQuery query) {
        return kudoRepository.findFeed(query);
    }
}

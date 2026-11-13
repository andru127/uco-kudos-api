package co.edu.uco.ucokudos.infrastructure.input.rest;

import co.edu.uco.ucokudos.infrastructure.input.rest.dto.AuditEventResponse;
import co.edu.uco.ucokudos.services.input.AuditQuery;
import co.edu.uco.ucokudos.services.usecases.audit.ListAuditInteractor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final ListAuditInteractor listAudit;

    public AuditController(ListAuditInteractor listAudit) {
        this.listAudit = listAudit;
    }

    @GetMapping
    public List<AuditEventResponse> recent(@AuthenticationPrincipal Jwt jwt,
                                           @RequestParam(required = false) UUID resourceId) {
        return listAudit.execute(new AuditQuery(CurrentUser.id(jwt), resourceId)).stream()
                .map(AuditEventResponse::from).toList();
    }
}

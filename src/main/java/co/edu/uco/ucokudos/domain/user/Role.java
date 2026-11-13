package co.edu.uco.ucokudos.domain.user;

import java.util.EnumSet;
import java.util.Set;

/**
 * Roles institucionales. Cada usuario tiene exactamente un rol activo (restricción de exclusividad),
 * por eso el rol es un único valor y no una colección.
 */
public enum Role {
    ADMINISTRADOR(6),
    RECTOR(5),
    COORDINADOR_ACADEMICO(4),
    COORDINADOR_FACULTAD(3),
    COORDINADOR_PROGRAMA(2),
    DOCENTE(1),
    ESTUDIANTE(1);

    private static final Set<Role> CATEGORY_MANAGERS = EnumSet.of(ADMINISTRADOR, RECTOR, COORDINADOR_ACADEMICO);
    private static final Set<Role> MODERATORS_WITH_VETO = EnumSet.of(RECTOR, COORDINADOR_ACADEMICO);

    private final int hierarchyLevel;

    Role(int hierarchyLevel) {
        this.hierarchyLevel = hierarchyLevel;
    }

    public int hierarchyLevel() {
        return hierarchyLevel;
    }

    public boolean canManageCategories() {
        return CATEGORY_MANAGERS.contains(this);
    }

    public boolean canVetoContent() {
        return MODERATORS_WITH_VETO.contains(this);
    }

    public boolean canReadAudit() {
        return CATEGORY_MANAGERS.contains(this);
    }
}

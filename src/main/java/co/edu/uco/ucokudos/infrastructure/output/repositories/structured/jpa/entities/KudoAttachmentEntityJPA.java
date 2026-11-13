package co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "kudo_attachments")
public class KudoAttachmentEntityJPA {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kudo_id")
    private KudoEntityJPA kudo;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(nullable = false)
    private String sha256;

    @Column(nullable = false)
    private byte[] content;

    protected KudoAttachmentEntityJPA() {
        // Requerido por JPA
    }

    public KudoAttachmentEntityJPA(UUID id, KudoEntityJPA kudo, String fileName, String contentType,
                                   long sizeBytes, String sha256, byte[] content) {
        this.id = id;
        this.kudo = kudo;
        this.fileName = fileName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.sha256 = sha256;
        this.content = content;
    }

    public UUID getId() { return id; }
    public KudoEntityJPA getKudo() { return kudo; }
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public long getSizeBytes() { return sizeBytes; }
    public String getSha256() { return sha256; }
    public byte[] getContent() { return content; }
}

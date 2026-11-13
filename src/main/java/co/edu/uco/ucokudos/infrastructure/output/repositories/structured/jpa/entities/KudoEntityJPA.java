package co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "kudos")
public class KudoEntityJPA {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id")
    private UserEntityJPA author;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id")
    private UserEntityJPA recipient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private CategoryEntityJPA category;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;

    @OneToMany(mappedBy = "kudo", cascade = CascadeType.PERSIST)
    private List<KudoAttachmentEntityJPA> attachments = new ArrayList<>();

    protected KudoEntityJPA() {
        // Requerido por JPA
    }

    public KudoEntityJPA(UUID id, UserEntityJPA author, UserEntityJPA recipient, CategoryEntityJPA category,
                         String content, Instant publishedAt) {
        this.id = id;
        this.author = author;
        this.recipient = recipient;
        this.category = category;
        this.content = content;
        this.publishedAt = publishedAt;
    }

    public void addAttachment(KudoAttachmentEntityJPA attachment) {
        attachments.add(attachment);
    }

    public UUID getId() { return id; }
    public UserEntityJPA getAuthor() { return author; }
    public UserEntityJPA getRecipient() { return recipient; }
    public CategoryEntityJPA getCategory() { return category; }
    public String getContent() { return content; }
    public Instant getPublishedAt() { return publishedAt; }
}

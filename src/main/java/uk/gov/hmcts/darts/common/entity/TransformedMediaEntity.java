package uk.gov.hmcts.darts.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.darts.audio.entity.MediaRequestEntity;
import uk.gov.hmcts.darts.common.entity.base.CreatedModifiedBaseEntity;

import java.time.OffsetDateTime;

@Entity
@Table(name = "transformed_media")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class TransformedMediaEntity extends CreatedModifiedBaseEntity {
    @Id
    @Column(name = "trm_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trm_gen")
    @SequenceGenerator(name = "trm_gen", sequenceName = "trm_seq", allocationSize = 1)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "mer_id", foreignKey = @ForeignKey(name = "trm_media_request_fk"), nullable = false)
    private MediaRequestEntity mediaRequest;

    @Column(name = "last_accessed_ts")
    private OffsetDateTime lastAccessed;

    @Column(name = "expiry_ts")
    private OffsetDateTime expiryTime;

    @Column(name = "output_filename")
    private String outputFilename;

    @Column(name = "output_filesize")
    private Integer outputFilesize;

    @Column(name = "output_format")
    private String outputFormat;

    @Column(name = "checksum")
    private String checksum;

    @Column(name = "start_ts", nullable = false)
    private OffsetDateTime startTime;

    @Column(name = "end_ts", nullable = false)
    private OffsetDateTime endTime;


}
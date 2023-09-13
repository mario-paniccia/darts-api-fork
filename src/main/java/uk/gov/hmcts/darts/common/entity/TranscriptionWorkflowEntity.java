package uk.gov.hmcts.darts.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.darts.common.entity.base.CreatedModifiedBaseEntity;
import uk.gov.hmcts.darts.common.enums.TranscriptionWorkflowStageEnum;

@Entity
@Table(name = "transcription_workflow")
@Getter
@Setter
public class TranscriptionWorkflowEntity extends CreatedModifiedBaseEntity {

    @Id
    @Column(name = "trw_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trw_gen")
    @SequenceGenerator(name = "trw_gen", sequenceName = "trw_seq", allocationSize = 1)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tra_id", nullable = false)
    private TranscriptionEntity transcription;

    @Column(name = "workflow_stage", nullable = false)
    @Enumerated(EnumType.STRING)
    private TranscriptionWorkflowStageEnum workflowStage;

    @Column(name = "workflow_comment")
    private String workflowComment;

}
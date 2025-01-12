package uk.gov.hmcts.darts.transcriptions.component.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.darts.transcriptions.component.TranscriberTranscriptsQuery;
import uk.gov.hmcts.darts.transcriptions.model.TranscriberViewSummary;

import java.util.List;

import static uk.gov.hmcts.darts.common.enums.SecurityRoleEnum.TRANSCRIBER;
import static uk.gov.hmcts.darts.transcriptions.enums.TranscriptionStatusEnum.WITH_TRANSCRIBER;

@Component
@RequiredArgsConstructor
public class TranscriberTranscriptsQueryImpl implements TranscriberTranscriptsQuery {

    private static final String USR_ID = "usr_id";
    private static final String ROL_ID = "rol_id";
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final TranscriberViewSummaryRowMapper transcriberViewSummaryRowMapper;

    @Override
    public List<TranscriberViewSummary> getTranscriptRequests(Integer userId) {
        return jdbcTemplate.query(
            """
                -- Transcript Requests (transcriber-view?assigned=false)
                SELECT
                    tra.tra_id as transcription_id,
                    tra.cas_id as case_id,
                    cas.case_number,
                    cth.courthouse_name,
                    hea.hearing_date,
                    trt.description             as transcription_type,
                    trs.display_name            as status,
                    tru.description             as urgency,
                    requested_trw.workflow_ts   as requested_ts,
                    approved_trw.workflow_ts    as state_change_ts,
                    tra.is_manual_transcription as is_manual
                FROM
                    darts.transcription tra
                INNER JOIN
                    darts.court_case cas
                ON
                    tra.cas_id = cas.cas_id
                INNER JOIN
                    darts.courthouse cth
                ON
                    cas.cth_id = cth.cth_id
                AND cth.cth_id IN
                    (   SELECT
                            DISTINCT(grc.cth_id)
                        FROM
                            darts.user_account usr
                        INNER JOIN
                            darts.security_group_user_account_ae gua
                        ON
                            usr.usr_id = gua.usr_id
                        INNER JOIN
                            darts.security_group grp
                        ON
                            gua.grp_id = grp.grp_id
                        INNER JOIN
                            darts.security_group_courthouse_ae grc
                        ON
                            grp.grp_id = grc.grp_id
                        WHERE
                            usr.usr_id = :usr_id
                        AND grp.rol_id = :rol_id)
                INNER JOIN
                    darts.hearing hea
                ON
                    tra.hea_id = hea.hea_id
                INNER JOIN
                    darts.transcription_type trt
                ON
                    tra.trt_id = trt.trt_id
                INNER JOIN
                    darts.transcription_status trs
                ON
                    tra.trs_id = trs.trs_id
                LEFT JOIN
                    darts.transcription_urgency tru
                ON
                    tra.tru_id = tru.tru_id
                INNER JOIN
                    darts.transcription_workflow requested_trw
                ON
                    tra.tra_id = requested_trw.tra_id
                AND requested_trw.trs_id = 1
                INNER JOIN
                    darts.transcription_workflow approved_trw
                ON
                    tra.tra_id = approved_trw.tra_id
                AND approved_trw.trs_id = 3
                WHERE
                    tra.trs_id = 3
                ORDER BY
                    transcription_id desc
                """,
            new MapSqlParameterSource(USR_ID, userId)
                .addValue(ROL_ID, TRANSCRIBER.getId()),
            transcriberViewSummaryRowMapper
        );
    }

    @Override
    public List<TranscriberViewSummary> getTranscriberTranscriptions(Integer userId) {
        return jdbcTemplate.query(
            """
                -- Your work > To do (transcriber-view?assigned=true)
                SELECT
                    tra.tra_id as transcription_id,
                    tra.cas_id as case_id,
                    cas.case_number,
                    cth.courthouse_name,
                    hea.hearing_date,
                    trt.description                  as transcription_type,
                    trs.display_name                 as status,
                    tru.description                  as urgency,
                    requested_trw.workflow_ts        as requested_ts,
                    with_transcriber_trw.workflow_ts as state_change_ts,
                    tra.is_manual_transcription      as is_manual
                FROM
                    darts.transcription tra
                INNER JOIN
                    darts.court_case cas
                ON
                    tra.cas_id = cas.cas_id
                INNER JOIN
                    darts.courthouse cth
                ON
                    cas.cth_id = cth.cth_id
                AND cth.cth_id IN
                    (   SELECT
                            DISTINCT(grc.cth_id)
                        FROM
                            darts.user_account usr
                        INNER JOIN
                            darts.security_group_user_account_ae gua
                        ON
                            usr.usr_id = gua.usr_id
                        INNER JOIN
                            darts.security_group grp
                        ON
                            gua.grp_id = grp.grp_id
                        INNER JOIN
                            darts.security_group_courthouse_ae grc
                        ON
                            grp.grp_id = grc.grp_id
                        WHERE
                            usr.usr_id = :usr_id
                        AND grp.rol_id = :rol_id)
                INNER JOIN
                    darts.hearing hea
                ON
                    tra.hea_id = hea.hea_id
                INNER JOIN
                    darts.transcription_type trt
                ON
                    tra.trt_id = trt.trt_id
                INNER JOIN
                    darts.transcription_status trs
                ON
                    tra.trs_id = trs.trs_id
                LEFT JOIN
                    darts.transcription_urgency tru
                ON
                    tra.tru_id = tru.tru_id
                INNER JOIN
                    darts.transcription_workflow requested_trw
                ON
                    tra.tra_id = requested_trw.tra_id
                AND requested_trw.trs_id = 1
                INNER JOIN
                    darts.transcription_workflow with_transcriber_trw
                ON
                    tra.tra_id = with_transcriber_trw.tra_id
                AND with_transcriber_trw.trs_id = 5
                AND with_transcriber_trw.workflow_actor = :usr_id
                WHERE
                    tra.trs_id = 5

                UNION

                -- Your work > Completed today (transcriber-view?assigned=true)
                SELECT
                    tra.tra_id as transcription_id,
                    tra.cas_id as case_id,
                    cas.case_number,
                    cth.courthouse_name,
                    hea.hearing_date,
                    trt.description             as transcription_type,
                    trs.display_name            as status,
                    tru.description             as urgency,
                    requested_trw.workflow_ts   as requested_ts,
                    complete_trw.workflow_ts    as state_change_ts,
                    tra.is_manual_transcription as is_manual
                FROM
                    darts.transcription tra
                INNER JOIN
                    darts.court_case cas
                ON
                    tra.cas_id = cas.cas_id
                INNER JOIN
                    darts.courthouse cth
                ON
                    cas.cth_id = cth.cth_id
                AND cth.cth_id IN
                    (   SELECT
                            DISTINCT(grc.cth_id)
                        FROM
                            darts.user_account usr
                        INNER JOIN
                            darts.security_group_user_account_ae gua
                        ON
                            usr.usr_id = gua.usr_id
                        INNER JOIN
                            darts.security_group grp
                        ON
                            gua.grp_id = grp.grp_id
                        INNER JOIN
                            darts.security_group_courthouse_ae grc
                        ON
                            grp.grp_id = grc.grp_id
                        WHERE
                            usr.usr_id = :usr_id
                        AND grp.rol_id = :rol_id)
                INNER JOIN
                    darts.hearing hea
                ON
                    tra.hea_id = hea.hea_id
                INNER JOIN
                    darts.transcription_type trt
                ON
                    tra.trt_id = trt.trt_id
                INNER JOIN
                    darts.transcription_status trs
                ON
                    tra.trs_id = trs.trs_id
                LEFT JOIN
                    darts.transcription_urgency tru
                ON
                    tra.tru_id = tru.tru_id
                INNER JOIN
                    darts.transcription_workflow requested_trw
                ON
                    tra.tra_id = requested_trw.tra_id
                AND requested_trw.trs_id = 1
                INNER JOIN
                    darts.transcription_workflow complete_trw
                ON
                    tra.tra_id = complete_trw.tra_id
                AND complete_trw.trs_id = 6
                AND complete_trw.workflow_actor = :usr_id
                WHERE
                    tra.trs_id = 6
                AND complete_trw.workflow_ts >= CURRENT_DATE
                ORDER BY
                    transcription_id desc
                """,
            new MapSqlParameterSource("usr_id", userId)
                .addValue("rol_id", TRANSCRIBER.getId()),
            transcriberViewSummaryRowMapper
        );
    }

    @Override
    public List<Integer> getAuthorisedCourthouses(Integer userId, Integer roleId) {
        return jdbcTemplate.queryForList(
            """
                select
                    distinct courthouse.cth_id
                from
                    darts.user_account user_account
                join
                    (darts.security_group_user_account_ae security_group_user_account_ae
                join
                    darts.security_group security_group
                        on security_group.grp_id=security_group_user_account_ae.grp_id)
                            on user_account.usr_id=security_group_user_account_ae.usr_id
                    join
                        (darts.security_group_courthouse_ae security_group_courthouse_ae
                    join
                        darts.courthouse courthouse
                            on courthouse.cth_id=security_group_courthouse_ae.cth_id)
                                on security_group.grp_id=security_group_courthouse_ae.grp_id
                        where
                            user_account.usr_id=:usr_id
                            and security_group.rol_id in (:rol_id)
                """,
            new MapSqlParameterSource(USR_ID, userId)
                .addValue(ROL_ID, roleId),
            Integer.class
        );
    }

    @Override
    public Integer getTranscriptionsCountForCourthouses(List<Integer> courthouseIds, Integer transcriptionStatusId, int userId) {
        String sql = """
            SELECT
                count(*)
            FROM
                darts.transcription transcription
            JOIN
                darts.court_case court_case
                    on court_case.cas_id=transcription.cas_id
            JOIN
                darts.courthouse courthouse
                    on courthouse.cth_id=court_case.cth_id
            INNER JOIN
                darts.transcription_workflow trw
            ON
                transcription.tra_id = trw.tra_id
            AND trw.trs_id = :trs_id
            WHERE
                court_case.cth_id IN (:cth_ids)
                AND transcription.trs_id=:trs_id
            """;
        if (transcriptionStatusId.equals(WITH_TRANSCRIBER.getId())) {
            sql += " AND trw.workflow_actor = " + userId;
        }

        return jdbcTemplate.queryForObject(
            sql,
            new MapSqlParameterSource("cth_ids", courthouseIds)
                .addValue("trs_id", transcriptionStatusId),
            Integer.class
        );
    }


}

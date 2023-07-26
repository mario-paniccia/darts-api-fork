--v6 add sequences,remove character/numeric size limits, change DATE to TIMESTAMP
--v7 consistently add the legacy primary and foreign keys
--v8 3NF courthouses
--v9 remove various legacy columns
--v10 change some numeric columns to boolean, remove unused legacy column c_upload_priority
--v11 introduce many:many case:hearing, removed version label & superceded from moj_hearing, as no source for migration, and assume unneeded by modernised
--v12 remove reporting_restrictions from annotation,cached_media,event,media,transcription,transformation_request
--    add message_id, event_type_id to moj_event
--    add moj_event_type table and links to moj_event by FK
--v13 adding Not null to moj_transcription FK moj_cas_id & moj_crt_id, 
--    adding moj_transcription_type table to replace c_type. Remove fields c_notification_type, c_urgent from transcription
--    add event_name to moj_event
--    add moj_urgency table and fk to moj_transcription
--    added comment_ts and author to moj_transcription_comment
--v14 removing unneeded columns from moj_courthouse, normalising crown court code from daily list 
--    amending judge,defendant,defence, prosecutor on hearing to be 1-d array instead of scalar
--    rename i_version_label to i_version
--v15 remove moj_crt_id from case and corresponding FK
--v16 add moj_hea_id to transcription and corresponding FK
--    add moj_user to this script
--v17 further comments reagrding properties of live data
--v18 moving atributes from moj_hearing to moj_case, changing timestamps to ts with tz
--v19 amended courthouse_name to be unique, amended courthouse_code to be integer
--    removing c_scheduled_start from moj_case, to be replaced by 2 columns on moj_hearing, scheduled_start_time and hearing_is_actual flag
--v20 moving moj_event and moj_media to link to moj_hearing rather than moj_case, resulting in moj_case_event_ae and 
--    moj_case_media_ae changing name
--v21 normalising c_reporting_restrictions into moj_reporting_restriction table
--    change alias for courthouse from CRT to CTH, accommodate new COURTROOM table aliased to CTR
--    add COURTROOM table, replace existing FKs to COURTHOUSE with ones to COURTROOM for event, media
--    rename moj_event_type.type to .evt_type 
--    remove c_courtroom from moj_annotation,,moj_cached_media, moj_event, moj_hearing, moj_media, 
--    moj_transcription, moj_transformation_request
--    Remove associative entity case_hearing, replace with simple PK-FK relation
--v22 updated all sequences to cache 20
--    updated moj_daily_list and introduced moj_notification
--v23 remove moj_transformation_request, moj_transformation_log, moj_cached_media, replace with moj_media_request
--v24 adding request_type to moj_media_request omitted in error
--    amending external_object_directory to store one external address per record
--    amended c_case_id to c_case_number
--    replacing all smallint with integer, which includes all use of i_version
--    moj_courthouse.courthouse_code, moj_media_request.req_proc_attempts, moj_notification.send_attempts
--    remove i_version, r_version_label and i_version_label from moj_case, due to no legacy versioned data
--    amend daily_list content column to be character varying from xml, to store list in JSON format
--    remove c_type from moj_case
--v25 adding tablespace clauses to tables and indexes
--v26 adding multi-column unique constraints to moj_courtroom and moj_hearing
--v27 adding c_case_id to moj_event, adding transfer_attempts to both object_directory Tables
--    changing checksum from uuid to character varying
--v28 removing all moj_ prefixes to table and pk columns
--    reinstating moj_cth_id to case table
--v29 removing c_, r_, i_ prefixes to column names, switching daily_list content back to character varying
--    reinstating FK from case to courthouse
--    amending start and end on daily_list to be DATE, no time component
--    adding suffix of _list where [] is used on datatype to denote an array
--    rename event_type to event_handler
--    added external_location_type table
--v30 added standing data for reporting restrictions
--    added region table and associative entity to courthouse
--    added device_register table ( equivalent to legacy tbl_moj_node)
--    added unique constraint on court_case(cth_id, case_number
--    standardised the use of "last_modified_ts" , where previously using "modified_ts" or "last_updated_ts"
--    standardised the use of "last_modified_by" , where previously using "modified_by"
--    reduced number of Documentum columns on user_account table, while adding a few others
--v31 add darts_owner and darts_user accounts and amend security accordingly
--v32 introduce defendant, prosecutor, defence tables to remove the need for character varying arrays on court_case, and add foreign keys to court_case
--    introduce judge table to remove the need for character varying array on hearing, and add foreign key to hearing
--    correct name of reporting_restriction_pk and case of the table name from pleural to singular
--    add not null constraint to PK columnms on region and user_account ( should be inferrable, but hibernate likes it explicitly defined)
--    amend NUMERIC to INTEGER on user_account and event tables
--v33 remove synthetic PK from associative entities hearing_events_ae and hearing_media_as, replace with PK on natural key
--v34 add case_retention, retention_policy & case_retention_event tables
--v35 remove reporting_restrictions table, replace with foreign key on case to event_handler and add boolean to event_handler

 


-- List of Table Aliases
-- annotation                 ANN
-- case_retention             CAR
-- case_retention_event       CRE
-- court_case                 CAS
-- courthouse                 CTH
-- courthouse_region_ae       CRA
-- courtroom                  CTR
-- daily_list                 DAL
-- defence_name               DFC
-- defendant_name             DFD
-- device_register            DER
-- event                      EVE
-- event_handler              EVH
-- external_object_directory  EOD
-- hearing                    HEA
-- judge_name                 JUD
-- media                      MED
-- media_request              MER
-- notification               NOT
-- object_directory_status    ODS
-- prosecutor_name            PRN
-- region                     REG
-- report                     REP
-- retention_policy           RTP
-- transcription              TRA
-- transcription_type         TRT
-- transient_object_directory TOD
-- urgency                    URG
-- user_account               USR

CREATE USER darts_owner with
NOSUPERUSER
NOINHERIT
NOCREATEDB
NOCREATEROLE
NOREPLICATION
PASSWORD 'darts_owner';

CREATE USER darts_user with
NOSUPERUSER
NOINHERIT
NOCREATEDB
NOCREATEROLE
NOREPLICATION
PASSWORD 'darts_user';

CREATE SCHEMA DARTS AUTHORIZATION DARTS_OWNER;

CREATE TABLESPACE darts_tables  location 'E:/PostgreSQL/Tables';
CREATE TABLESPACE darts_indexes location 'E:/PostgreSQL/Indexes';

GRANT ALL ON TABLESPACE darts_tables TO darts_owner;
GRANT ALL ON TABLESPACE darts_indexes TO darts_owner;

SET ROLE DARTS_OWNER;

SET SEARCH_PATH TO darts;

CREATE TABLE annotation
(ann_id                   INTEGER					 NOT NULL
,cas_id                   INTEGER					 NOT NULL
,ctr_id                   INTEGER
,annotation_text          CHARACTER VARYING
,annotation_ts            TIMESTAMP WITH TIME ZONE
,annotation_object_id     CHARACTER VARYING(16)		
,version_label            CHARACTER VARYING(32)
,superseded               BOOLEAN
,version                  INTEGER
) TABLESPACE darts_tables;

COMMENT ON COLUMN annotation.ann_id
IS 'primary key of annotation';

COMMENT ON COLUMN annotation.cas_id
IS 'foreign key from court_case';

COMMENT ON COLUMN annotation.ctr_id
IS 'foreign key from courtroom';

COMMENT ON COLUMN annotation.annotation_object_id
IS 'internal Documentum primary key from moj_annotation_s';

COMMENT ON COLUMN annotation.annotation_text
IS 'directly sourced from moj_annotation_s.c_text';

COMMENT ON COLUMN annotation.annotation_ts
IS 'directly sourced from moj_annotation_s';

COMMENT ON COLUMN annotation.version_label
IS 'inherited from dm_sysobject_r, for r_object_type of moj_annotation';

CREATE TABLE case_retention
(car_id                    INTEGER                   NOT NULL
,cas_id                    INTEGER                   NOT NULL
,rtp_id                    INTEGER                   NOT NULL
,retain_until_ts           TIMESTAMP WITH TIME ZONE
,manual_override           BOOLEAN                   NOT NULL
) TABLESPACE darts_tables;

COMMENT ON COLUMN case_retention.car_id
IS 'primary key of case_retention';

COMMENT ON COLUMN case_retention.cas_id
IS 'foreign key from court_case';

COMMENT ON COLUMN case_retention.rtp_id
IS 'foreign key from retention_policy';

CREATE TABLE case_retention_event
(cre_id                    INTEGER                   NOT NULL
,car_id                    INTEGER                   NOT NULL
,sentencing_type           INTEGER                   NOT NULL
,total_sentencing          CHARACTER VARYING
,last_processed_event_ts   TIMESTAMP WITH TIME ZONE  NOT NULL
,submitted_by              INTEGER
,user_comment              CHARACTER VARYING
) TABLESPACE darts_tables;

COMMENT ON COLUMN case_retention_event.cre_id
IS 'primary key of case_retention_event';

COMMENT ON COLUMN case_retention_event.car_id
IS 'foreign key from case_retention';



CREATE TABLE court_case
(cas_id                    INTEGER					 NOT NULL
,cth_id                    INTEGER                   NOT NULL
,evh_id                    INTEGER               -- must map to one of the reporting restriction elements
,case_object_id            CHARACTER VARYING(16)
,case_number               CHARACTER VARYING     -- maps to c_case_id in legacy                    
,case_closed               BOOLEAN
,interpreter_used          BOOLEAN
,case_closed_ts            TIMESTAMP WITH TIME ZONE
,retain_until_ts           TIMESTAMP WITH TIME ZONE
,version_label             CHARACTER VARYING(32)
,superseded                BOOLEAN
,version                   INTEGER
) TABLESPACE darts_tables;

COMMENT ON COLUMN court_case.cas_id
IS 'primary key of court_case';

COMMENT ON COLUMN court_case.cth_id
IS 'foreign key to courthouse';

COMMENT ON COLUMN court_case.case_object_id
IS 'internal Documentum primary key from moj_case_s';

COMMENT ON COLUMN court_case.case_number
IS 'directly sourced from moj_case_s.c_case_id';

COMMENT ON COLUMN court_case.case_closed
IS 'migrated from moj_case_s, converted from numeric to boolean';

COMMENT ON COLUMN court_case.interpreter_used
IS 'migrated from moj_case_s, converted from numeric to boolean';

COMMENT ON COLUMN court_case.case_closed_ts
IS 'directly sourced from moj_case_s.c_case_closed_date';

COMMENT ON COLUMN court_case.version_label
IS 'inherited from dm_sysobject_r, for r_object_type of moj_case, containing the version record';

COMMENT ON COLUMN court_case.version
IS 'inherited from dm_sysobject_r, for r_object_type of moj_case, set according to the current flag';

CREATE TABLE courthouse
(cth_id                    INTEGER					 NOT NULL
,courthouse_code           INTEGER                                     UNIQUE
,courthouse_name           CHARACTER VARYING         NOT NULL          UNIQUE
,created_ts                TIMESTAMP WITH TIME ZONE  NOT NULL
,last_modified_ts          TIMESTAMP WITH TIME ZONE  NOT NULL
) TABLESPACE darts_tables;

COMMENT ON COLUMN courthouse.cth_id
IS 'primary key of courthouse';

COMMENT ON COLUMN courthouse.courthouse_code
IS 'corresponds to the c_crown_court_code found in daily lists';

COMMENT ON COLUMN courthouse.courthouse_name
IS 'directly sourced from moj_courthouse_s.c_id';

CREATE TABLE courthouse_region_ae
(cra_id                     INTEGER                  NOT NULL
,cth_id                     INTEGER                  NOT NULL
,reg_id                     INTEGER                  NOT NULL
) TABLESPACE darts_tables;

CREATE TABLE courtroom
(ctr_id                     INTEGER                  NOT NULL
,cth_id                     INTEGER                  NOT NULL
,courtroom_name             CHARACTER VARYING        NOT NULL
--,UNIQUE(moj_cth_id,courtroom_name)
) TABLESPACE darts_tables;

COMMENT ON COLUMN courtroom.ctr_id
IS 'primary key of courtroom';

COMMENT ON COLUMN courtroom.cth_id
IS 'foreign key to courthouse';

CREATE TABLE daily_list
(dal_id                     INTEGER					 NOT NULL
,cth_id                     INTEGER					 NOT NULL
,daily_list_object_id       CHARACTER VARYING(16)
,unique_id                  CHARACTER VARYING
--,c_crown_court_name       CHARACTER VARYING        -- removed, normalised to courthouses, but note that in legacy there is mismatch between moj_courthouse_s.c_id and moj_daily_list_s.c_crown_court_name to be resolved
,job_status                 CHARACTER VARYING        -- one of "New","Partially Processed","Processed","Ignored","Invalid"
,published_ts               TIMESTAMP WITH TIME ZONE 
,start_dt                   DATE   
,end_dt                     DATE -- all values match c_start_date
,daily_list_id_s            CHARACTER VARYING        -- non unique integer in legacy
,daily_list_source          CHARACTER VARYING        -- one of CPP,XHB ( live also sees nulls and spaces)   
,daily_list_content         CHARACTER VARYING
,created_ts                 TIMESTAMP WITH TIME ZONE               
,last_modified_ts           TIMESTAMP WITH TIME ZONE
,version_label              CHARACTER VARYING(32)  
,superseded                 BOOLEAN
,version                    INTEGER
) TABLESPACE darts_tables;

COMMENT ON COLUMN daily_list.dal_id
IS 'primary key of daily_list';

COMMENT ON COLUMN daily_list.cth_id
IS 'foreign key from courthouse';

COMMENT ON COLUMN daily_list.daily_list_object_id
IS 'internal Documentum primary key from moj_daily_list_s';

COMMENT ON COLUMN daily_list.unique_id
IS 'directly sourced from moj_daily_list_s, received as part of the XML, used to find duplicate daily lists';

COMMENT ON COLUMN daily_list.job_status
IS 'directly sourced from moj_daily_list_s';

COMMENT ON COLUMN daily_list.published_ts
IS 'directly sourced from moj_daily_list_s.c_timestamp';

COMMENT ON COLUMN daily_list.start_dt
IS 'directly sourced from moj_daily_list_s.c_start_date';

COMMENT ON COLUMN daily_list.end_dt
IS 'directly sourced from moj_daily_list_s.c_end_date';

COMMENT ON COLUMN daily_list.daily_list_id_s
IS 'directly sourced from moj_daily_list_s';

COMMENT ON COLUMN daily_list.daily_list_source
IS 'directly sourced from moj_daily_list_s';

COMMENT ON COLUMN daily_list.version_label
IS 'inherited from dm_sysobject_r, for r_object_type of moj_daily_list';

CREATE TABLE defence_name
(dfc_id                     INTEGER                 NOT NULL
,cas_id                     INTEGER                 NOT NULL
,defence                    CHARACTER VARYING       NOT NULL
) TABLESPACE darts_tables;

COMMENT ON COLUMN defence_name.dfc_id 
IS 'primary key of defence_name';

COMMENT ON COLUMN defence_name.cas_id
IS 'foreign key from court_case';

CREATE TABLE defendant_name
(dfd_id                     INTEGER                 NOT NULL
,cas_id                     INTEGER                 NOT NULL
,defence                    CHARACTER VARYING       NOT NULL
) TABLESPACE darts_tables;

COMMENT ON COLUMN defendant_name.dfd_id 
IS 'primary key of defendant_name';

COMMENT ON COLUMN defendant_name.cas_id
IS 'foreign key from court_case';

CREATE TABLE device_register
(der_id                     INTEGER                  NOT NULL
,ctr_id                     INTEGER                  NOT NULL
,node_id                    INTEGER                  
,hostname                   CHARACTER VARYING
,ip_address                 CHARACTER VARYING
,mac_address                CHARACTER VARYING
) TABLESPACE darts_tables;

COMMENT ON TABLE device_register
IS 'corresponds to tbl_moj_node from legacy';

COMMENT ON COLUMN device_register.der_id
IS 'primary key of device_register';

COMMENT ON COLUMN device_register.ctr_id 
IS 'foreign key from moj_courtroom, legacy stored courthouse and courtroon un-normalised';


CREATE TABLE event
(eve_id                     INTEGER					 NOT NULL
,ctr_id                     INTEGER
,evh_id                     INTEGER
,event_object_id            CHARACTER VARYING(16)
,event_id                   INTEGER
,event_name                 CHARACTER VARYING -- details of the handler, at point in time the event arose
,event_text                 CHARACTER VARYING
,event_ts                   TIMESTAMP WITH TIME ZONE  
,case_number                CHARACTER VARYING(32)[] 
,version_label              CHARACTER VARYING(32)
,message_id                 CHARACTER VARYING
,superseded                 BOOLEAN
,version                    INTEGER
) TABLESPACE darts_tables;

COMMENT ON COLUMN event.eve_id
IS 'primary key of moj_event';

COMMENT ON COLUMN event.ctr_id
IS 'foreign key from moj_courtroom';

COMMENT ON COLUMN event.evh_id
IS 'foreign key for the moj_event_handler table';

COMMENT ON COLUMN event.event_object_id
IS 'internal Documentum primary key from moj_event_s';

COMMENT ON COLUMN event.event_id
IS 'directly sourced from moj_event_s';

COMMENT ON COLUMN event.event_name
IS 'inherited from dm_sysobect_s.object_name';

COMMENT ON COLUMN event.event_text
IS 'inherited from moj_annotation_s.c_text';

COMMENT ON COLUMN event.event_ts
IS 'inherited from moj_annotation_s';

COMMENT ON COLUMN event.version_label
IS 'inherited from dm_sysobject_r, for r_object_type of moj_event';

COMMENT ON COLUMN event.message_id
IS 'no migration element, records the id of the message that gave rise to this event';

CREATE TABLE event_handler
(evh_id                      INTEGER					 NOT NULL
,event_type                  CHARACTER VARYING           NOT NULL
,event_sub_type              CHARACTER VARYING
,event_name                  CHARACTER VARYING           NOT NULL
,handler                     CHARACTER VARYING
,active                      BOOLEAN                     NOT NULL
,created_ts                  TIMESTAMP WITH TIME ZONE    NOT NULL
,last_modified_ts            TIMESTAMP WITH TIME ZONE    NOT NULL
,last_modified_by            INTEGER                     NOT NULL
) TABLESPACE darts_tables;

COMMENT ON TABLE event_handler
IS 'content will be derived from TBL_MOJ_DOC_HANDLER in the legacy database, but currently has no primary key and 6 fully duplicated rows';

COMMENT ON COLUMN event_handler.evh_id
IS 'primary key of moj_event_type';

COMMENT ON COLUMN event_handler.event_type
IS 'directly sourced from doc_type';

COMMENT ON COLUMN event_handler.event_sub_type
IS 'directly sourced from doc_sub_type';

COMMENT ON COLUMN event_handler.event_name
IS 'directly sourced from event_name';

COMMENT ON COLUMN event_handler.handler
IS 'directly sourced from doc_handler';


CREATE TABLE external_object_directory
(eod_id                      INTEGER			 		 NOT NULL
,med_id                      INTEGER
,tra_id                      INTEGER
,ann_id                      INTEGER
,ods_id                      INTEGER                     NOT NULL  -- FK to moj_object_directory_status
,elt_id                      INTEGER                     NOT NULL  -- one of inbound,unstructured,arm,tempstore,vodafone 
-- additional optional FKs to other relevant internal objects would require columns here
,external_location           UUID                        NOT NULL
,checksum	                 CHARACTER VARYING
,transfer_attempts           INTEGER
,created_ts                  TIMESTAMP WITH TIME ZONE    NOT NULL
,last_modified_ts            TIMESTAMP WITH TIME ZONE    NOT NULL
,last_modified_by            INTEGER                     NOT NULL  -- FK to moj_user.moj_usr_id
) TABLESPACE darts_tables;

COMMENT ON COLUMN external_object_directory.eod_id
IS 'primary key of external_object_directory';

COMMENT ON COLUMN external_object_directory.elt_id
IS 'foreign key from external_location_type';

-- added two foreign key columns, but there will be as many FKs as there are distinct objects with externally stored components

COMMENT ON COLUMN external_object_directory.med_id
IS 'foreign key from media';

COMMENT ON COLUMN external_object_directory.tra_id
IS 'foreign key from transcription';

COMMENT ON COLUMN external_object_directory.ann_id
IS 'foreign key from annotation';

CREATE TABLE external_location_type
(elt_id                     INTEGER                  NOT NULL
,elt_description            CHARACTER VARYING
) TABLESPACE darts_tables;

COMMENT ON TABLE external_location_type
IS 'used to record acceptable external locations, found in external_object_directory';

CREATE TABLE hearing
(hea_id                     INTEGER					   NOT NULL
,cas_id                     INTEGER                    NOT NULL
,ctr_id                     INTEGER                    NOT NULL
,hearing_date               DATE     -- to record only DATE component of hearings, both scheduled and actual
,scheduled_start_time       TIME     -- to record only TIME component of hearings, while they are scheduled only
,hearing_is_actual          BOOLEAN  -- TRUE for actual hearings, FALSE for scheduled hearings
,judge_hearing_date         CHARACTER VARYING
--,UNIQUE(moj_cas_id,moj_ctr,c_hearing_date)
) TABLESPACE darts_tables;

COMMENT ON COLUMN hearing.hea_id
IS 'primary key of hearing';

COMMENT ON COLUMN hearing.cas_id
IS 'foreign key from case';

COMMENT ON COLUMN hearing.ctr_id
IS 'foreign key from courtroom';

COMMENT ON COLUMN hearing.hearing_date
IS 'directly sourced from moj_case_r';

COMMENT ON COLUMN hearing.judge_hearing_date
IS 'directly sourced from moj_case_r';

CREATE TABLE hearing_event_ae
(hea_id                     INTEGER							 NOT NULL
,eve_id                     INTEGER							 NOT NULL
) TABLESPACE darts_tables;

COMMENT ON COLUMN hearing_event_ae.hea_id
IS 'foreign key from hearing, part of composite natural key and PK';

COMMENT ON COLUMN hearing_event_ae.eve_id
IS 'foreign key from event, part of composite natural key and PK';

CREATE TABLE hearing_media_ae
(hea_id                     INTEGER							 NOT NULL
,med_id                     INTEGER							 NOT NULL
) TABLESPACE darts_tables;

COMMENT ON COLUMN hearing_media_ae.hea_id
IS 'foreign key from case, part of composite natural key and PK';

COMMENT ON COLUMN hearing_media_ae.med_id
IS 'foreign key from media, part of composite natural key and PK';

CREATE TABLE judge_name
(jud_id                     INTEGER                 NOT NULL
,hea_id                     INTEGER                 NOT NULL
,judge                      CHARACTER VARYING       NOT NULL
) TABLESPACE darts_tables;

COMMENT ON COLUMN judge_name.jud_id 
IS 'primary key of judge_name';

COMMENT ON COLUMN judge_name.hea_id
IS 'foreign key from hearing';

CREATE TABLE media
(med_id                     INTEGER					 NOT NULL
,ctr_id                     INTEGER
,media_object_id            CHARACTER VARYING(16)
,channel                    INTEGER
,total_channels             INTEGER                  --99.9% are "4" in legacy 
,reference_id               CHARACTER VARYING        --all nulls in legacy
,start_ts                   TIMESTAMP WITH TIME ZONE 
,end_ts                     TIMESTAMP WITH TIME ZONE
,case_number                CHARACTER VARYING(32)[]  --this is a placeholder for moj_case_document_r.c_case_id, known to be repeated for moj_media object types
,version_label              CHARACTER VARYING(32)
,superseded                 BOOLEAN
,version                    INTEGER
) TABLESPACE darts_tables;

COMMENT ON COLUMN media.med_id
IS 'primary key of media';

COMMENT ON COLUMN media.ctr_id
IS 'foreign key from courtroom';

COMMENT ON COLUMN media.media_object_id
IS 'internal Documentum primary key from moj_media_s';

COMMENT ON COLUMN media.channel
IS 'directly sourced from moj_media_s';

COMMENT ON COLUMN media.total_channels
IS 'directly sourced from moj_media_s';

COMMENT ON COLUMN media.reference_id
IS 'directly sourced from moj_media_s';

COMMENT ON COLUMN media.start_ts
IS 'inherited from moj_case_document_s';

COMMENT ON COLUMN media.end_ts
IS 'inherited from moj_case_document_s';

COMMENT ON COLUMN media.version_label
IS 'inherited from dm_sysobject_r, for r_object_type of moj_media';

CREATE TABLE media_request
(mer_id                     INTEGER                    NOT NULL
,hea_id                     INTEGER                    NOT NULL
,requestor                  INTEGER                    NOT NULL  -- FK to moj_user.moj_usr_id
,request_status             CHARACTER VARYING
,request_type               CHARACTER VARYING
,req_proc_attempts          INTEGER 
,start_ts                   TIMESTAMP WITH TIME ZONE
,end_ts                     TIMESTAMP WITH TIME ZONE
,created_ts                 TIMESTAMP WITH TIME ZONE
,last_modified_ts            TIMESTAMP WITH TIME ZONE
,last_accessed_ts           TIMESTAMP WITH TIME ZONE
,output_filename            CHARACTER VARYING
,output_format              CHARACTER VARYING
) TABLESPACE darts_tables;

COMMENT ON COLUMN media_request.mer_id
IS 'primary key of media_request';

COMMENT ON COLUMN media_request.hea_id
IS 'foreign key of hearing';

COMMENT ON COLUMN media_request.requestor
IS 'requestor of the media request, possibly migrated from moj_transformation_request_s';

COMMENT ON COLUMN media_request.request_status
IS 'status of the migration request';

COMMENT ON COLUMN media_request.req_proc_attempts
IS 'number of attempts by ATS to process the request';

COMMENT ON COLUMN media_request.start_ts
IS 'start time in the search criteria for request, possibly migrated from moj_cached_media_s or moj_transformation_request_s';

COMMENT ON COLUMN media_request.end_ts
IS 'end time in the search criteria for request, possibly migrated from moj_cached_media_s or moj_transformation_request_s';

COMMENT ON COLUMN media_request.output_filename
IS 'filename of the requested media object, possibly migrated from moj_transformation_request_s';

COMMENT ON COLUMN media_request.output_format
IS 'format of the requested media object, possibly migrated from moj_transformation_s';


CREATE TABLE notification
(not_id                     INTEGER                    NOT NULL
,cas_id                     INTEGER                    NOT NULL
,notification_event   		CHARACTER VARYING          NOT NULL
,notification_status        CHARACTER VARYING          NOT NULL
,email_address              CHARACTER VARYING          NOT NULL
,send_attempts              INTEGER
,template_values            CHARACTER VARYING
,created_ts                 TIMESTAMP WITH TIME ZONE   NOT NULL
,last_modified_ts            TIMESTAMP WITH TIME ZONE   NOT NULL
) TABLESPACE darts_tables;

COMMENT ON COLUMN notification.not_id
IS 'primary key of notification';

COMMENT ON COLUMN notification.cas_id
IS 'foreign key to case';

COMMENT ON COLUMN notification.notification_event
IS 'event giving rise to the need for outgoing notification';

COMMENT ON COLUMN notification.notification_status
IS 'status of the notification, expected to be one of [O]pen, [P]rocessing, [S]end, [F]ailed';

COMMENT ON COLUMN notification.email_address
IS 'recipient of the notification';

COMMENT ON COLUMN notification.send_attempts
IS 'number of outgoing requests to gov.uk';

COMMENT ON COLUMN notification.template_values
IS 'any extra fields not already covered or inferred from the case, in JSON format';


CREATE TABLE object_directory_status
(ods_id                     INTEGER                  NOT NULL
,ods_description            CHARACTER VARYING
) TABLESPACE darts_tables;

COMMENT ON TABLE object_directory_status
IS 'used to record acceptable statuses found in [external/transient]_object_directory';

CREATE TABLE prosecutor_name
(prn_id                     INTEGER                 NOT NULL
,cas_id                     INTEGER                 NOT NULL
,prosecutor                 CHARACTER VARYING       NOT NULL
) TABLESPACE darts_tables;

COMMENT ON COLUMN prosecutor_name.prn_id 
IS 'primary key of prosecutor_name';

COMMENT ON COLUMN prosecutor_name.cas_id
IS 'foreign key from court_case';

CREATE TABLE region
(reg_id                     INTEGER                 NOT NULL
,region_name                CHARACTER VARYING       NOT NULL
) TABLESPACE darts_tables;


CREATE TABLE report               
(rep_id                     INTEGER					 NOT NULL
,report_object_id           CHARACTER VARYING(16)
,name                       CHARACTER VARYING
,subject                    CHARACTER VARYING
,report_text                CHARACTER VARYING
,query                      CHARACTER VARYING
,recipients                 CHARACTER VARYING
,version_label              CHARACTER VARYING(32)
,superseded                 BOOLEAN
,version                    INTEGER
) TABLESPACE darts_tables;

COMMENT ON COLUMN report.rep_id
IS 'primary key of report';

COMMENT ON COLUMN report.report_object_id
IS 'internal Documentum primary key from moj_report_s';

COMMENT ON COLUMN report.name
IS 'directly sourced from moj_report_s';

COMMENT ON COLUMN report.subject
IS 'directly sourced from moj_report_s';

COMMENT ON COLUMN report.report_text
IS 'directly sourced from moj_report_s';

COMMENT ON COLUMN report.query
IS 'directly sourced from moj_report_s';

COMMENT ON COLUMN report.recipients
IS 'directly sourced from moj_report_s';

COMMENT ON COLUMN report.version_label
IS 'inherited from dm_sysobject_r, for r_object_type of moj_report';

CREATE TABLE retention_policy
(rtp_id                   INTEGER                  NOT NULL
,policy_name              CHARACTER VARYING        NOT NULL
,retention_period         INTEGER                  NOT NULL
) TABLESPACE darts_tables;

COMMENT ON COLUMN retention_policy.rtp_id
IS 'primary key of retention_policy';

CREATE TABLE transcription
(tra_id                   INTEGER				   NOT NULL
,cas_id                   INTEGER                  NOT NULL
,ctr_id                   INTEGER                  NOT NULL
,trt_id                   INTEGER                  NOT NULL  
,urg_id                   INTEGER                  -- remains nullable, as nulls present in source data ( c_urgency)       
,hea_id                   INTEGER                  -- remains nullable, until migration is complete
,transcription_object_id  CHARACTER VARYING(16)    -- legacy pk from moj_transcription_s.r_object_id
,company                  CHARACTER VARYING        -- effectively unused in legacy, either null or "<this field will be completed by the system>"
,requestor                CHARACTER VARYING        -- 1055 distinct, from <forname><surname> to <AAANNA>
,current_state            CHARACTER VARYING        -- 23 distinct, far more than 5 expected (requested,awaiting authorisation,with transcribed, complete, rejected)
,current_state_ts         TIMESTAMP WITH TIME ZONE -- date & time record entered the current c_current_state
,hearing_date             TIMESTAMP WITH TIME ZONE -- 3k records have time component, but all times are 23:00,so effectively DATE only, will be absolete once moj_hea_id populated
,start_ts                 TIMESTAMP WITH TIME ZONE -- both c_start and c_end have time components
,end_ts                   TIMESTAMP WITH TIME ZONE -- we have 49k rows in legacy moj_transcription_s, 7k have c_end != c_start
,created_ts               TIMESTAMP WITH TIME ZONE               
,last_modified_ts         TIMESTAMP WITH TIME ZONE
,last_modified_by         INTEGER                  -- will need to be FK to users table
,requested_by             INTEGER                  -- will need to be FK to users table
,approved_by              INTEGER                  -- will need to be FK to users table
,approved_on_ts           TIMESTAMP WITH TIME ZONE
,transcribed_by           INTEGER                  -- will need to be FK to users table
,version_label            CHARACTER VARYING(32)
,superseded               BOOLEAN
,version                  INTEGER
) TABLESPACE darts_tables;

COMMENT ON COLUMN transcription.tra_id
IS 'primary key of transcription';
    
COMMENT ON COLUMN transcription.cas_id
IS 'foreign key from case';

COMMENT ON COLUMN transcription.ctr_id
IS 'foreign key from courtroom';

COMMENT ON COLUMN transcription.urg_id
IS 'foreign key from urgency';

COMMENT ON COLUMN transcription.trt_id
IS 'foreign key to transcription_type, sourced from moj_transcription_s.c_type';

COMMENT ON COLUMN transcription.transcription_object_id
IS 'internal Documentum primary key from moj_transcription_s';
    
COMMENT ON COLUMN transcription.company
IS 'directly sourced from moj_transcription_s';

COMMENT ON COLUMN transcription.requestor
IS 'directly sourced from moj_transcription_s';

COMMENT ON COLUMN transcription.current_state
IS 'directly sourced from moj_transcription_s';

COMMENT ON COLUMN transcription.hearing_date
IS 'directly sourced from moj_transcription_s';

COMMENT ON COLUMN transcription.start_ts
IS 'inherited from moj_case_document_s';

COMMENT ON COLUMN transcription.end_ts
IS 'inherited from moj_case_document_s';

COMMENT ON COLUMN transcription.version_label
IS 'inherited from dm_sysobject_r, for r_object_type of moj_transcription';

CREATE TABLE transcription_comment
(trc_id                            INTEGER					 NOT NULL
,tra_id                            INTEGER
,transcription_object_id           CHARACTER VARYING(16)     -- this is a placeholder for moj_transcription_s.r_object_id
,transcription_comment             CHARACTER VARYING
,comment_ts                        TIMESTAMP WITH TIME ZONE
,author                            INTEGER                   -- will need to be FK to user table
,created_ts                        TIMESTAMP WITH TIME ZONE               
,last_modified_ts                  TIMESTAMP WITH TIME ZONE
,last_modified_by                  INTEGER                   -- will need to be FK to users table
,superseded                        BOOLEAN
,version                           INTEGER
) TABLESPACE darts_tables;

COMMENT ON COLUMN transcription_comment.trc_id
IS 'primary key of transcription_comment'; 

COMMENT ON COLUMN transcription_comment.tra_id
IS 'foreign key from transcription'; 

COMMENT ON COLUMN transcription_comment.transcription_object_id
IS 'internal Documentum primary key from moj_transcription_s'; 

COMMENT ON COLUMN transcription_comment.transcription_comment
IS 'directly sourced from moj_transcription_r';

COMMENT ON COLUMN transcription_comment.transcription_object_id
IS 'internal Documentum id from moj_transcription_s acting as foreign key';

CREATE TABLE transcription_type
(trt_id                            INTEGER                   NOT NULL
,description                       CHARACTER VARYING        
);

COMMENT ON TABLE transcription_type
IS 'standing data table, migrated from tbl_moj_transcription_type';

COMMENT ON COLUMN transcription_type.trt_id
IS 'primary key, but not sequence generated';

CREATE TABLE transient_object_directory
(tod_id                      INTEGER			 		 NOT NULL
,mer_id                      INTEGER                     NOT NULL 
,ods_id                      INTEGER                     NOT NULL  -- FK to moj_object_directory_status.moj_ods_id
,external_location           UUID                        NOT NULL
,checksum	                 CHARACTER VARYING
,transfer_attempts           INTEGER
,created_ts                  TIMESTAMP WITH TIME ZONE    NOT NULL
,last_modified_ts            TIMESTAMP WITH TIME ZONE    NOT NULL
,last_modified_by            INTEGER                     NOT NULL  -- FK to moj_user.moj_usr_id
) TABLESPACE darts_tables;

CREATE TABLE urgency
(urg_id                            INTEGER                 NOT NULL
,description                       CHARACTER VARYING
) TABLESPACE darts_tables;

COMMENT ON TABLE urgency 
IS 'will be migrated from tbl_moj_urgency';

COMMENT ON COLUMN urgency.urg_id 
IS 'inherited from tbl_moj_urgency.urgency_id';

COMMENT ON COLUMN urgency.description
IS 'inherited from tbl_moj_urgency.description';

CREATE TABLE user_account
(usr_id                  INTEGER                          NOT NULL
,dm_user_s_object_id     CHARACTER VARYING(16)
,user_name               CHARACTER VARYING
,user_email_address      CHARACTER VARYING
,description             CHARACTER VARYING
,user_state              INTEGER
,created_ts              TIMESTAMP WITH TIME ZONE
,last_modified_ts        TIMESTAMP WITH TIME ZONE
,last_login_ts           TIMESTAMP WITH TIME ZONE
,last_modified_by        INTEGER
) TABLESPACE darts_tables;

COMMENT ON TABLE user_account 
IS 'migration columns all sourced directly from dm_user_s, but only for rows where r_is_group = 0';
COMMENT ON COLUMN user_account.usr_id
IS 'primary key of user_account';
COMMENT ON COLUMN user_account.dm_user_s_object_id
IS 'internal Documentum primary key from dm_user_s';

-- primary keys

CREATE UNIQUE INDEX annotation_pk ON annotation(ann_id) TABLESPACE darts_indexes;
ALTER TABLE annotation              ADD PRIMARY KEY USING INDEX annotation_pk;

CREATE UNIQUE INDEX case_retention_pk ON case_retention(car_id) TABLESPACE darts_indexes; 
ALTER TABLE case_retention          ADD PRIMARY KEY USING INDEX case_retention_pk;

CREATE UNIQUE INDEX case_retention_event_pk ON case_retention_event(cre_id) TABLESPACE darts_indexes; 
ALTER TABLE case_retention_event    ADD PRIMARY KEY USING INDEX case_retention_event_pk;

CREATE UNIQUE INDEX court_case_pk ON court_case(cas_id) TABLESPACE darts_indexes; 
ALTER TABLE court_case              ADD PRIMARY KEY USING INDEX court_case_pk;

CREATE UNIQUE INDEX courthouse_pk ON courthouse(cth_id) TABLESPACE darts_indexes;
ALTER TABLE courthouse              ADD PRIMARY KEY USING INDEX courthouse_pk;

CREATE UNIQUE INDEX courthouse_region_ae_pk ON courthouse_region_ae(cra_id) TABLESPACE darts_indexes;
ALTER TABLE courthouse_region_ae    ADD PRIMARY KEY USING INDEX courthouse_region_ae_pk;

CREATE UNIQUE INDEX courtroom_pk ON courtroom(ctr_id) TABLESPACE darts_indexes;
ALTER TABLE courtroom               ADD PRIMARY KEY USING INDEX courtroom_pk;

CREATE UNIQUE INDEX daily_list_pk ON daily_list(dal_id) TABLESPACE darts_indexes;
ALTER TABLE daily_list              ADD PRIMARY KEY USING INDEX daily_list_pk;

CREATE UNIQUE INDEX defence_name_pk ON defence_name(dfc_id) TABLESPACE darts_indexes;
ALTER TABLE defence_name            ADD PRIMARY KEY USING INDEX defence_name_pk;

CREATE UNIQUE INDEX defendant_name_pk ON defendant_name(dfd_id) TABLESPACE darts_indexes;
ALTER TABLE defendant_name          ADD PRIMARY KEY USING INDEX defendant_name_pk;

CREATE UNIQUE INDEX device_register_pk ON device_register(der_id) TABLESPACE darts_indexes;
ALTER TABLE device_register         ADD PRIMARY KEY USING INDEX device_register_pk;

CREATE UNIQUE INDEX event_pk ON event(eve_id) TABLESPACE darts_indexes;
ALTER TABLE event                   ADD PRIMARY KEY USING INDEX event_pk;

CREATE UNIQUE INDEX event_handler_pk ON event_handler(evh_id) TABLESPACE darts_indexes;
ALTER TABLE event_handler            ADD PRIMARY KEY USING INDEX event_handler_pk;

CREATE UNIQUE INDEX external_object_directory_pk ON external_object_directory(eod_id) TABLESPACE darts_indexes;
ALTER TABLE external_object_directory   ADD PRIMARY KEY USING INDEX external_object_directory_pk;

CREATE UNIQUE INDEX external_location_type_pk ON external_location_type(elt_id) TABLESPACE darts_indexes;
ALTER TABLE external_location_type   ADD PRIMARY KEY USING INDEX external_location_type_pk;

CREATE UNIQUE INDEX hearing_pk ON hearing(hea_id) TABLESPACE darts_indexes;
ALTER TABLE hearing                 ADD PRIMARY KEY USING INDEX hearing_pk;

CREATE UNIQUE INDEX hearing_event_ae_pk ON hearing_event_ae(hea_id,eve_id) TABLESPACE darts_indexes;
ALTER TABLE hearing_event_ae        ADD PRIMARY KEY USING INDEX hearing_event_ae_pk;

CREATE UNIQUE INDEX hearing_media_ae_pk ON hearing_media_ae(hea_id,med_id) TABLESPACE darts_indexes;
ALTER TABLE hearing_media_ae        ADD PRIMARY KEY USING INDEX hearing_media_ae_pk;

CREATE UNIQUE INDEX judge_name_pk ON judge_name(jud_id) TABLESPACE darts_indexes;
ALTER TABLE judge_name          ADD PRIMARY KEY USING INDEX judge_name_pk;

CREATE UNIQUE INDEX media_pk ON media(med_id) TABLESPACE darts_indexes;
ALTER TABLE media                   ADD PRIMARY KEY USING INDEX media_pk;

CREATE UNIQUE INDEX media_request_pk ON media_request(mer_id) TABLESPACE darts_indexes;
ALTER TABLE media_request           ADD PRIMARY KEY USING INDEX media_request_pk;

CREATE UNIQUE INDEX notification_pk ON notification(not_id) TABLESPACE darts_indexes;
ALTER TABLE notification            ADD PRIMARY KEY USING INDEX notification_pk;

CREATE UNIQUE INDEX object_directory_status_pk ON object_directory_status(ods_id) TABLESPACE darts_indexes;
ALTER TABLE object_directory_status ADD PRIMARY KEY USING INDEX object_directory_status_pk;

CREATE UNIQUE INDEX prosecutor_name_pk ON prosecutor_name(prn_id) TABLESPACE darts_indexes;
ALTER TABLE prosecutor_name          ADD PRIMARY KEY USING INDEX prosecutor_name_pk;

CREATE UNIQUE INDEX region_pk ON region(reg_id) TABLESPACE darts_indexes;
ALTER TABLE region                  ADD PRIMARY KEY USING INDEX region_pk;

CREATE UNIQUE INDEX report_pk ON report(rep_id) TABLESPACE darts_indexes;
ALTER TABLE report                  ADD PRIMARY KEY USING INDEX report_pk;

CREATE UNIQUE INDEX retention_policy_pk ON retention_policy(rtp_id) TABLESPACE darts_indexes;
ALTER TABLE retention_policy           ADD PRIMARY KEY USING INDEX retention_policy_pk;

CREATE UNIQUE INDEX transcription_pk ON transcription(tra_id) TABLESPACE darts_indexes;
ALTER TABLE transcription           ADD PRIMARY KEY USING INDEX transcription_pk;

CREATE UNIQUE INDEX transcription_comment_pk ON transcription_comment(trc_id) TABLESPACE darts_indexes;
ALTER TABLE transcription_comment   ADD PRIMARY KEY USING INDEX transcription_comment_pk;

CREATE UNIQUE INDEX transcription_type_pk ON transcription_type(trt_id) TABLESPACE darts_indexes;
ALTER TABLE transcription_type      ADD PRIMARY KEY USING INDEX transcription_type_pk;

CREATE UNIQUE INDEX transient_object_directory_pk ON transient_object_directory(tod_id) TABLESPACE darts_indexes;
ALTER TABLE transient_object_directory  ADD PRIMARY KEY USING INDEX transient_object_directory_pk;

CREATE UNIQUE INDEX urgency_pk ON urgency(urg_id) TABLESPACE darts_indexes;
ALTER TABLE urgency                 ADD PRIMARY KEY USING INDEX urgency_pk;

CREATE UNIQUE INDEX user_account_pk ON user_account( usr_id) TABLESPACE darts_indexes;
ALTER TABLE user_account            ADD PRIMARY KEY USING INDEX user_account_pk;

-- defaults for postgres sequences, datatype->bigint, increment->1, nocycle is default, owned by none
CREATE SEQUENCE ann_seq CACHE 20;
CREATE SEQUENCE car_seq CACHE 20;
CREATE SEQUENCE cre_seq CACHE 20;
CREATE SEQUENCE cas_seq CACHE 20;
CREATE SEQUENCE cth_seq CACHE 20;
CREATE SEQUENCE cra_seq CACHE 20;
CREATE SEQUENCE ctr_seq CACHE 20;
CREATE SEQUENCE dal_seq CACHE 20;
CREATE SEQUENCE dfc_seq CACHE 20;
CREATE SEQUENCE dfd_seq CACHE 20;
CREATE SEQUENCE der_seq CACHE 20;
CREATE SEQUENCE eve_seq CACHE 20;
CREATE SEQUENCE evh_seq CACHE 20;
CREATE SEQUENCE eod_seq CACHE 20;
CREATE SEQUENCE elt_seq CACHE 20;
CREATE SEQUENCE jud_seq CACHE 20;
CREATE SEQUENCE hea_seq CACHE 20;
CREATE SEQUENCE med_seq CACHE 20;
CREATE SEQUENCE mer_seq CACHE 20;
CREATE SEQUENCE not_seq CACHE 20;
CREATE SEQUENCE ods_seq CACHE 20;
CREATE SEQUENCE prn_seq CACHE 20;
CREATE SEQUENCE reg_seq CACHE 20;
CREATE SEQUENCE rep_seq CACHE 20;
CREATE SEQUENCE rtp_seq CACHE 20;
CREATE SEQUENCE tra_seq CACHE 20;
CREATE SEQUENCE trc_seq CACHE 20;
CREATE SEQUENCE trt_seq CACHE 20;
CREATE SEQUENCE tod_seq CACHE 20;
CREATE SEQUENCE urg_seq CACHE 20;
CREATE SEQUENCE usr_seq CACHE 20;

-- foreign keys

ALTER TABLE annotation                
ADD CONSTRAINT annotation_case_fk
FOREIGN KEY (cas_id) REFERENCES court_case(cas_id);

ALTER TABLE annotation                
ADD CONSTRAINT annotation_courtroom_fk
FOREIGN KEY (ctr_id) REFERENCES courtroom(ctr_id);

ALTER TABLE case_retention                
ADD CONSTRAINT case_retention_case_fk
FOREIGN KEY (cas_id) REFERENCES court_case(cas_id);

ALTER TABLE case_retention                
ADD CONSTRAINT case_retention_retention_policy_fk
FOREIGN KEY (rtp_id) REFERENCES retention_policy(rtp_id);

ALTER TABLE case_retention_event               
ADD CONSTRAINT case_retention_event_case_retention_fk
FOREIGN KEY (car_id) REFERENCES case_retention(car_id);

ALTER TABLE court_case                        
ADD CONSTRAINT court_case_event_handler_fk
FOREIGN KEY (evh_id) REFERENCES event_handler(evh_id);

ALTER TABLE court_case                        
ADD CONSTRAINT court_case_courthouse_fk
FOREIGN KEY (cth_id) REFERENCES courthouse(cth_id);

ALTER TABLE courthouse_region_ae                        
ADD CONSTRAINT courthouse__region_courthouse_fk
FOREIGN KEY (cth_id) REFERENCES courthouse(cth_id);

ALTER TABLE courthouse_region_ae                        
ADD CONSTRAINT courthouse__region_region_fk
FOREIGN KEY (reg_id) REFERENCES region(reg_id);

ALTER TABLE hearing                     
ADD CONSTRAINT hearing_case_fk
FOREIGN KEY (cas_id) REFERENCES court_case(cas_id);

ALTER TABLE courtroom                   
ADD CONSTRAINT courtroom_courthouse_fk
FOREIGN KEY (cth_id) REFERENCES courthouse(cth_id);

ALTER TABLE daily_list                  
ADD CONSTRAINT daily_list_courthouse_fk
FOREIGN KEY (cth_id) REFERENCES courthouse(cth_id);

ALTER TABLE defence_name                
ADD CONSTRAINT defence_name_case_fk
FOREIGN KEY (cas_id) REFERENCES court_case(cas_id);

ALTER TABLE defendant_name                
ADD CONSTRAINT defendant_name_case_fk
FOREIGN KEY (cas_id) REFERENCES court_case(cas_id);

ALTER TABLE device_register
ADD CONSTRAINT device_register_courtroom_fk
FOREIGN KEY (ctr_id) REFERENCES courtroom(ctr_id);

ALTER TABLE event                       
ADD CONSTRAINT event_courtroom_fk
FOREIGN KEY (ctr_id) REFERENCES courtroom(ctr_id);

ALTER TABLE event                       
ADD CONSTRAINT event_event_handler_fk
FOREIGN KEY (evh_id) REFERENCES event_handler(evh_id);

ALTER TABLE external_object_directory   
ADD CONSTRAINT eod_media_fk
FOREIGN KEY (med_id) REFERENCES media(med_id);

ALTER TABLE external_object_directory   
ADD CONSTRAINT eod_transcription_fk
FOREIGN KEY (tra_id) REFERENCES transcription(tra_id);

ALTER TABLE external_object_directory   
ADD CONSTRAINT eod_annotation_fk
FOREIGN KEY (ann_id) REFERENCES annotation(ann_id);

ALTER TABLE external_object_directory   
ADD CONSTRAINT eod_modified_by_fk
FOREIGN KEY (last_modified_by) REFERENCES user_account(usr_id);

ALTER TABLE external_object_directory   
ADD CONSTRAINT eod_object_directory_status_fk
FOREIGN KEY (ods_id) REFERENCES object_directory_status(ods_id);

ALTER TABLE external_object_directory   
ADD CONSTRAINT eod_external_location_type_fk
FOREIGN KEY (elt_id) REFERENCES external_location_type(elt_id);

ALTER TABLE hearing                     
ADD CONSTRAINT hearing_courtroom_fk
FOREIGN KEY (ctr_id) REFERENCES courtroom(ctr_id);

ALTER TABLE hearing_event_ae            
ADD CONSTRAINT hearing_event_ae_hearing_fk
FOREIGN KEY (hea_id) REFERENCES hearing(hea_id);

ALTER TABLE hearing_event_ae            
ADD CONSTRAINT hearing_event_ae_event_fk
FOREIGN KEY (eve_id) REFERENCES event(eve_id);

ALTER TABLE hearing_media_ae            
ADD CONSTRAINT hearing_media_ae_hearing_fk
FOREIGN KEY (hea_id) REFERENCES hearing(hea_id);

ALTER TABLE hearing_media_ae            
ADD CONSTRAINT hearing_media_ae_media_fk
FOREIGN KEY (med_id) REFERENCES media(med_id);

ALTER TABLE judge_name                
ADD CONSTRAINT judge_name_hearing_fk
FOREIGN KEY (hea_id) REFERENCES hearing(hea_id);

ALTER TABLE media                       
ADD CONSTRAINT media_courtroom_fk
FOREIGN KEY (ctr_id) REFERENCES courtroom(ctr_id);

ALTER TABLE media_request               
ADD CONSTRAINT media_hearing_fk
FOREIGN KEY (hea_id) REFERENCES hearing(hea_id);

ALTER TABLE notification                
ADD CONSTRAINT notification_case_fk
FOREIGN KEY (cas_id) REFERENCES court_case(cas_id);

ALTER TABLE prosecutor_name                
ADD CONSTRAINT prosecutor_name_case_fk
FOREIGN KEY (cas_id) REFERENCES court_case(cas_id);

ALTER TABLE transcription               
ADD CONSTRAINT transcription_case_fk
FOREIGN KEY (cas_id) REFERENCES court_case(cas_id);

ALTER TABLE transcription               
ADD CONSTRAINT transcription_courtroom_fk
FOREIGN KEY (ctr_id) REFERENCES courtroom(ctr_id);

ALTER TABLE transcription               
ADD CONSTRAINT transcription_urgency_fk
FOREIGN KEY (urg_id) REFERENCES urgency(urg_id);

ALTER TABLE transcription               
ADD CONSTRAINT transcription_last_modified_by_fk
FOREIGN KEY (last_modified_by) REFERENCES user_account(usr_id);

ALTER TABLE transcription               
ADD CONSTRAINT transcription_requested_by_fk
FOREIGN KEY (requested_by) REFERENCES user_account(usr_id);

ALTER TABLE transcription               
ADD CONSTRAINT transcription_approved_by_fk
FOREIGN KEY (approved_by) REFERENCES user_account(usr_id);

ALTER TABLE transcription               
ADD CONSTRAINT transcription_transcribed_by_fk
FOREIGN KEY (transcribed_by) REFERENCES user_account(usr_id);

ALTER TABLE transcription               
ADD CONSTRAINT transcription_transcription_type_fk
FOREIGN KEY (trt_id) REFERENCES transcription_type(trt_id);

ALTER TABLE transcription_comment       
ADD CONSTRAINT transcription_comment_transcription_fk
FOREIGN KEY (tra_id) REFERENCES transcription(tra_id);

ALTER TABLE transcription_comment       
ADD CONSTRAINT transcription_comment_author_fk
FOREIGN KEY (author) REFERENCES user_account(usr_id);

ALTER TABLE transient_object_directory  
ADD CONSTRAINT tod_modified_by_fk
FOREIGN KEY (last_modified_by) REFERENCES user_account(usr_id);

ALTER TABLE transient_object_directory  
ADD CONSTRAINT tod_media_request_fk
FOREIGN KEY (mer_id) REFERENCES media_request(mer_id);

ALTER TABLE transient_object_directory  
ADD CONSTRAINT tod_object_directory_status_fk
FOREIGN KEY (ods_id) REFERENCES object_directory_status(ods_id);

-- additional unique multi-column indexes and constraints

--,UNIQUE (cth_id,courtroom_name)
CREATE UNIQUE INDEX ctr_chr_crn_unq ON courtroom( cth_id, courtroom_name) TABLESPACE darts_indexes;
ALTER TABLE courtroom ADD UNIQUE USING INDEX ctr_chr_crn_unq;

--,UNIQUE(cas_id,ctr_id,c_hearing_date)
CREATE UNIQUE INDEX hea_cas_ctr_hd_unq ON hearing( cas_id, ctr_id,hearing_date) TABLESPACE darts_indexes;
ALTER TABLE hearing ADD UNIQUE USING INDEX hea_cas_ctr_hd_unq;

--,UNIQUE(cth_id, case_number)
CREATE UNIQUE INDEX cas_case_number_cth_id_unq ON court_case(case_number,cth_id) TABLESPACE darts_indexes;
ALTER TABLE court_case ADD UNIQUE USING INDEX cas_case_number_cth_id_unq;


GRANT SELECT,INSERT,UPDATE,DELETE ON annotation TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON case_retention TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON case_retention_event TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON court_case TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON courthouse TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON courthouse_region_ae TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON courtroom TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON daily_list TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON defence_name TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON defendant_name TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON device_register TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON event TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON event_handler TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON external_location_type TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON external_object_directory TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON hearing TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON hearing_event_ae TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON hearing_media_ae TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON judge_name TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON media TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON media_request TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON notification TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON object_directory_status TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON prosecutor_name TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON region TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON report TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON retention_policy TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON transcription TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON transcription_comment TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON transcription_type TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON transient_object_directory TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON urgency TO darts_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON user_account TO darts_user;

GRANT SELECT,UPDATE ON  ann_seq TO darts_user;
GRANT SELECT,UPDATE ON  car_seq TO darts_user;
GRANT SELECT,UPDATE ON  cas_seq TO darts_user;
GRANT SELECT,UPDATE ON  cra_seq TO darts_user;
GRANT SELECT,UPDATE ON  cre_seq TO darts_user;
GRANT SELECT,UPDATE ON  cth_seq TO darts_user;
GRANT SELECT,UPDATE ON  ctr_seq TO darts_user;
GRANT SELECT,UPDATE ON  dal_seq TO darts_user;
GRANT SELECT,UPDATE ON  dfc_seq TO darts_user;
GRANT SELECT,UPDATE ON  dfd_seq TO darts_user;
GRANT SELECT,UPDATE ON  der_seq TO darts_user;
GRANT SELECT,UPDATE ON  elt_seq TO darts_user;
GRANT SELECT,UPDATE ON  eod_seq TO darts_user;
GRANT SELECT,UPDATE ON  eve_seq TO darts_user;
GRANT SELECT,UPDATE ON  evh_seq TO darts_user;
GRANT SELECT,UPDATE ON  hea_seq TO darts_user;
GRANT SELECT,UPDATE ON  jud_seq TO darts_user;
GRANT SELECT,UPDATE ON  med_seq TO darts_user;
GRANT SELECT,UPDATE ON  mer_seq TO darts_user;
GRANT SELECT,UPDATE ON  not_seq TO darts_user;
GRANT SELECT,UPDATE ON  ods_seq TO darts_user;
GRANT SELECT,UPDATE ON  prn_seq TO darts_user;
GRANT SELECT,UPDATE ON  reg_seq TO darts_user;
GRANT SELECT,UPDATE ON  rep_seq TO darts_user;
GRANT SELECT,UPDATE ON  rtp_seq TO darts_user;
GRANT SELECT,UPDATE ON  tod_seq TO darts_user;
GRANT SELECT,UPDATE ON  tra_seq TO darts_user;
GRANT SELECT,UPDATE ON  trc_seq TO darts_user;
GRANT SELECT,UPDATE ON  trt_seq TO darts_user;
GRANT SELECT,UPDATE ON  urg_seq TO darts_user;
GRANT SELECT,UPDATE ON  usr_seq TO darts_user;

GRANT USAGE ON SCHEMA DARTS TO darts_user;

SET ROLE DARTS_USER;
SET SEARCH_PATH TO darts;


INSERT INTO object_directory_status (ods_id,ods_description) VALUES (nextval('ods_seq'),'New');
INSERT INTO object_directory_status (ods_id,ods_description) VALUES (nextval('ods_seq'),'Stored');
INSERT INTO object_directory_status (ods_id,ods_description) VALUES (nextval('ods_seq'),'Failure');
INSERT INTO object_directory_status (ods_id,ods_description) VALUES (nextval('ods_seq'),'Failure - File not found');
INSERT INTO object_directory_status (ods_id,ods_description) VALUES (nextval('ods_seq'),'Failure - File size check failed');
INSERT INTO object_directory_status (ods_id,ods_description) VALUES (nextval('ods_seq'),'Failure - File type check failed');
INSERT INTO object_directory_status (ods_id,ods_description) VALUES (nextval('ods_seq'),'Failure - Checksum failed');
INSERT INTO object_directory_status (ods_id,ods_description) VALUES (nextval('ods_seq'),'Failure - ARM ingestion failed');
INSERT INTO object_directory_status (ods_id,ods_description) VALUES (nextval('ods_seq'),'Awaiting Verification');
INSERT INTO object_directory_status (ods_id,ods_description) VALUES (nextval('ods_seq'),'marked for Deletion');
INSERT INTO object_directory_status (ods_id,ods_description) VALUES (nextval('ods_seq'),'Deleted');








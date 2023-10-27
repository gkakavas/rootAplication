
--create table Groups

CREATE TABLE IF NOT EXISTS public.groups
(
    group_creation_date timestamp(6) without time zone,
    group_creator uuid,
    group_id uuid NOT NULL,
    group_name character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT groups_pkey PRIMARY KEY (group_id)
)

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.groups
    OWNER to "test";

--create table Users

CREATE TABLE IF NOT EXISTS public.users
(
    last_login timestamp(6) without time zone,
    register_date timestamp(6) without time zone,
    created_by uuid,
    group_id uuid,
    user_id uuid NOT NULL,
    current_project character varying(255) COLLATE pg_catalog."default",
    email character varying(255) COLLATE pg_catalog."default",
    firstname character varying(255) COLLATE pg_catalog."default",
    lastname character varying(255) COLLATE pg_catalog."default",
    password character varying(255) COLLATE pg_catalog."default",
    role_value character varying(255) COLLATE pg_catalog."default",
    specialization character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT users_pkey PRIMARY KEY (user_id),
    CONSTRAINT users_email_key UNIQUE (email),
    CONSTRAINT group_fkey FOREIGN KEY (group_id)
        REFERENCES public.groups (group_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.users
    OWNER to "test";

--create table Events

CREATE TABLE IF NOT EXISTS public.events
(
    event_date_time timestamp(6) without time zone,
    event_expiration timestamp(6) without time zone,
    event_creator uuid,
    event_id uuid NOT NULL,
    event_body character varying(255) COLLATE pg_catalog."default",
    event_description character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT events_pkey PRIMARY KEY (event_id)
)

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.events
    OWNER to "test";

--create table Users_Events_Mapping

CREATE TABLE IF NOT EXISTS public.user_event_mapping
(
    event_id uuid NOT NULL,
    user_id uuid NOT NULL,
    CONSTRAINT user_event_mapping_pkey PRIMARY KEY (event_id, user_id),
    CONSTRAINT event_id_fkey FOREIGN KEY (event_id)
        REFERENCES public.events (event_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT user_id_fkey FOREIGN KEY (user_id)
        REFERENCES public.users (user_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.user_event_mapping
    OWNER to "test";

--create table Leaves

CREATE TABLE IF NOT EXISTS public.leaves
(
    approved boolean,
    leave_ends date,
    leave_starts date,
    approved_on timestamp(6) without time zone,
    approved_by uuid,
    leave_id uuid NOT NULL,
    user_id uuid,
    leave_type character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT leaves_pkey PRIMARY KEY (leave_id),
    CONSTRAINT user_id_fkey FOREIGN KEY (user_id)
        REFERENCES public.users (user_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT leaves_leave_type_check CHECK (leave_type::text = ANY (ARRAY['HOLIDAY'::character varying, 'SICK_LEAVE'::character varying, 'MATERNITY_LEAVE'::character varying, 'BRIDGE_DAY_LEAVE'::character varying]::text[]))
)

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.leaves
    OWNER to "test";

--create table Files

CREATE TABLE IF NOT EXISTS public.files
(
    approved boolean,
    approved_date timestamp(6) without time zone,
    file_size bigint,
    upload_date timestamp(6) without time zone,
    approved_by uuid,
    file_id uuid NOT NULL,
    user_id uuid,
    access_url character varying(255) COLLATE pg_catalog."default",
    file_kind character varying(255) COLLATE pg_catalog."default",
    file_type character varying(255) COLLATE pg_catalog."default",
    filename character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT files_pkey PRIMARY KEY (file_id),
    CONSTRAINT user_id_fkey FOREIGN KEY (user_id)
        REFERENCES public.users (user_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT files_file_kind_check CHECK (file_kind::text = ANY (ARRAY['EVALUATION'::character varying, 'TIMESHEET'::character varying]::text[]))
)

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.files
    OWNER to "test";
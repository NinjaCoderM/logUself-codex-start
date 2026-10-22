CREATE TABLE projects (
    id uuid PRIMARY KEY,
    name text NOT NULL
);
CREATE TABLE entry_types (
    id uuid PRIMARY KEY,
    name text NOT NULL,
    definition_version integer NOT NULL CHECK (definition_version > 0),
    definition jsonb NOT NULL
);
CREATE TABLE tickets (
    id uuid PRIMARY KEY,
    project_id uuid NOT NULL REFERENCES projects(id),
    entry_type_id uuid NOT NULL REFERENCES entry_types(id),
    definition_version integer NOT NULL DEFAULT 1 CHECK (definition_version > 0),
    title text NOT NULL CHECK (length(trim(title)) > 0),
    description text NOT NULL DEFAULT '',
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE records (
    id uuid PRIMARY KEY,
    ticket_id uuid NOT NULL REFERENCES tickets(id),
    definition_version integer NOT NULL,
    recorded_on date NOT NULL,
    duration_minutes integer NOT NULL CHECK (duration_minutes > 0),
    description text NOT NULL DEFAULT '',
    fields jsonb NOT NULL,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX records_ticket_date_idx ON records(ticket_id, recorded_on);
CREATE INDEX tickets_project_idx ON tickets(project_id);

INSERT INTO projects VALUES ('00000000-0000-0000-0000-000000000001', 'PrivatMartin');
INSERT INTO entry_types VALUES (
    '00000000-0000-0000-0000-000000000002', 'Sport', 1,
    '{"ticketFields":[],"recordFields":[{"key":"sport","label":"Sportart","type":"text","required":true},{"key":"durationMinutes","label":"Dauer","type":"integer","required":true,"minimum":1,"storage":"duration_minutes","unit":"min","aggregation":"SUMME"}],"action":"Zeit erfassen","usesStatus":false}'
);

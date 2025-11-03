CREATE TABLE promotion (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    status TEXT NOT NULL,
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    version INT,
    created_by TEXT,
    reviewed_by TEXT,
    published_by TEXT,
    template_id UUID
);

CREATE TABLE promotion_scope (
    scope_id UUID PRIMARY KEY,
    promotion UUID NOT NULL ,
    scope_type TEXT
);

CREATE TABLE scope_member (
    member_id UUID PRIMARY KEY,
    scope_id UUID NOT NULL,
    external_id TEXT
);

CREATE TABLE day_template (
  id UUID PRIMARY KEY,
  name TEXT NOT NULL,
  description TEXT,
  rule_json TEXT,
  created_by TEXT,
  created_at TIMESTAMP
);
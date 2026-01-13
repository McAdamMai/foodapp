CREATE TABLE time_slice (
                            id UUID PRIMARY KEY,
                            promotion_id UUID NOT NULL,
                            version INT NOT NULL,
                            slice_date DATE NOT NULL,
                            start_time TIMESTAMP WITH TIME ZONE NOT NULL,
                            end_time TIMESTAMP WITH TIME ZONE NOT NULL,
                            timezone TEXT NOT NULL,
                            effect_type TEXT NOT NULL,
                            effect_value DOUBLE PRECISION NOT NULL
);

CREATE TABLE expander_tracker (
                                  promotion_id UUID PRIMARY KEY,
                                  last_processed_version INT NOT NULL,
                                  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- CRITICAL: We almost always query/delete by promotion_id
CREATE INDEX idx_time_slice_promotion_id ON time_slice(promotion_id);


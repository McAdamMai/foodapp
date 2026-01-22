package com.foodapp.promotion_expander.infra.persistence.typehandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodapp.promotion_expander.domain.model.PromotionRules;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PromotionRulesTypeHandler extends BaseTypeHandler<PromotionRules> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, PromotionRules parameter, JdbcType jdbcType) throws SQLException {
        try {
            PGobject jsonObject = new PGobject();
            jsonObject.setType("jsonb");
            jsonObject.setValue(mapper.writeValueAsString(parameter));
            ps.setObject(i, jsonObject);
        } catch (Exception e) {
            throw new SQLException("Error converting PromotionRules to JSON", e);
        }
    }

    @Override
    public PromotionRules getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseJson(rs.getString(columnName));
    }

    @Override
    public PromotionRules getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseJson(rs.getString(columnIndex));
    }

    @Override
    public PromotionRules getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseJson(cs.getString(columnIndex));
    }

    private PromotionRules parseJson(String json) throws SQLException {
        if (json == null) return null;
        try {
            return mapper.readValue(json, PromotionRules.class);
        } catch (Exception e) {
            throw new SQLException("Error converting JSON to PromotionRules", e);
        }
    }
}
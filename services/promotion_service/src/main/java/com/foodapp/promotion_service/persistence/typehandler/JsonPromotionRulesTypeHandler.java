package com.foodapp.promotion_service.persistence.typehandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Important for LocalTime
import com.foodapp.promotion_service.domain.model.PromotionRules;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JsonPromotionRulesTypeHandler extends BaseTypeHandler<PromotionRules> {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()); // Registers support for LocalTime/LocalDate

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, PromotionRules parameter, JdbcType jdbcType) throws SQLException {
        try {
            PGobject jsonObject = new PGobject();
            jsonObject.setType("jsonb");
            jsonObject.setValue(mapper.writeValueAsString(parameter));
            ps.setObject(i, jsonObject);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error converting PromotionRules to JSON", e);
        }
    }

    @Override
    public PromotionRules getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public PromotionRules getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public PromotionRules getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    private PromotionRules parse(String json) throws SQLException {
        if (json == null || json.isEmpty()) return null;
        try {
            return mapper.readValue(json, PromotionRules.class);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error parsing JSON to PromotionRules", e);
        }
    }
}
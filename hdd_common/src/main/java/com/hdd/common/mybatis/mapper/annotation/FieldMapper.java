package com.hdd.common.mybatis.mapper.annotation;

import org.apache.ibatis.type.JdbcType;

/**
 * 字段映射类，用于描述java对象字段和数据库表字段之间的对应关系
 * 
 * @author david
 * 
 */
public class FieldMapper {
    /**
     * Java对象字段名
     */
    private String fieldName;
    /**
     * 数据库表字段名
     */
    private String dbFieldName;
    /**
     * 数据库字段对应的jdbc类型
     */
    private JdbcType jdbcType;

    public String getDbFieldName() {
        return dbFieldName;
    }

    public void setDbFieldName(String dbFieldName) {
        this.dbFieldName = dbFieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public JdbcType getJdbcType() {
        return jdbcType;
    }

    public void setJdbcType(JdbcType jdbcType) {
        this.jdbcType = jdbcType;
    }
}

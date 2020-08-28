package com.aegis.mybatis.xmlless.config

import com.aegis.jackson.createObjectMapper
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet


open class TmpHandler(val type: JavaType? = null) : BaseTypeHandler<Any>() {

  val mapper = createObjectMapper()

  override fun getNullableResult(rs: ResultSet, column: String): Any? {
    val columnValue = rs.getString(column)
    return if (columnValue == null) {
      null
    } else {
      toTypeValue(columnValue)
    }
  }

  override fun getNullableResult(rs: ResultSet, columnIndex: Int): Any? {
    val str = rs.getString(columnIndex)
    return toTypeValue(str)
  }

  override fun getNullableResult(cs: CallableStatement, columnIndex: Int): Any? {
    val str = cs.getString(columnIndex)
    return toTypeValue(str)
  }

  override fun setNonNullParameter(ps: PreparedStatement, i: Int, parameter: Any?, jdbcType: JdbcType?) {
    ps.setString(i, mapper.writeValueAsString(parameter))
  }

  private fun toTypeValue(str: String): Any? = mapper.readValue(str, type)

}

package com.aegis.mybatis.xmlless.config

import com.aegis.jackson.createObjectMapper
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter


open class TmpHandler(val type: JavaType? = null) : BaseTypeHandler<Any>() {

  val mapper = createObjectMapper()

  init {
    val javaTimeModule = JavaTimeModule()
    javaTimeModule.addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
    javaTimeModule.addSerializer(LocalDate::class.java, LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
    javaTimeModule.addSerializer(LocalTime::class.java, LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")))
    mapper.registerModule(javaTimeModule)
  }

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

  private fun toTypeValue(str: String): Any? {
    return mapper.readValue(str, type)
  }

}

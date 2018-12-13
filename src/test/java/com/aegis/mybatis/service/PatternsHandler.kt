package com.aegis.mybatis.service

import com.aegis.jackson.createObjectMapper
import com.aegis.mybatis.bean.Patterns
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Created by wuhao on 2017/5/21.
 */
class PatternsHandler : BaseTypeHandler<Patterns>() {

  private val mapper = createObjectMapper()

  @Throws(SQLException::class)
  override fun getNullableResult(resultSet: ResultSet, s: String): Patterns? {
    return toPatterns(resultSet.getString(s))
  }

  @Throws(SQLException::class)
  override fun getNullableResult(resultSet: ResultSet, i: Int): Patterns? {
    val str = resultSet.getString(i)
    return toPatterns(str)
  }

  @Throws(SQLException::class)
  override fun getNullableResult(callableStatement: CallableStatement, i: Int): Patterns? {
    val str = callableStatement.getString(i)
    return toPatterns(str)
  }

  @Throws(SQLException::class)
  override fun setNonNullParameter(preparedStatement: PreparedStatement,
                                   i: Int,
                                   patterns: Patterns,
                                   jdbcType: JdbcType?) {
    preparedStatement.setString(i, mapper.writeValueAsString(patterns))
  }

  private fun toPatterns(str: String): Patterns {
    return mapper.readValue(str, Patterns::class.java)
  }

}

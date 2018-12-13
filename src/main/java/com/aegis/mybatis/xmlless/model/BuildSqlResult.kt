package com.aegis.mybatis.xmlless.model


/**
 * sql构建结果，sql属性为null表示构建失败
 * @author 吴昊
 * @since 0.0.1
 */
data class BuildSqlResult(val sql: String?, val reasons: List<String> = listOf()) {

  companion object {
    fun empty(): BuildSqlResult {
      return BuildSqlResult("")
    }
  }

  constructor(sql: String?, reason: String) : this(
      sql, listOf(reason)
  )

  fun invalid(): Boolean {
    return !valid()
  }

  private fun valid(): Boolean {
    return sql != null && reasons.isEmpty()
  }

}

package com.aegis.mybatis.xmlless.model


/**
 * 查询操作的返回字段
 */
data class Properties(
    /** 指定包含的持久化属性 */
    val includes: List<String> = listOf(),
    /** 指定排除的持久化属性 */
    val excludes: List<String> = listOf(),
    /** 指定排除更新的持久化属性 */
    val updateExcludeProperties: List<String> = listOf(),
    /** 属性到指定sql表达式的映射 */
    val propertiesMapping: Map<String, String> = mapOf()
) {

  fun isNotEmpty(): Boolean {
    return includes.isNotEmpty() || excludes.isNotEmpty()
  }

  fun isIncludeEmpty(): Boolean {
    return includes.isEmpty()
  }

  operator fun contains(property: String): Boolean {
    return property in includes && property !in excludes
  }

  fun isIncludeNotEmpty(): Boolean {
    return includes.isNotEmpty()
  }

}

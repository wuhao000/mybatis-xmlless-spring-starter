package com.aegis.mybatis.xmlless.model

data class Properties(
    val includes: List<String> = listOf(),
    val excludes: List<String> = listOf(),
    val updateExcludeProperties: List<String> = listOf()
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

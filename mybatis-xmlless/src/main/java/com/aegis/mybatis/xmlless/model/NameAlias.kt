package com.aegis.mybatis.xmlless.model


/**
 * Created by 吴昊 on 2018/12/18.
 */
open class NameAlias(val name: String, val alias: String) {

  companion object {
    fun resolve(target: String, aliasPrefix: String? = null): NameAlias {
      val splits = target.split("\\s+".toRegex())
      val name = splits[0]
      val alias = when (splits.size) {
        3    -> {
          if (splits[1].toUpperCase() != "AS") {
            throw IllegalStateException("无法识别的join表名称: $target")
          }
          splits[2]
        }
        2    -> splits[1]
        1    -> when {
          aliasPrefix != null -> aliasPrefix + name
          else                -> name
        }
        else -> throw IllegalStateException("无法识别的join表名称: $target")
      }
      return NameAlias(name, alias)
    }
  }

  /**
   * 表声明完整表达式
   */
  fun toSql(): String {
    return "$name AS $alias"
  }

}

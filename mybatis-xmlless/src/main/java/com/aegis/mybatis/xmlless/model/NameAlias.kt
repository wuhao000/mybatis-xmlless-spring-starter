package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.model.component.ISqlPart
import java.util.*


/**
 * Created by 吴昊 on 2018/12/18.
 */
open class NameAlias(
    val name: String,
    alias: String
) : ISqlPart {

  val alias: String = alias.replace('.', '_')

  companion object {
    fun resolve(target: String, aliasPrefix: String? = null): NameAlias {
      val splits = target.split("\\s+".toRegex())
      val name = splits[0]
      val alias = when (splits.size) {
        3    -> {
          if (splits[1].uppercase(Locale.getDefault()) != "AS") {
            throw IllegalStateException("无法识别的join表名称: $target")
          }
          splits[2]
        }

        2    -> splits[1]
        1    -> when {
          aliasPrefix != null -> aliasPrefix + name
          else                -> name
        }

        else -> error("无法识别的join表名称: $target")
      }
      return NameAlias(name, alias.replace('.', '_'))
    }
  }

  /**
   * 表声明完整表达式
   */
  override fun toSql(): String {
    if (alias.isNotBlank()) {
      return "$name AS $alias"
    }
    return name
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is NameAlias) return false

    if (name != other.name) return false
    if (alias != other.alias) return false

    return true
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + alias.hashCode()
    return result
  }


}

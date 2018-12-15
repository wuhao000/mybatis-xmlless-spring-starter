package com.aegis.mybatis.xmlless.enums

import com.aegis.mybatis.xmlless.constant.IN_TEMPLATE
import com.aegis.mybatis.xmlless.constant.NOT_NULL_OR_IS_NULL
import com.aegis.mybatis.xmlless.kotlin.toWords
import com.aegis.mybatis.xmlless.methods.XmlLessMethods

/**
 * 数据库支持的操作符
 * @author 吴昊
 * @since 0.0.1
 */
enum class Operations(val operator: String) {

  Eq("="),
  EqDefault("="),
  Gt(">"),
  Gte(">="),
  In("IN"),
  IsNotNull("IS NOT NULL"),
  IsNull("IS NULL"),
  Like("LIKE"),
  LikeLeft("LIKE"),
  LikeRight("LIKE"),
  Lt("<"),
  Lte("<="),
  Ne("!="),
  NotNull("IS NOT NULL");

  companion object {
    fun nameWords(): List<List<String>> {
      return names().map { it.toWords() }
    }

    fun valueOf(words: List<String>): Operations? {
      val name = words.joinToString("")
      return values().firstOrNull {
        it.name == name
      }
    }

    private fun names(): List<String> {
      return Operations.values().map { it.name }
    }
  }

  fun getTemplate(value: Boolean = false): String {
    val valueHolder = when {
      value -> "%s"
      else  -> "${XmlLessMethods.PROPERTY_PREFIX}%s${XmlLessMethods.PROPERTY_SUFFIX}"
    }
    return when (this) {
      Like                          -> "%s %s CONCAT('%%', $valueHolder,'%%')"
      LikeLeft                      -> "%s %s CONCAT($valueHolder, '%%')"
      LikeRight                     -> "%s %s CONCAT('%%', $valueHolder)"
      In                            -> IN_TEMPLATE
      in listOf(NotNull, IsNotNull) -> NOT_NULL_OR_IS_NULL
      IsNull                        -> NOT_NULL_OR_IS_NULL
      else                          -> "%s %s $valueHolder"
    }
  }

  fun getValueTemplate(): String {
    return getTemplate(true)
  }

}

package com.aegis.mybatis.xmlless.enums

import com.aegis.kotlin.toWords
import com.aegis.mybatis.xmlless.constant.IN_TEMPLATE
import com.aegis.mybatis.xmlless.constant.NO_VALUE
import com.aegis.mybatis.xmlless.constant.PROPERTY_PREFIX
import com.aegis.mybatis.xmlless.constant.PROPERTY_SUFFIX

/**
 * 数据库支持的操作符
 * @author 吴昊
 * @since 0.0.1
 */
enum class Operations(
    val operator: String,
    val parameterCount: Int
) {

  Between("BETWEEN", 2),
  Eq("=", 1),
  EqDate("=", 1),
  EqDefault("=", 0),
  EqFalse("= FALSE", 0),
  EqMonth("=", 1),
  EqTrue("= TRUE", 0),
  Gt("&gt;", 1),
  GtDate("&gt;", 1),
  Gte("&gt;=", 1),
  GteDate("&gt;=", 1),
  In("IN", 1),
  IsNotNull("IS NOT NULL", 0),
  IsNull("IS NULL", 0),
  Like("LIKE", 1),
  LikeLeft("LIKE", 1),
  LikeRight("LIKE", 1),
  Lt("&lt;", 1),
  LtDate("&lt;", 1),
  LteDate("&lt;", 1),
  Lte("&lt;=", 1),
  NeDate("!=", 1),
  Ne("!=", 1),
  NotNull("IS NOT NULL", 0);

  companion object {
    fun nameWords(): List<List<String>> {
      return names().map { it.toWords() }
    }

    fun valueOf(words: List<String>): Operations? {
      val name = words.joinToString("")
      return entries.firstOrNull {
        it.name == name
      }
    }

    private fun names(): List<String> {
      return entries.map { it.name }
    }
  }

  fun getTemplate(value: Boolean = false): String {
    val valueHolder = when {
      value -> "%s"
      else  -> "${PROPERTY_PREFIX}%s${PROPERTY_SUFFIX}"
    }
    return when (this) {
      LtDate, LteDate, NeDate, GtDate, GteDate,
      EqDate    -> "AND date_format(%s, '%%Y-%%m-%%d') %s date_format($valueHolder, '%%Y-%%m-%%d')"
      EqMonth   -> "AND date_format(%s, '%%Y-%%m') %s date_format($valueHolder, '%%Y-%%m')"
      Like      -> "%s %s CONCAT('%%', $valueHolder,'%%')"
      LikeLeft  -> "%s %s CONCAT($valueHolder, '%%')"
      LikeRight -> "%s %s CONCAT('%%', $valueHolder)"
      In        -> IN_TEMPLATE
      in listOf(
          NotNull, IsNotNull,
          IsNull, EqTrue, EqFalse
      )         -> NO_VALUE

      Between   -> "%s %s $valueHolder AND $valueHolder"
      else      -> "%s %s $valueHolder"
    }
  }

  fun getValueTemplate(): String {
    return getTemplate(true)
  }

}

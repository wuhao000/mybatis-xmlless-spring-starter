package com.aegis.mybatis.xmlless.constant

import kotlin.reflect.full.memberProperties

/**
 *
 * Created by 吴昊 on 2018/12/19.
 *
 * @author 吴昊
 * @since 1.4.3
 */
object SQLKeywords {

  const val ADD = "ADD"
  const val ALTER = "ALTER"
  const val AND = "AND"
  const val AS = "AS"
  const val BACKUP = "BACKUP"
  const val BETWEEN = "BETWEEN"
  const val BY = "BY"
  const val CASE = "CASE"
  const val COMMENT = "COMMENT"
  const val DEFAULT = "DEFAULT"
  const val DESC = "DESC"
  const val EXISTS = "EXISTS"
  const val FROM = "FROM"
  const val GROUP = "GROUP"
  const val INNER = "INNER"
  const val JOIN = "JOIN"
  const val KEY = "KEY"
  const val LEFT = "LEFT"
  const val LIMIT = "LIMIT"
  const val NEW = "NEW"
  const val OR = "OR"
  const val ORDER = "ORDER"
  const val PRIMARY = "PRIMARY"
  const val RIGHT = "RIGHT"
  const val ROW = "ROW"
  const val SELECT = "SELECT"
  const val SET = "SET"
  const val SHARE = "SHARE"
  const val TABLE = "TABLE"
  const val TEXT = "TEXT"
  const val TIME = "TIME"
  const val USER = "USER"
  const val VIEW = "VIEW"
  const val WHEN = "WHEN"
  const val WHERE = "WHERE"
  fun getValues(): List<String> {
    return SQLKeywords::class.memberProperties.map {
      it.call().toString()
    }
  }

}

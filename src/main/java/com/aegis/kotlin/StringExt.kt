/**
 *
 * Created by 吴昊 on 2018/6/25.
 *
 * @author 吴昊
 * @since
 */
@file:Suppress("unused")

package com.aegis.kotlin

import java.util.*

/**
 * 判断字符串是否以给定的字符串结尾，如果不是则将给定字符串添加到当前字符串之后返回新的字符串，反之则返回当前字符串
 */
fun String.appendIfNotEndWith(suffix: String): String {
  return if (this.endsWith(suffix)) {
    this
  } else {
    this + suffix
  }
}

/**
 * 判断字符串是否包含任一字符串
 * @param other 可能包含的字符串集合
 * @return 如果包含集合中的一个字符串返回true，反之false
 */
fun String.containsAny(vararg other: CharSequence): Boolean {
  return other.any { this.contains(it) }
}

/**
 * 是否以某个字符串结束
 * @param suffixes 可能匹配的后缀字符串
 * @return 存在匹配的后缀返回true，反之false
 */
fun String.endsWithAny(vararg suffixes: String): Boolean {
  return suffixes.any { this.endsWith(it) }
}

/**
 * 是否以某个字符串结束
 * @param suffixes 可能匹配的后缀字符串
 * @return 存在匹配的后缀返回true，反之false
 */
infix fun String.endsWithAny(suffixes: Collection<String>): Boolean {
  return suffixes.any { this.endsWith(it) }
}

/**
 * 获取字符串中形如 ${name} 的占位符
 * @return 占位符名称列表
 */
fun String.getHolderNames(): List<String> {
  val pattern = "\\\$\\{(.*?)}".toRegex()
  val matches = pattern.findAll(this)
  val holders = arrayListOf<String>()
  matches.forEach {
    val paramName = it.groups[1]!!.value
    holders.add(paramName)
  }
  return holders
}

/**
 * 是否以某个字符串开头
 * @param prefixes 可能匹配的前缀字符串
 * @return 存在匹配的前缀返回true，反之false
 */
fun String.startsWithAny(vararg prefixes: String): Boolean {
  return prefixes.any { this.startsWith(it) }
}

/**
 * 是否以某个字符串开头
 * @param prefixes 可能匹配的前缀字符串
 * @return 存在匹配的前缀返回true，反之false
 */
infix fun String.startsWithAny(prefixes: Collection<String>): Boolean {
  return prefixes.any { this.startsWith(it) }
}

/**
 * 将字符串转化为驼峰命名
 * @return 转化为驼峰命名的字符串
 */
fun String.toCamelCase(): String {
  return if (!this.isBlank()) {
    val words = toWords().toMutableList()
    words[0] = words[0].toLowerCase()
    words.joinToString("")
  } else {
    ""
  }
}

/**
 * 将字符串转化为常量命名
 * @return 转化为常量命名的字符串
 */
fun String.toConstantCase(): String {
  return toWords().joinToString("_") { it.toUpperCase() }
}

/**
 * 将字符串转化为中划线命名
 * @return 转化为中划线命名的字符串
 */
fun String.toDashCase(): String {
  return toWords().joinToString("-") { it.toLowerCase() }
}

/**
 * 将字符串转化为帕斯卡命名
 * @return 转化为帕斯卡命名的字符串
 */
fun String.toPascalCase(): String {
  return toWords().joinToString("")
}

/**
 * 将字符串转化为下划线命名
 * @return 转化为下划线命名的字符串
 */
fun String.toUnderlineCase(): String {
  return toConstantCase().toUpperCase()
}

/**
 * 获取当前字符串里的单词，将每个单词的首字母大写
 * @return 字符串里包含的单词列表
 */
fun String.toWords(): List<String> {
  return when {
    this.contains("[-_\\s]".toRegex()) -> this.split("[-_\\s]".toRegex()).map {
      if (it.length == 1) {
        it.toUpperCase()
      } else {
        it.substring(0, 1).toUpperCase() + it.substring(1).toLowerCase()
      }
    }
    this.isBlank()                     -> listOf()
    else                               -> {
      val result = ArrayList<String>()
      var wordCount = 0
      var upperCaseContinue = false
      var tmpWord = ""
      this.forEachIndexed { index, char ->
        if (char == ' ') {
          upperCaseContinue = false
          if (tmpWord.isNotEmpty()) {
            result.add(tmpWord)
            tmpWord = ""
          }
        } else if (char in 'a'..'z' || char in '0'..'9') {
          if (index == 0) {
            wordCount++
          }
          tmpWord += char
          upperCaseContinue = false
        } else if (char in 'A'..'Z') {
          if (!upperCaseContinue) {
            wordCount++
            if (tmpWord.isNotEmpty()) {
              result.add(tmpWord)
            }
            tmpWord = "$char"
          } else {
            tmpWord += char
          }
          upperCaseContinue = true
        }
      }
      if (tmpWord.isNotEmpty()) {
        result.add(tmpWord)
      }
      result.map {
        if (it.length == 1) {
          it.toUpperCase()
        } else {
          it.substring(0, 1).toUpperCase() + it.substring(1).toLowerCase()
        }
      }
    }
  }
}

/**
 * 生成32位不含-的UUID
 *
 * @param upperCase 是否生成全部大写的UUID
 * @return
 */
fun uuid(upperCase: Boolean = false, removeDash: Boolean = true): String {
  var uuid = UUID.randomUUID().toString()
  if (upperCase) {
    uuid = uuid.toUpperCase()
  }
  if (removeDash) {
    uuid = uuid.replace("-", "")
  }
  return uuid
}

/**
 * 获取当前字符串包含指定字符串的个数
 * @param unit 包含的字符串
 * @return 包含的个数
 */
fun String.withinCount(unit: String): Int {
  return ((this.length - this.replace(unit, "").length) / unit.length)
}

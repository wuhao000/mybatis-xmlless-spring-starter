package com.aegis.mybatis.xmlless.kotlin

import com.aegis.kotlin.toPascalCase
import com.aegis.kotlin.toWords
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

/**
 * Created by 吴昊 on 2023/12/9.
 */
class StringExtKtTest {

  @Test
  fun toCamelCase() {
  }

  @Test
  fun toConstantCase() {
  }

  @Test
  fun toDashCase() {
  }

  @Test
  fun toPascalCase() {
    println("userName".toPascalCase())
    println("userNameLikeKeywords".toPascalCase())
  }

  @Test
  fun toUnderlineCase() {
  }

  @Test
  fun toWords() {
    "".toWords()
  }
}

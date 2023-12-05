package com.aegis.mybatis.xmlless.model.component

import com.aegis.mybatis.xmlless.enums.TestType

/**
 *
 * @author 吴昊
 * @date 2023/12/5 22:41
 * @since v4.0.0
 * @version 1.0
 */

class TestConditionDeclaration(
    private val parameter: String,
    private val type: TestType,
): ISqlPart {
  override fun toSql(): String {
    return parameter + " " + type.expression
  }

}

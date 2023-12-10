package com.aegis.mybatis.xmlless.annotations

import com.aegis.mybatis.xmlless.enums.TestType
import org.intellij.lang.annotations.Language


/**
 * sql查询中条件是否生效的判断条件，相当于if标签中的test属性
 *
 * Created by 吴昊 on 2018/12/18.
 * @param expression
 * @param value 常用的判断条件枚举，多个条件之间是并的关系
 */
@Target(allowedTargets = [
  AnnotationTarget.VALUE_PARAMETER,
  AnnotationTarget.PROPERTY,
  AnnotationTarget.FIELD
])
annotation class TestExpression(val value: Array<TestType> = [],
                                @Language("sql")
                                val expression: String = "")

package com.aegis.mybatis.xmlless.annotations

import com.aegis.mybatis.xmlless.enums.Operations
import org.intellij.lang.annotations.Language

/**
 *
 * Created by 吴昊 on 2018/12/21.
 *
 * @author 吴昊
 * @since 1.4.3
 * @param expression 条件表达式（不包含if）
 * @param operator 条件操作符
 * @param property 条件作用的持久化对象属性
 * @param test 条件生效的判断
 */
@Target(allowedTargets = [
  AnnotationTarget.VALUE_PARAMETER,
  AnnotationTarget.PROPERTY,
  AnnotationTarget.FIELD
])
annotation class Criteria(
    @Language("GenericSQL")
    val expression: String = "",
    val operator: Operations = Operations.EqDefault,
    val test: TestExpression = TestExpression(),
    val property: String = ""
)

package com.aegis.mybatis.xmlless.annotations

import com.aegis.mybatis.xmlless.enums.Operations

/**
 *
 * Created by 吴昊 on 2018/12/21.
 *
 * @author 吴昊
 * @since 1.4.3
 */
@Target(allowedTargets = [
  AnnotationTarget.VALUE_PARAMETER,
  AnnotationTarget.PROPERTY,
  AnnotationTarget.FIELD
])
annotation class Criteria(
    val expression: String = "",
    val operator: Operations = Operations.EqDefault,
    val test: TestExpression = TestExpression()
)

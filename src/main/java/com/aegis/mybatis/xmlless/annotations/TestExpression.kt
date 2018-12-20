package com.aegis.mybatis.xmlless.annotations

import com.aegis.mybatis.xmlless.enums.TestType


/**
 * Created by 吴昊 on 2018/12/18.
 */
@Target(allowedTargets = [
  AnnotationTarget.VALUE_PARAMETER,
  AnnotationTarget.PROPERTY,
  AnnotationTarget.FIELD
])
annotation class TestExpression(val expression: String = "",
                                val tests: Array<TestType> = [])

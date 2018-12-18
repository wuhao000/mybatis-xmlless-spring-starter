package com.aegis.mybatis.xmlless.annotations


/**
 * Created by 吴昊 on 2018/12/18.
 */
@Target(allowedTargets = [
  AnnotationTarget.VALUE_PARAMETER
])
annotation class ParameterTest(val expression: String)

package com.aegis.mybatis.xmlless.model

import java.lang.reflect.Field

data class MatchedParameter(
    val parameter: ParameterInfo?,
    val paramName: String,
    val property: Field? = null
)

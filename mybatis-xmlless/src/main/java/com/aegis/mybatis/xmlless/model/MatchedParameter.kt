package com.aegis.mybatis.xmlless.model

import java.lang.reflect.Field
import java.lang.reflect.Parameter

data class MatchedParameter(
    val parameter: Parameter,
    val paramName: String,
    val property: Field? = null)

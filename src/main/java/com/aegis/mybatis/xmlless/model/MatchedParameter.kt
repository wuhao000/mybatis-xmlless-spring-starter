package com.aegis.mybatis.xmlless.model

import kotlin.reflect.KParameter
import kotlin.reflect.KProperty

data class MatchedParameter(val parameter: KParameter,
                            val paramName: String,
                            val property: KProperty<*>? = null)
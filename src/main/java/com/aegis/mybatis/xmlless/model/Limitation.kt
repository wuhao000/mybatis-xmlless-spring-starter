package com.aegis.mybatis.xmlless.model


/**
 * sql查询的数量限制条件
 * @author 吴昊
 * @since 0.0.3
 */
data class Limitation(
    /**  limit的offset参数名称 */
    val offsetParam: String,
    /**  limit的size参数名称 */
    val sizeParam: String)

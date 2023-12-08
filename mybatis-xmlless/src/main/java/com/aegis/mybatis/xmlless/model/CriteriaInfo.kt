package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.enums.Operations
import com.aegis.mybatis.xmlless.enums.TestType

/**
 *
 * @author 吴昊
 * @date 2023/12/8 11:09
 * @since v4.0.0
 * @version 1.0
 */
data class CriteriaInfo(
    val property: String,
    val expression: String,
    val operator: Operations,
    val testInfo: TestInfo
)
/**
 *
 * @author 吴昊
 * @date 2023/12/8 11:09
 * @since v4.0.0
 * @version 1.0
 */
data class TestInfo(val value: Array<TestType>, val expression: String)

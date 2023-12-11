package com.aegis.mybatis.bean

/**
 * 学生信息统计
 *
 * @author 吴昊
 * @date 2023/12/11 9:54
 * @since v4.0.0
 * @version 1.0
 */
data class StudentStats(
    val grade: Int = 0,
    val count: Int = 0,
    val sumAge: Double = 0.0,
    val avgAge: Double = 0.0
)

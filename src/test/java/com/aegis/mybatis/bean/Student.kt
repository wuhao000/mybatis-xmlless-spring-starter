package com.aegis.mybatis.bean

import com.aegis.mybatis.xmlless.annotations.JoinObject
import com.baomidou.mybatisplus.annotation.TableField
import javax.persistence.Id

/**
 *
 * @author 吴昊
 * @since 0.0.4
 */
data class Student(
    @Id
    var id: String,
    var name: String,
    var phoneNumber: String,
    @TableField("sex")
    var gender: Int,
    @JoinObject(
        targetTable = "t_score",
        targetColumn = "student_id",
        joinProperty = "id",
        associationPrefix = "score_",
        selectColumns = ["score", "subject_id"]
    )
    var scores: List<Score> = listOf())

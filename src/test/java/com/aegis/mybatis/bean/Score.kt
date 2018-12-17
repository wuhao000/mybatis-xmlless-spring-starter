package com.aegis.mybatis.bean

import com.aegis.mybatis.xmlless.annotations.JoinObject


/**
 * 学生的成绩
 * @author 吴昊
 * @since 0.0.5
 */
@Suppress("unused")
class Score {

  var score: Int = 0
  var studentId: String = ""
  @JoinObject(
      selectProperties = ["id","name"],
      targetTable = "t_subject",
      targetColumn = "id",
      joinProperty = "subjectId",
      associationPrefix = "subject_"
  )
  var subject: Subject? = null
  var subjectId: Int = 0

}

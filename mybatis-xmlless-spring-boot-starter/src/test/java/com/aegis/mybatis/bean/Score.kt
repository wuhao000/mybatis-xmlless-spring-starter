package com.aegis.mybatis.bean

import com.aegis.mybatis.xmlless.annotations.JoinObject
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id


/**
 * 学生的成绩
 * @author 吴昊
 * @since 0.0.5
 */
@Suppress("unused")
@Entity
class Score {

  @Id
  @GeneratedValue
  var id: Int = 0
  var score: Int = 0
  var studentId: String = ""
  @JoinObject(
      selectProperties = ["id", "name"],
      targetTable = "t_subject",
      targetColumn = "id",
      joinProperty = "subjectId",
      associationPrefix = "subject_"
  )
  var subject: Subject? = null
  var subjectId: Int = 0


  constructor()

  constructor(score: Int, studentId: String, subjectId: Int) {
    this.score  = score
    this.studentId = studentId
    this.subjectId = subjectId
  }

  override fun toString(): String {
    return "Score(score=$score, studentId=${studentId}, subjectId=${subjectId})"
  }
}

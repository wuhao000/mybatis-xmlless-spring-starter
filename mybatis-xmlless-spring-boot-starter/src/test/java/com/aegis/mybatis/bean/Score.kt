package com.aegis.mybatis.bean

import com.aegis.mybatis.xmlless.annotations.JoinObject
import com.aegis.mybatis.xmlless.annotations.SelectedProperties
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id


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
      joinProperty = "subjectId",
      associationPrefix = "subject_"
  )
  @SelectedProperties(["id", "name"])
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

package com.aegis.mybatis.bean

import com.aegis.mybatis.xmlless.annotations.JoinObject
import com.aegis.mybatis.xmlless.annotations.ModifyIgnore
import com.baomidou.mybatisplus.annotation.TableField
import javax.persistence.Id

/**
 *
 * @author 吴昊
 * @since 0.0.4
 */
class Student() {

  @TableField("sex")
  var gender: Int = 1
  @Id
  var id: String = ""
  var name: String = ""
  var phoneNumber: String = ""
  @JoinObject(
      targetTable = "t_score",
      targetColumn = "student_id",
      joinProperty = "id",
      associationPrefix = "score_",
      selectColumns = ["score", "subject_id"]
  )
  @ModifyIgnore
  var scores: MutableList<Score>? = null

  constructor(id: String, name: String, phoneNumber: String, gender: Int)
      : this() {
    this.id = id
    this.name = name
    this.phoneNumber = phoneNumber
    this.gender = gender
  }

}

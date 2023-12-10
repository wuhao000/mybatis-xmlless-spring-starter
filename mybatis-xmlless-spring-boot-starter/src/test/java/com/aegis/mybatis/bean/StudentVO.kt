package com.aegis.mybatis.bean

import com.aegis.mybatis.xmlless.annotations.JoinEntityProperty

/**
 *
 * @author 吴昊
 * @date 2023/12/10 10:28
 * @since v4.0.0
 * @version 1.0
 */

class StudentVO {

  var id: String? = null

  var createUserId: Int? = null

  var updateUserId: Int? = null

  @JoinEntityProperty(
      entity = User::class,
      propertyMapTo = "name",
      joinOnThisProperty = "createUserId"
  )
  var createUserName: String? = null

  /** 当事人身份  */
  @JoinEntityProperty(
      entity = School::class,
      joinOnProperty = "studentId",
      propertyMapTo = "location",
      joinOnThisProperty = "id"
  )
  var schoolLocation: String? = null

  @JoinEntityProperty(
      entity = User::class,
      propertyMapTo = "name",
      joinOnThisProperty = "updateUserId"
  )
  var updateUserName: String? = null

  var schoolId: String? = null

  @JoinEntityProperty(
      entity = School::class,
      joinOnProperty = "id",
      propertyMapTo = "name",
      joinOnThisProperty = "schoolId"
  )
  var schoolName: String? = null
}

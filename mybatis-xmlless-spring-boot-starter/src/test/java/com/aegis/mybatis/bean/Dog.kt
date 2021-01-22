package com.aegis.mybatis.bean

import javax.persistence.GeneratedValue
import javax.persistence.Id

class Dog {

  @Id
  @GeneratedValue
  var id: Int = 0
  var name: String? = null

}

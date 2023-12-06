package com.aegis.mybatis.bean

import com.aegis.mybatis.xmlless.annotations.JsonMappingProperty
import com.baomidou.mybatisplus.annotation.TableLogic
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.CreatedDate
import java.util.*
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

class Dog {

  @CreatedDate
  var createTime: Date? = null
  @TableLogic
  var deleteFlag: Boolean = false
  @Id
  @GeneratedValue
  var id: Int = 0
  var name: String? = null

  @JsonMappingProperty
  var names: List<String>? = null

  @JsonMappingProperty
  var ages: List<Int>? = null

}

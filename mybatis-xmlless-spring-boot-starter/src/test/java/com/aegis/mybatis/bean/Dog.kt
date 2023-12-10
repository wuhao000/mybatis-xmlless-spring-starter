package com.aegis.mybatis.bean

import com.aegis.mybatis.xmlless.annotations.JsonMappingProperty
import com.baomidou.mybatisplus.annotation.TableLogic
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.springframework.data.annotation.CreatedDate
import java.util.*

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

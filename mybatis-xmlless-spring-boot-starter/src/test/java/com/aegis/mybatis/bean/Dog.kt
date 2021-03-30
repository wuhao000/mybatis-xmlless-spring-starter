package com.aegis.mybatis.bean

import com.baomidou.mybatisplus.annotation.TableLogic
import org.springframework.data.annotation.CreatedDate
import java.util.*
import javax.persistence.GeneratedValue
import javax.persistence.Id

class Dog {

  @CreatedDate
  var createTime: Date? = null
  @TableLogic
  var deleteFlag: Boolean = false
  @Id
  @GeneratedValue
  var id: Int = 0
  var name: String? = null

}

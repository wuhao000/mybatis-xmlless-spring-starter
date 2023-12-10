package com.aegis.mybatis.bean

import com.baomidou.mybatisplus.annotation.TableName
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

/**
 *
 * @author 吴昊
 * @date 2023/12/10 19:05
 * @since v4.0.0
 * @version 1.0
 */
@Entity
@TableName("xx")
class School: BaseEntity() {

  var name: String? = null
  var studentId: String? = null
  @Column(name = "wz")
  var location: String? = null

}

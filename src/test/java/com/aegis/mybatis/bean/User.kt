package com.aegis.mybatis.bean

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableLogic
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Transient


@Suppress("ALL")
object QUser {

  const val age = "age"
  const val id = "id"
  const val name = "name"

}

/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
@Entity
@Table(schema = "os")
data class User(
    @Id
    var id: Int? = null,
    @TableField()
    var name: String? = null,

    var age: Int? = null,
    @TableLogic
    var deleted: Boolean = false) {

  /**
   * 临时字段，忽略
   */
  @Transient
  var count: Int = 0

}

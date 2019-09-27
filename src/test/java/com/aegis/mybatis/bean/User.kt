@file:Suppress("unused")

package com.aegis.mybatis.bean

import com.aegis.mybatis.xmlless.annotations.InsertIgnore
import com.baomidou.mybatisplus.annotation.TableField
import javax.persistence.*

/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
@Suppress("unused")
@Entity
@Table(schema = "test")
data class User(
    @Id
    @TableField()
    @InsertIgnore
    @GeneratedValue
    var id: Int? = null,
    @TableField()
    var name: String? = null,
    var deleted: Boolean = false) {

  /**
   * 临时字段，忽略
   */
  @Transient
  var count: Int = 0

}

@Suppress("ALL")
object QUser {
  const val id = "id"
  const val name = "name"
  const val age = "age"
  const val deleted = "deleted"
}

@Suppress("ALL")
val quser = QUser

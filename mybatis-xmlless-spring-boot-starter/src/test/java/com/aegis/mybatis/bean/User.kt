@file:Suppress("unused")

package com.aegis.mybatis.bean

import com.aegis.mybatis.xmlless.annotations.InsertIgnore
import com.aegis.mybatis.xmlless.annotations.JoinObject
import com.aegis.mybatis.xmlless.annotations.JsonMappingProperty
import com.baomidou.mybatisplus.annotation.TableField
import jakarta.persistence.*

/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
@Table(name = "t_user")
@Entity
data class User(
    @Id
    @TableField()
    @InsertIgnore
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int? = null,
    @TableField()
    var name: String? = null,
    var deleted: Boolean = false
) {

  /**  临时字段，忽略 */
  @Transient
  @TableField(exist = false)
  var count: Int = 0

  @JsonMappingProperty
  var roles: List<Int> = listOf()

  @JoinObject(
      targetTable = "t_role",
      targetColumn = "id",
      joinProperty = "roles",
      associationPrefix = "role_",
      selectProperties = ["id", "name", "deps", "depList"]
  )
  var roleList: MutableList<Role> = arrayListOf()
}

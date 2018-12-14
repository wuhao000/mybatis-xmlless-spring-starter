package com.aegis.mybatis.bean

import com.aegis.mybatis.xmlless.annotations.InsertIgnore
import com.baomidou.mybatisplus.annotation.TableField
import javax.persistence.*

/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
@Entity
@Table(schema = "test")
data class User(
    @Id
    @TableField()
    @InsertIgnore
    @GeneratedValue
    var id: Long? = null,
    @TableField()
    var name: String? = null,
    var age: Int? = null,
    var deleted: Boolean = false) {

  /**
   * 临时字段，忽略
   */
  @Transient
  var count: Int = 0

}

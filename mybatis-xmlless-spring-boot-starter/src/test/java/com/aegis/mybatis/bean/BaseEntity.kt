package com.aegis.mybatis.bean

import com.baomidou.mybatisplus.annotation.TableLogic
import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.springframework.format.annotation.DateTimeFormat
import java.util.*

/**
 * 基础实体类
 *
 * @author 骆嘉民 2021/6/28
 * @version 0.0.1
 */
open class BaseEntity {

  /** 主键  */
  @Id
  @GeneratedValue(generator = "SNOWFLAKE")
  @Column(name = "id", updatable = false)
  var id: String? = null

}

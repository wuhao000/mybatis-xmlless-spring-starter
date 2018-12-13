package com.aegis.mybatis.bean

import com.baomidou.mybatisplus.annotation.TableField
import javax.persistence.Id

/**
 *
 * @author 吴昊
 * @since 0.0.4
 */
data class Student(
    @Id
    var id: String,
    var name: String,
    var phoneNumber: String,
    @TableField("sex")
    var gender: Int)

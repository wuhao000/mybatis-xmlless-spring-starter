package com.aegis.mybatis.bean

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

/**
 *
 * @author 吴昊
 * @date 2021/3/30 10:24 下午
 * @since 3.5.1
 * @version 1.0
 */
@Entity
data class Dep(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int = 0,
    var name: String = ""
)

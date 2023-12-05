package com.aegis.mybatis.bean

import jakarta.persistence.Entity
import jakarta.persistence.Id


/**
 *
 * Created by 吴昊 on 2018/12/17.
 *
 * @author 吴昊
 * @since 0.0.9
 */
@Entity
data class Subject(
    @Id
    var id: Int = 0,
    var name: String = ""
)

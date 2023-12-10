package com.aegis.mybatis.bean

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table


/**
 *
 * Created by 吴昊 on 2018/12/18.
 *
 * @author 吴昊
 * @since 0.0.9
 */
@Entity
@Table(name = "t_server_provider")
data class ServerProvider(
    @Id
    var id: Int = 0,
    var name: String = ""
)

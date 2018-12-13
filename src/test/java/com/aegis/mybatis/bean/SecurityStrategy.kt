package com.aegis.mybatis.bean

import com.aegis.mybatis.xmlless.annotations.Handler
import com.aegis.mybatis.xmlless.annotations.JoinProperty
import com.aegis.mybatis.xmlless.annotations.UpdateIgnore
import com.aegis.mybatis.service.PatternsHandler
import com.baomidou.mybatisplus.annotation.TableName
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Transient


/**
 *
 * Created by 吴昊 on 2018/12/6.
 *
 * @author 吴昊
 * @since 0.0.1-SNAPSHOT
 */
@Entity
@TableName(resultMap = "strategy")
data class SecurityStrategy(
    @Id
    var clientId: String? = null,
    var host: String? = null,
    var type: String? = null,
    @Handler(PatternsHandler::class)
    var includePatterns: Patterns? = null,
    @Handler(PatternsHandler::class)
    var excludePatterns: Patterns? = null,
    @UpdateIgnore
    val createTime: Date? = null,
    val creatorId: Int = 0,
    @JoinProperty(selectColumn = "secret", targetTable = "t_app",
        joinProperty = "clientId",
        targetColumn = "client_id")
    @Transient
    var clientSecret: String? = null
)

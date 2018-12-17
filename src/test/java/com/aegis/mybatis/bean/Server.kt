package com.aegis.mybatis.bean

import com.aegis.mybatis.xmlless.annotations.JoinObject
import javax.persistence.Entity


/**
 * Created by 吴昊 on 2018/12/17.
 */
@Entity
data class Server(var id: Int = 0,
                  var name: String = "",
                  var ip: String = "",
                  var parentId: Int? = null,
                  @JoinObject(
                      selectProperties = ["name", "ip"],
                      targetColumn = "id",
                      targetTable = "t_server AS parent_server",
                      associationPrefix = "parent_",
                      joinProperty = "parentId"
                  )
                  var parent: Server? = null)

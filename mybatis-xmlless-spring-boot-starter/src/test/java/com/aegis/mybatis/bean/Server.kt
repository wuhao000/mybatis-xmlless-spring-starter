package com.aegis.mybatis.bean

import com.aegis.mybatis.xmlless.annotations.JoinObject
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id


/**
 * Created by 吴昊 on 2018/12/17.
 */
@Entity
data class Server(
    @Id
    @GeneratedValue
    var id: Int = 0,
    var name: String = "",
    var ip: String = "",
    var parentId: Int? = null,
    var providerId: Int = 0,
    @JoinObject(
        selectProperties = ["name", "ip"],
        targetColumn = "id",
        targetTable = "t_server AS parent_server",
        associationPrefix = "parent_",
        joinProperty = "parentId"
    )
    var parent: Server? = null
) {

  var order: Int = 1
  @JoinObject(
      selectProperties = ["name", "id"],
      targetColumn = "id",
      targetTable = "t_server_provider AS server_provider",
      associationPrefix = "provider_",
      joinProperty = "providerId"
  )
  var provider: ServerProvider? = null

}

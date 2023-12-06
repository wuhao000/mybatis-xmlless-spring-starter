package com.aegis.mybatis.bean

import com.aegis.mybatis.xmlless.annotations.JoinObject
import com.aegis.mybatis.xmlless.annotations.SelectedProperties
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable


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
        targetColumn = "id",
        targetTable = "t_server AS parent_server",
        associationPrefix = "parent_",
        joinProperty = "parentId"
    )
    @SelectedProperties(["name", "ip"])
    var parent: Server? = null
) {

  var order: Int = 1
  @JoinObject(
      targetColumn = "id",
      targetTable = "t_server_provider AS server_provider",
      associationPrefix = "provider_",
      joinProperty = "providerId"
  )
  var provider: ServerProvider? = null

}

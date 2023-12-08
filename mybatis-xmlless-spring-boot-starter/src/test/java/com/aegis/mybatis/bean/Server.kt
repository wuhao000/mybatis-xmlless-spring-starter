package com.aegis.mybatis.bean

import com.aegis.mybatis.xmlless.annotations.JoinObject
import com.aegis.mybatis.xmlless.annotations.JoinTable
import com.aegis.mybatis.xmlless.annotations.SelectedProperties
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id


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
        toTable = JoinTable("t_server AS parent_server", joinOnColumn = "id"),
        joinOnProperty = "parentId"
    )
    @SelectedProperties(["name", "ip"])
    var parent: Server? = null
) {

  var order: Int = 1
  @JoinObject(
      toTable = JoinTable("t_server_provider AS server_provider", joinOnColumn = "id"),
      joinOnProperty = "providerId"
  )
  var provider: ServerProvider? = null

}

package com.aegis.mybatis.bean

import com.aegis.mybatis.xmlless.annotations.JoinObject
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
        Server::class,
        joinOnProperty = "id",
        joinOnThisProperty = "parentId"
    )
    @SelectedProperties(["name", "ip"])
    var parent: Server? = null
) {

  var order: Int = 1

  @JoinObject(
      ServerProvider::class,
      "id",
      joinOnThisProperty = "providerId"
  )
  var provider: ServerProvider? = null

}

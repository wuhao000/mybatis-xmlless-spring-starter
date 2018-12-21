package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.bean.Server
import com.aegis.mybatis.dao.ServerDAO
import org.junit.Test


/**
 *
 * Created by 吴昊 on 2018-12-09.
 *
 * @author 吴昊
 * @since 0.0.1
 */
//@RunWith(SpringRunner::class)
//@SpringBootTest
class ServerMappingsTest : BaseResolverTest(
    Server::class.java,
    ServerDAO::class.java,
    "findById"
) {

  @Test
  fun resolveColumns() {
    val mappings = MappingResolver.getMappingCache(modelClass)
    mappings!!.mappings.mapNotNull { it.joinInfo }.forEach {
      println(it.selectFields(1))
    }
  }

  @Test
  fun resolveJoins() {
    val mappings = MappingResolver.getMappingCache(modelClass)
    println(mappings!!.selectJoins(1))
  }

}

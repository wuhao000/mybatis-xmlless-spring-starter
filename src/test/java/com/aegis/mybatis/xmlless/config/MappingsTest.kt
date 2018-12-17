package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.bean.Server
import com.aegis.mybatis.dao.ServerDAO
import com.aegis.mybatis.xmlless.model.ResolvedQuery
import com.aegis.mybatis.xmlless.resolver.QueryResolver
import com.baomidou.mybatisplus.core.config.GlobalConfig
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils
import com.baomidou.mybatisplus.core.toolkit.TableInfoHelper
import org.apache.ibatis.builder.MapperBuilderAssistant
import org.apache.ibatis.session.Configuration
import org.junit.Before
import org.junit.Test
import kotlin.reflect.full.declaredFunctions


/**
 *
 * Created by 吴昊 on 2018-12-09.
 *
 * @author 吴昊
 * @since 0.0.1
 */
//@RunWith(SpringRunner::class)
//@SpringBootTest
class MappingsTest {

  private val configuration = Configuration().apply {
    this.isMapUnderscoreToCamelCase = true
  }
  private val currentNameSpace = "np"
  private val mapperClass = ServerDAO::class.java
  private val modelClass = Server::class.java
  private val resource = modelClass.name.replace('.', '/') + ".java (best guess)"
  private val builderAssistant = MapperBuilderAssistant(configuration, resource).apply {
    currentNamespace = currentNameSpace
  }
  private lateinit var queries: List<ResolvedQuery>
  private lateinit var tableInfo: TableInfo

  @Before
  fun init() {
    GlobalConfigUtils.setGlobalConfig(
        configuration,
        GlobalConfig().setDbConfig(GlobalConfig.DbConfig().apply {
          this.tablePrefix = "t_"
        })
    )
    tableInfo = createTableInfo(modelClass)
    queries = mapperClass.kotlin.declaredFunctions
        .filter { it.name.startsWith("findById") }
        .map {
          QueryResolver.resolve(it, tableInfo, modelClass, mapperClass, builderAssistant)
        }
  }

  @Test
  fun resolveColumns() {
    val mappings = MappingResolver.getMappingCache(modelClass)
    val selectFields = mappings!!.selectFields()
    mappings.mappings.mapNotNull { it.joinInfo }.forEach {
      println(it.selectFields(1))
    }
  }

  private fun createTableInfo(modelClass: Class<*>): TableInfo {
    TableInfoHelper.initTableInfo(
        builderAssistant, modelClass
    )
    return TableInfoHelper.getTableInfo(modelClass)
  }

}

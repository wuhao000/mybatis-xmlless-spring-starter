package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.config.BaseResolverTest
import com.aegis.mybatis.xmlless.XmlLessMapper
import org.junit.jupiter.api.Test
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import kotlin.test.assertEquals

interface TestEntityDAO : XmlLessMapper<TestEntity> {

  /**
   *
   * @param name
   * @param keyword
   * @return
   */
  fun findByNameAndDescLikeKeyword(name: String, keyword: String): List<TestEntity>

  /**
   *
   * @param keyword
   * @return
   */
  fun findByNameOrDescLikeKeyword(keyword: String): List<TestEntity>

}

/**
 * Created by 吴昊 on 2018/12/26.
 */
class QueryTest : BaseResolverTest(TestEntity::class.java,
    TestEntityDAO::class.java,
    "findByNameAndDescLikeKeyword",
    "findByNameOrDescLikeKeyword") {

  @Test
  fun buildCountSql() {
  }

  @Test
  fun buildDeleteSql() {
  }

  @Test
  fun buildExistsSql() {
  }

  @Test
  fun buildInsertSql() {
  }

  @Test
  fun buildSelectSql() {
  }

  @Test
  fun buildUpdateSql() {
  }

  @Test
  operator fun component1() {
  }

  @Test
  operator fun component2() {
  }

  @Test
  operator fun component3() {
  }

  @Test
  operator fun component4() {
  }

  @Test
  operator fun component5() {
  }

  @Test
  operator fun component6() {
  }

  @Test
  operator fun component7() {
  }

  @Test
  operator fun component8() {
  }

  @Test
  fun containedTables() {
  }

  @Test
  fun convertIf() {
  }

  @Test
  fun copy() {
  }

  @Test
  fun equals() {
  }

  @Test
  fun getCriterion() {
  }

  @Test
  fun getExtraSortScript() {
  }

  @Test
  fun getFunction() {
  }

  @Test
  fun getLimitation() {
  }

  @Test
  fun getMappings() {
  }

  @Test
  fun getProperties() {
  }

  @Test
  fun getResolvedNameAnnotation() {
  }

  @Test
  fun getSorts() {
  }

  @Test
  fun getSqlSet() {
  }

  @Test
  fun getType() {
  }

  @Test
  fun hasCollectionJoinProperty() {
  }

  @Test
  fun includeJoins() {
  }

  @Test
  fun limitInSubQuery() {
  }

  @Test
  fun resolveFrom() {
  }

  @Test
  fun resolveGroupBy() {
    val query1 = this.queries.first { it.function.name == "findByNameAndDescLikeKeyword" }.query!!
    println(query1.toSql())
    assertEquals(2, query1.resolveGroups().size)
    val query2 = this.queries.first { it.function.name == "findByNameOrDescLikeKeyword" }.query!!
    println(query2.toSql())
    assertEquals(1, query2.resolveGroups().size)
  }

  @Test
  fun resolveGroups() {
  }

  @Test
  fun resolveLimit() {
  }

  @Test
  fun resolveOrder() {
  }

  @Test
  fun resolveUpdateProperties() {
  }

  @Test
  fun resolveUpdateWhere() {
  }

  @Test
  fun resolveWhere() {
  }

  @Test
  fun setExtraSortScript() {
  }

  @Test
  fun setLimitation() {
  }

  @Test
  fun toSql() {
  }

}

class TestEntity {

  var desc: String = ""
  @Id
  @GeneratedValue
  var id: Int = 0
  var name: String = ""

}

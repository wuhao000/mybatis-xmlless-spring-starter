package com.aegis.mybatis.xmlless.model

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KFunction


/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
class ResolvedQueries(private val mapperClass: Class<*>,
                      private val unmappedMethods: List<KFunction<*>> = listOf()) {

  private val queries = mutableListOf<ResolvedQuery>()

  companion object {
    private val log: Logger = LoggerFactory.getLogger(ResolvedQueries::class.java)
  }

  fun add(resolvedQuery: ResolvedQuery) {
    this.queries.add(resolvedQuery)
  }

  fun log() {
    val sb = StringBuilder()
    sb.append("===================================================")
    sb.append("\n\n\t类: $mapperClass\n")
    if (unmappedMethods.isNotEmpty()) {
      sb.append("\n 未映射的方法:")
      unmappedMethods.forEach {
        sb.append("\n" + "\t".repeat(3) + it.name)
      }
    }
    queries.sortedBy { it.query == null }.forEach { resolvedQuery ->
      sb.append(resolvedQuery.toString())
    }
    sb.append("\n**********************************************")
    log.info("\n\n" + sb.toString() + "\n")
  }

}

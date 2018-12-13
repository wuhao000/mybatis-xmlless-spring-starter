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
    sb.append("**********************************************")
    sb.append("\n\tClass: $mapperClass\n")
    if (unmappedMethods.isNotEmpty()) {
      sb.append("\n Unmapped methods:")
      unmappedMethods.forEach {
        sb.append("\n" + "\t".repeat(3) + it.name)
      }
    }
    queries.sortedBy { it.query == null }.forEach { resolvedQuery ->
      sb.append("\t ${if (resolvedQuery.isValid()) {
        "Resolved"
      } else {
        "Unresolved"
      }} method:\t${resolvedQuery.function}\n")
      if (resolvedQuery.isValid()) {
        sb.append("\t\t- Type: ${resolvedQuery.type()}\n")
        sb.append("\t\t- SQL: \n${resolvedQuery.sql!!.trim().lines().joinToString("\n") { "\t".repeat(5) + it }}\n")
        sb.append("\t\t- Return: ${resolvedQuery.returnType}")
      } else {
        resolvedQuery.unresolvedReasons.forEach { unresolvedReason ->
          sb.append("\t\t - $unresolvedReason")
        }
      }
      sb.append("\n\n")
    }
    sb.append("\n**********************************************")
    log.info("\n\n" + sb.toString() + "\n")
  }

}

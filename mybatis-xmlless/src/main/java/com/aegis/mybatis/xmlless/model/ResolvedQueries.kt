package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.exception.XmlLessException
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
class ResolvedQueries(private val mapperClass: Class<*>) {

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
    queries.sortedBy { it.method.name }
        .filter { it.query != null }.forEach {
          sb.append(it.toString())
        }
    queries.sortedBy { it.method.name }
        .filter { it.query == null }.forEach {
          sb.append(it.toString())
        }
    sb.append("\n===================================================")
    if (log.isDebugEnabled) {
      log.info("\n\n" + sb.toString() + "\n")
    }
    logUnresolved()
  }

  private fun logUnresolved() {
    val sb = StringBuilder()
    val unResolved = queries.filter { it.query == null }
    if (unResolved.isNotEmpty()) {
      sb.append("以下方法未能成功解析:")
      unResolved.sortedBy { it.method.name }.forEach {
        sb.append("\n\t\t- ${it.method.declaringClass.name}." + it.method.name + ", 原因: " + it.unresolvedReason)
      }
      throw XmlLessException(sb.toString())
    }
  }

}

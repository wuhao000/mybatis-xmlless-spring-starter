package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.config.paginition.XmlLessException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.Method
import kotlin.reflect.jvm.javaMethod


/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
class ResolvedQueries(private val mapperClass: Class<*>,
                      private val unmappedMethods: List<Method> = listOf()) {

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
    queries.sortedBy { it.function.name }
        .filter { it.query != null }.forEach {
          sb.append(it.toString())
        }
    queries.sortedBy { it.function.name }
        .filter { it.query == null }.forEach {
          sb.append(it.toString())
        }
    sb.append("\n===================================================")
//    if (log.isDebugEnabled) {
      log.info("\n\n" + sb.toString() + "\n")
//    }
    logUnresolved()
  }

  private fun logUnresolved() {
    val sb = StringBuilder()
    val unResolved = queries.filter { it.query == null }
    if (unResolved.isNotEmpty()) {
      sb.append("以下方法未能成功解析:")
      unResolved.sortedBy { it.function.name }.forEach {
        sb.append("\n\t\t- ${it.function.declaringClass.name}." + it.function.name + ", 原因: " + it.unresolvedReason)
      }
      throw XmlLessException(sb.toString())
    }
  }

}

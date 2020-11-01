package com.aegis.mybatis.xmlless.config.paginition

import com.aegis.jackson.createObjectMapper
import com.aegis.mybatis.xmlless.annotations.JsonMappingProperty
import com.aegis.mybatis.xmlless.annotations.JsonResult
import com.aegis.mybatis.xmlless.methods.XmlLessMethods
import com.aegis.mybatis.xmlless.model.JsonWrapper
import com.aegis.mybatis.xmlless.resolver.QueryResolver
import com.baomidou.mybatisplus.core.metadata.IPage
import org.apache.ibatis.binding.MapperMethod
import org.apache.ibatis.mapping.SqlCommandType
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.SqlSession
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.lang.reflect.Method

/**
 *
 * Created by 吴昊 on 2018/12/13.
 *
 * @author 吴昊
 * @since 0.0.4
 */
class XmlLessPageMapperMethod(
    mapperInterface: Class<*>,
    val requestMethod: Method,
    config: Configuration
) : MapperMethod(mapperInterface, requestMethod, config) {

  private val command = SqlCommand(config, mapperInterface, requestMethod)
  private val method = MethodSignature(config, mapperInterface, requestMethod)

  companion object {
    val mapper = createObjectMapper()
  }

  @Suppress("UNCHECKED_CAST")
  override fun execute(sqlSession: SqlSession, args: Array<out Any?>?): Any? {
    var result: Any? = null
    if (command.type == SqlCommandType.SELECT && args != null
        && Page::class.java.isAssignableFrom(method.returnType)
    ) {
      val list = executeForMany<Any>(sqlSession, args) as List<Any>
      val pageArg = findIPageArg(args) as IPage<*>?
      result = if (pageArg != null) {
        PageImpl(list, PageRequest.of((pageArg.current - 1).toInt(), pageArg.size.toInt()), pageArg.total)
      } else {
        val pageableArg = args.firstOrNull { it is Pageable } as Pageable?
        val total = executeForTotal(sqlSession, args)
        if (pageableArg != null) {
          PageImpl(list, pageableArg, total)
        } else {
          PageImpl(list, PageRequest.of(0, list.size), total)
        }
      }
    }
    if (result == null) {
      try {
        result = super.execute(sqlSession, args)
      } catch (e: Exception) {
        e.printStackTrace()
        throw e
      }
    }
    if (result == null) {
      return result
    }
    val returnClass = QueryResolver.resolveReturnType(requestMethod)
    val forceSingleValue = forceSingleValue(requestMethod)
    val type = QueryResolver.resolveJavaType(requestMethod, forceSingleValue)
    if (requestMethod.isAnnotationPresent(JsonResult::class.java)
        || returnClass.isAnnotationPresent(JsonMappingProperty::class.java)
    ) {
      if (forceSingleValue) {
        val size = (result as MutableCollection<Any?>).size
        if (size > 1) {
          throw XmlLessException("Need result size to be 1, but got [$size]")
        } else if (size == 0){
          return null
        } else {
          val value = result.first()
          val json = extractJson(value)
          return when (json) {
            null -> null
            else -> mapper.readValue<Any?>(json, type)
          }
        }
      }
      if (result is MutableCollection<*>) {
        val list = arrayListOf<Any?>()
        list.addAll(result as Collection<Any?>)
        result.clear()
        (result as MutableCollection<Any?>).addAll(
            list.map {
              val json = extractJson(it)
              when (json) {
                null -> null
                else -> mapper.readValue<Any?>(json, type)
              }
            }
        )
      } else {
        val json = (result as JsonWrapper).json ?: return null
        result = mapper.readValue<Any?>(json, type)
      }
    }
    return result
  }

  private fun extractJson(it: Any?): String? {
    return when (it) {
      null           -> null
      is String      -> it
      is JsonWrapper -> it.json
      else           -> it.toString()
    }
  }

  private fun <E> executeForMany(sqlSession: SqlSession, args: Array<out Any?>?): Any {
    val result: List<E>
    val param = method.convertArgsToSqlCommandParam(args)
    result = if (method.hasRowBounds()) {
      val rowBounds = method.extractRowBounds(args)
      sqlSession.selectList(command.name, param, rowBounds)
    } else {
      sqlSession.selectList(command.name, param)
    }
    return result
  }

  private fun executeForTotal(sqlSession: SqlSession, args: Array<out Any?>?): Long {
    val param = method.convertArgsToSqlCommandParam(args)
    val commandName = command.name + XmlLessMethods.COUNT_STATEMENT_SUFFIX
    return sqlSession.selectOne(commandName, param)
  }

  private fun findIPageArg(args: Array<out Any?>): Any? {
    return args.filterNotNull().firstOrNull {
      IPage::class.java.isAssignableFrom(it.javaClass)
    }
  }

  private fun forceSingleValue(requestMethod: Method): Boolean {
    return requestMethod.isAnnotationPresent(JsonResult::class.java)
        && requestMethod.getAnnotation(JsonResult::class.java).forceSingleValue
  }

}

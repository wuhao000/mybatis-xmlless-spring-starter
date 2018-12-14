package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.xmlless.kotlin.split
import com.aegis.mybatis.xmlless.kotlin.toCamelCase
import com.aegis.mybatis.xmlless.kotlin.toWords
import com.aegis.mybatis.xmlless.annotations.ResolvedName
import com.aegis.mybatis.xmlless.model.*
import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.toolkit.StringPool.DOT
import org.apache.ibatis.annotations.ResultMap
import org.apache.ibatis.reflection.ParamNameResolver
import org.apache.ibatis.session.Configuration
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmErasure

/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
object QueryResolver {

  private val QUERY_CACHE = hashMapOf<String, ResolvedQuery>()

  fun getQueryCache(key: String): ResolvedQuery? {
    return QUERY_CACHE[key]
  }

  fun resolve(function: KFunction<*>, tableInfo: TableInfo, modelClass: Class<*>, mapperClass: Class<*>):
      ResolvedQuery {
    if (getQueryCache(function, mapperClass) != null) {
      return getQueryCache(function, mapperClass)!!
    }
    val mappings = MappingResolver.resolve(modelClass, tableInfo)
    val paramNames = ParamNameResolver(
        Configuration().apply {
          this.isUseActualParamName = true
        }, function.javaMethod
    ).names
    val resolvedNameAnnotation = function.findAnnotation<ResolvedName>()
    val resolvedName = resolvedNameAnnotation?.name ?: function.name
    val resolveSortsResult = resolveSorts(resolvedName)
    val resolveTypeResult = resolveType(resolveSortsResult.remainName)
    if (resolveTypeResult.type == null) {
      return ResolvedQuery(null, null, null, function,
          arrayListOf("Cannot resolve query type with word ${resolveTypeResult.typeWord}"))
    }
    val resolvePropertiesResult = resolveProperties(resolveTypeResult.remainWords)
    val conditions = resolveConditions(resolvePropertiesResult.conditionWords, function, paramNames)
    val query = Query(
        resolveTypeResult.type,
        resolvePropertiesResult.properties,
        conditions,
        resolveSortsResult.sorts,
        function,
        mappings,
        null,
        resolvedNameAnnotation,
        mapperClass
    )
    function.valueParameters.forEachIndexed { index, param ->
      val paramName = paramNames[index]
      if (Pageable::class.isSuperclassOf(param.type.jvmErasure)) {
        query.limitation = Limitation("$paramName.offset", "$paramName.pageSize")
        query.extraSortScript =
            String.format(
                """<if test="%s.sort.isSorted">
                <foreach collection="%s.sort.get().toArray()"
                item="item" separator=",">

                  ${'$'}{item.property} <if test="item.isAscending">ASC</if><if test="item.isDescending">DESC</if>
                </foreach>
              </if>""".trimIndent(), paramName,paramName)
      }
    }
    val resolvedQuery = ResolvedQuery(
        query, resolveResultMap(function), resolveReturnType(function), function, arrayListOf()
    )
    putQueryCache(function, mapperClass, resolvedQuery)
    return resolvedQuery
  }

  private fun getQueryCache(function: KFunction<*>, mapperClass: Class<*>): ResolvedQuery? {
    return QUERY_CACHE[mapperClass.name + DOT + function.name]
  }

  private fun putQueryCache(function: KFunction<*>, mapperClass: Class<*>, query: ResolvedQuery) {
    QUERY_CACHE[mapperClass.name + DOT + function.name] = query
  }

  private fun resolveConditions(allConditionWords: List<String>,
                                function: KFunction<*>, paramNames: Array<String>): List<Condition> {
    return if (allConditionWords.isNotEmpty()) {
      allConditionWords.split("And").map { addPropertiesWords ->
        // 解析形如 nameEq 或者 nameLikeKeywords 的表达式
        // nameEq 解析为 name = #{name}
        // nameLikeKeywords 解析为 name  LIKE concat('%',#{keywords},'%')
        addPropertiesWords.split("Or").map { singleConditionWords ->
          // 获取表示条件表达式操作符的单词
          val opWords = Operations.nameWords().firstOrNull { singleConditionWords.containsAll(it) }
          val props = if (opWords != null) {
            singleConditionWords.split(opWords)
          } else {
            listOf(singleConditionWords)
          }
          if (props.size !in 1..2) {
            throw IllegalStateException("Cannot resolve query conditions from ${allConditionWords.joinToString().toCamelCase()}")
          }
          // 解析条件表达式的二元操作符 = > < >= <= != in like 等
          val op = when {
            opWords != null -> Operations.valueOf(opWords)
            else            -> null
          }
          val property = when {
            op != null -> props.first().joinToString("").toCamelCase()
            else       -> singleConditionWords.joinToString("").toCamelCase()
          }
          val paramName = when {
            props.size == 2 -> props[1].joinToString("").toCamelCase()
            else            -> null
          } ?: property
          var parameter: KParameter? = null
          val paramIndex = paramNames.indexOf(paramName)
          if (paramIndex >= 0) {
            parameter = function.valueParameters[paramIndex]
          }
          Condition(property, op ?: Operations.EqDefault, "Or", paramName, parameter,
              function.findAnnotation<ResolvedName>()?.values?.firstOrNull {
                it.param == property
              })
        }.apply {
          last().append = "And"
        }
      }.flatten()
    } else {
      listOf()
    }
  }

  private fun resolveProperties(remainWords: List<String>): ResolvePropertiesResult {
    val byIndex = remainWords.indexOf("By")
    val properties: List<String> = if (byIndex == 0) {
      listOf()
    } else {
      val propertiesWords = if (byIndex > 0) {
        remainWords.subList(0, byIndex)
      } else {
        remainWords
      }
      propertiesWords.split("And")
          .filter { !(it.size == 1 && it.first() == "All") }
          .map { it.joinToString("").toCamelCase() }
    }
    val conditionWords = if (byIndex >= 0) {
      remainWords.subList(byIndex + 1, remainWords.size)
    } else {
      listOf()
    }
    return ResolvePropertiesResult(properties, conditionWords)
  }

  private fun resolveResultMap(function: KFunction<*>): String? {
    return function.findAnnotation<ResultMap>()?.value?.firstOrNull()
  }

  private fun resolveReturnType(function: KFunction<*>): Class<*> {
    return if (listOf(Collection::class, Page::class, IPage::class)
            .any { it.java.isAssignableFrom(function.javaMethod!!.returnType) }) {
      val type = (function.javaMethod!!.genericReturnType as ParameterizedType).actualTypeArguments[0]
      if (type is Class<*>) {
        type
      } else {
        function.javaMethod!!.returnType
      }
    } else {
      function.javaMethod!!.returnType
    }
  }

  private fun resolveSorts(name: String): ResolveSortsResult {
    val orderByIndex = name.indexOf("OrderBy")
    val sorts = if (orderByIndex > 0) {
      val orderByString = name.substring(orderByIndex).replace("OrderBy", "")
      val sortStrings = orderByString.split("And")
      sortStrings.map {
        var direction = Sort.Direction.ASC
        val sortProperty = when {
          it.endsWith("Desc") -> {
            direction = Sort.Direction.DESC
            it.substring(0, it.length - 4)
          }
          it.endsWith("Asc")  -> it.substring(0, it.length - 3)
          else                -> it
        }
        Sort.Order(direction, sortProperty)
      }
    } else {
      listOf()
    }
    val remainName = if (orderByIndex > 0) {
      name.substring(0, orderByIndex)
    } else {
      name
    }
    return ResolveSortsResult(sorts, remainName)
  }

  private fun resolveType(name: String): ResolveTypeResult {
    val wordsWithoutSort = name.toWords()
    val typeWord = wordsWithoutSort[0]
    val type: QueryType = when (typeWord) {
      in listOf("Find", "Select", "Query") -> QueryType.Select
      "Exists"                             -> QueryType.Exists
      "Count"                              -> QueryType.Count
      "Update"                             -> QueryType.Update
      in listOf("Delete", "Remove")        -> QueryType.Delete
      in listOf("Insert", "Save")          -> QueryType.Insert
      else                                 -> null
    } ?: return ResolveTypeResult(null, listOf(), typeWord)
    val remainWords = wordsWithoutSort.drop(1)
    return ResolveTypeResult(type, remainWords, typeWord)
  }

  /**
   * 从方法名中解析出的查询属性结果
   * @author 吴昊
   * @since 0.0.2
   */
  data class ResolvePropertiesResult(val properties: List<String>, val conditionWords: List<String>)

  /**
   * 从方法名中解析出的排序属性结果
   * @author 吴昊
   * @since 0.0.2
   */
  data class ResolveSortsResult(val sorts: List<Sort.Order>, val remainName: String)

  /**
   * 从方法名中解析出的sql类型结果
   * @author 吴昊
   * @since 0.0.2
   */
  data class ResolveTypeResult(val type: QueryType?, val remainWords: List<String>, val typeWord: String)

}

/**
 * 数据库支持的操作符
 * @author 吴昊
 * @since 0.0.1
 */
enum class Operations(private val operator: String) {

  Eq("="),
  EqDefault("="),
  Gt(">"),
  Gte(">="),
  In("IN"),
  IsNull("IS NULL"),
  Like("LIKE"),
  Lt("<"),
  Lte("<="),
  Ne("!="),
  NotNull("IS NOT NULL");

  companion object {
    fun nameWords(): List<List<String>> {
      return names().map { it.toWords() }
    }

    private fun names(): List<String> {
      return Operations.values().map { it.name }
    }

    fun valueOf(words: List<String>): Operations? {
      val name = words.joinToString("")
      return values().firstOrNull {
        it.name == name
      }
    }
  }

  fun getTemplate(): String {
    return when (this) {
      Like    -> "%s ${this.operator} CONCAT('%%',#{%s},'%%')"
      In      -> "%s\n\t${this.operator}\n%s"
      NotNull -> "%s ${this.operator}"
      IsNull  -> "%s ${this.operator}"
      else    -> "%s ${this.operator} #{%s}"
    }
  }

  fun getValueTemplate(): String {
    return when (this) {
      Like    -> "%s ${this.operator} CONCAT('%%',%s,'%%')"
      In      -> "%s\n\t${this.operator}\n%s"
      NotNull -> "%s ${this.operator}"
      IsNull  -> "%s ${this.operator}"
      else    -> "%s ${this.operator} %s"
    }
  }

}

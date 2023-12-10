/*
 * Copyright 2009-2017 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.xmlless.resolver.ParameterResolver
import com.aegis.mybatis.xmlless.resolver.QueryResolver
import com.baomidou.mybatisplus.core.mapper.Mapper
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit
import com.baomidou.mybatisplus.core.toolkit.StringPool
import org.apache.ibatis.annotations.*
import org.apache.ibatis.annotations.Options.FlushCachePolicy
import org.apache.ibatis.annotations.ResultMap
import org.apache.ibatis.binding.BindingException
import org.apache.ibatis.binding.MapperMethod.ParamMap
import org.apache.ibatis.builder.BuilderException
import org.apache.ibatis.builder.IncompleteElementException
import org.apache.ibatis.builder.MapperBuilderAssistant
import org.apache.ibatis.builder.annotation.MapperAnnotationBuilder
import org.apache.ibatis.builder.annotation.MethodResolver
import org.apache.ibatis.builder.annotation.ProviderSqlSource
import org.apache.ibatis.builder.xml.XMLMapperBuilder
import org.apache.ibatis.cursor.Cursor
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator
import org.apache.ibatis.executor.keygen.KeyGenerator
import org.apache.ibatis.executor.keygen.NoKeyGenerator
import org.apache.ibatis.executor.keygen.SelectKeyGenerator
import org.apache.ibatis.io.Resources
import org.apache.ibatis.mapping.*
import org.apache.ibatis.parsing.PropertyParser
import org.apache.ibatis.reflection.TypeParameterResolver
import org.apache.ibatis.scripting.LanguageDriver
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.ResultHandler
import org.apache.ibatis.session.RowBounds
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.UnknownTypeHandler
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.GenericArrayType
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.util.*

/**
 *
 *
 * 继承 MapperAnnotationBuilder 没有XML配置文件注入基础CRUD方法
 *
 *
 * @author Caratacus
 * @since 2017-01-04
 */
class MybatisXmlLessMapperAnnotationBuilder(configuration: Configuration, type: Class<*>) :
    MapperAnnotationBuilder(configuration, type) {

  private val assistant: MapperBuilderAssistant
  private val configuration: Configuration
  private val sqlAnnotationTypes: MutableSet<Class<out Annotation>> = HashSet()
  private val sqlProviderAnnotationTypes: MutableSet<Class<out Annotation>> = HashSet()
  private val type: Class<*>

  init {
    // 执行父类
    val resource = type.getName().replace('.', '/') + ".java (best guess)"
    this.assistant = MapperBuilderAssistant(configuration, resource)
    this.configuration = configuration
    this.type = type
    sqlAnnotationTypes.add(Select::class.java)
    sqlAnnotationTypes.add(Insert::class.java)
    sqlAnnotationTypes.add(Update::class.java)
    sqlAnnotationTypes.add(Delete::class.java)
    sqlProviderAnnotationTypes.add(SelectProvider::class.java)
    sqlProviderAnnotationTypes.add(InsertProvider::class.java)
    sqlProviderAnnotationTypes.add(UpdateProvider::class.java)
    sqlProviderAnnotationTypes.add(DeleteProvider::class.java)
  }

  override fun parse() {
    val resource = type.toString()

    if (!configuration.isResourceLoaded(resource)) {
      loadXmlResource()
      configuration.addLoadedResource(resource)
      assistant.currentNamespace = type.getName()
      parseCache()
      parseCacheRef()
      val modelClass = ReflectionKit.getSuperClassGenericType(type, Mapper::class.java, 0)
      val methods = type.getMethods()
      if (modelClass != null) {
        type.methods.mapNotNull {
          QueryResolver.resolveJavaType(it, modelClass, false)?.rawClass
        }.filter {
          ParameterResolver.isComplexType(it)
        }.distinct().filter { it != modelClass }.forEach {
          TableInfoHelper.initTableInfo(assistant, it)
        }
      }
      GlobalConfigUtils.getSqlInjector(configuration).inspectInject(assistant, type)
      for (method in methods) {
        try {
          // issue #237
          if (!method.isBridge) {
            parseStatement(method)
          }
        } catch (ex: IncompleteElementException) {
          configuration.addIncompleteMethod(MethodResolver(this, method))
        }
      }
    }
    parsePendingMethods()
  }

  private fun applyConstructorArgs(
      args: Array<Arg>,
      resultType: Class<*>,
      resultMappings: MutableList<ResultMapping>
  ) {
    for (arg in args) {
      val flags: MutableList<ResultFlag> = ArrayList()
      flags.add(ResultFlag.CONSTRUCTOR)
      if (arg.id) {
        flags.add(ResultFlag.ID)
      }
      val typeHandler = if (arg.typeHandler != UnknownTypeHandler::class.java) {
        arg.typeHandler.java
      } else {
        null
      }
      val resultMapping = assistant.buildResultMapping(
          resultType,
          nullOrEmpty(arg.name),
          nullOrEmpty(arg.column),
          if (arg.javaType == Void.TYPE) {
            null
          } else {
            arg.javaType.java
          },
          if (arg.jdbcType == JdbcType.UNDEFINED) {
            null
          } else {
            arg.jdbcType
          },
          nullOrEmpty(arg.select),
          nullOrEmpty(arg.resultMap),
          null,
          null,
          typeHandler,
          flags,
          null,
          null,
          false
      )
      resultMappings.add(resultMapping)
    }
  }

  private fun applyDiscriminator(
      resultMapId: String,
      resultType: Class<*>,
      discriminator: TypeDiscriminator?
  ): Discriminator? {
    if (discriminator != null) {
      val column = discriminator.column
      val javaType: Class<*> = if (discriminator.javaType == Void.TYPE) {
        String::class.java
      } else {
        discriminator.javaType.java
      }
      val jdbcType = if (discriminator.jdbcType == JdbcType.UNDEFINED) {
        null
      } else {
        discriminator.jdbcType
      }
      val typeHandler = if (discriminator.typeHandler != UnknownTypeHandler::class.java) {
        discriminator.typeHandler.java
      } else {
        null
      }
      val cases = discriminator.cases
      val discriminatorMap: MutableMap<String, String> = HashMap()
      for (c in cases) {
        val value = c.value
        val caseResultMapId = resultMapId + StringPool.DASH + value
        discriminatorMap[value] = caseResultMapId
      }
      return assistant.buildDiscriminator(resultType, column, javaType, jdbcType, typeHandler, discriminatorMap)
    }
    return null
  }

  private fun applyResultMap(
      resultMapId: String, returnType: Class<*>, args: Array<Arg>, results: Array<Result>,
      discriminator: TypeDiscriminator?
  ) {
    val resultMappings: MutableList<ResultMapping> = ArrayList()
    applyConstructorArgs(args, returnType, resultMappings)
    applyResults(results, returnType, resultMappings)
    val disc = applyDiscriminator(resultMapId, returnType, discriminator)
    // add AutoMappingBehaviour
    assistant.addResultMap(resultMapId, returnType, null, disc, resultMappings, null)
    createDiscriminatorResultMaps(resultMapId, returnType, discriminator)
  }

  private fun applyResults(results: Array<Result>, resultType: Class<*>, resultMappings: MutableList<ResultMapping>) {
    for (result in results) {
      val flags: MutableList<ResultFlag> = ArrayList()
      if (result.id) {
        flags.add(ResultFlag.ID)
      }
      val typeHandler = if (result.typeHandler != UnknownTypeHandler::class.java) {
        result.typeHandler.java
      } else {
        null
      }
      val resultMapping = assistant.buildResultMapping(
          resultType,
          nullOrEmpty(result.property),
          nullOrEmpty(result.column),
          if (result.javaType == Void.TYPE) {
            null
          } else {
            result.javaType.java
          },
          if (result.jdbcType == JdbcType.UNDEFINED) {
            null
          } else {
            result.jdbcType
          },
          if (hasNestedSelect(result)) {
            nestedSelectId(result)
          } else {
            null
          },
          null,
          null,
          null,
          typeHandler,
          flags,
          null,
          null,
          isLazy(result)
      )
      resultMappings.add(resultMapping)
    }
  }

  private fun argsIf(args: ConstructorArgs?): Array<Arg> {
    return args?.value ?: arrayOf()
  }

  private fun buildSqlSourceFromStrings(
      strings: Array<String>,
      parameterTypeClass: Class<*>?,
      languageDriver: LanguageDriver
  ): SqlSource {
    val sql = StringBuilder()
    for (fragment in strings) {
      sql.append(fragment)
      sql.append(StringPool.SPACE)
    }
    return languageDriver.createSqlSource(configuration, sql.toString().trim { it <= ' ' }, parameterTypeClass)
  }

  private fun chooseAnnotationType(method: Method, types: Set<Class<out Annotation>>): Class<out Annotation>? {
    for (type in types) {
      val annotation = method.getAnnotation(type)
      if (annotation != null) {
        return type
      }
    }
    return null
  }

  private fun convertToProperties(properties: Array<Property>): Properties? {
    if (properties.isEmpty()) {
      return null
    }
    val props = Properties()
    for (property in properties) {
      props.setProperty(property.name, PropertyParser.parse(property.value, configuration.variables))
    }
    return props
  }

  private fun createDiscriminatorResultMaps(
      resultMapId: String,
      resultType: Class<*>,
      discriminator: TypeDiscriminator?
  ) {
    if (discriminator != null) {
      for (c in discriminator.cases) {
        val caseResultMapId = resultMapId + StringPool.DASH + c.value
        val resultMappings: MutableList<ResultMapping> = ArrayList()
        // issue #136
        applyConstructorArgs(c.constructArgs, resultType, resultMappings)
        applyResults(c.results, resultType, resultMappings)
        assistant.addResultMap(caseResultMapId, c.type.java, resultMapId, null, resultMappings, null)
      }
    }
  }

  private fun generateResultMapName(method: Method): String {
    val results = method.getAnnotation(
        Results::class.java
    )
    if (results != null && results.id.isNotEmpty()) {
      return type.getName() + StringPool.DOT + results.id
    }
    val suffix = StringBuilder()
    for (c in method.parameterTypes) {
      suffix.append(StringPool.DASH)
      suffix.append(c.getSimpleName())
    }
    if (suffix.isEmpty()) {
      suffix.append("-void")
    }
    return type.getName() + StringPool.DOT + method.name + suffix
  }

  private fun getLanguageDriver(method: Method): LanguageDriver {
    val lang = method.getAnnotation(Lang::class.java)
    var langClass: Class<out LanguageDriver?>? = null
    if (lang != null) {
      langClass = lang.value.java
    }
    return assistant.configuration.getLanguageDriver(langClass)
  }

  private fun getParameterType(method: Method): Class<*>? {
    var parameterType: Class<*>? = null
    val parameterTypes = method.parameterTypes
    for (currentParameterType in parameterTypes) {
      if (!RowBounds::class.java.isAssignableFrom(currentParameterType)
          && !ResultHandler::class.java.isAssignableFrom(currentParameterType)
      ) {
        parameterType = if (parameterType == null) {
          currentParameterType
        } else {
          // issue #135
          ParamMap::class.java
        }
      }
    }
    return parameterType
  }

  private fun getReturnType(method: Method): Class<*> {
    var returnType = method.returnType
    val resolvedReturnType = TypeParameterResolver.resolveReturnType(method, type)
    if (resolvedReturnType is Class<*>) {
      returnType = resolvedReturnType
      if (returnType.isArray) {
        returnType = returnType.componentType
      }
      if (Void.TYPE == returnType) {
        val rt = method.getAnnotation(
            ResultType::class.java
        )
        if (rt != null) {
          returnType = rt.value.java
        }
      }
    } else if (resolvedReturnType is ParameterizedType) {
      val rawType = resolvedReturnType.rawType as Class<*>
      if (MutableCollection::class.java.isAssignableFrom(rawType) || Cursor::class.java.isAssignableFrom(rawType)) {
        val actualTypeArguments = resolvedReturnType.actualTypeArguments
        if (actualTypeArguments != null && actualTypeArguments.size == 1) {
          when (val returnTypeParameter = actualTypeArguments[0]) {
            is Class<*>          -> {
              returnType = returnTypeParameter
            }

            is ParameterizedType -> {
              returnType = returnTypeParameter.rawType as Class<*>
            }

            is GenericArrayType  -> {
              val componentType = returnTypeParameter.genericComponentType as Class<*>
              // (gcode issue #525) support List<byte[]>
              returnType = java.lang.reflect.Array.newInstance(componentType, 0).javaClass
            }
          }
        }
      } else if (method.isAnnotationPresent(MapKey::class.java) && MutableMap::class.java.isAssignableFrom(rawType)) {
        // (gcode issue 504) Do not look into Maps if there is not MapKey annotation
        val actualTypeArguments = resolvedReturnType.actualTypeArguments
        if (actualTypeArguments != null && actualTypeArguments.size == 2) {
          val returnTypeParameter = actualTypeArguments[1]
          if (returnTypeParameter is Class<*>) {
            returnType = returnTypeParameter
          } else if (returnTypeParameter is ParameterizedType) {
            returnType = returnTypeParameter.rawType as Class<*>
          }
        }
      }
    }
    return returnType
  }

  private fun getSqlAnnotationType(method: Method): Class<out Annotation>? {
    return chooseAnnotationType(method, sqlAnnotationTypes)
  }

  private fun getSqlCommandType(method: Method): SqlCommandType {
    var type = getSqlAnnotationType(method)
    if (type == null) {
      type = getSqlProviderAnnotationType(method)
      if (type == null) {
        return SqlCommandType.UNKNOWN
      }
      when (type) {
        SelectProvider::class.java -> {
          type = Select::class.java
        }

        InsertProvider::class.java -> {
          type = Insert::class.java
        }

        UpdateProvider::class.java -> {
          type = Update::class.java
        }

        DeleteProvider::class.java -> {
          type = Delete::class.java
        }
      }
    }
    return SqlCommandType.valueOf(type.getSimpleName().uppercase())
  }

  private fun getSqlProviderAnnotationType(method: Method): Class<out Annotation>? {
    return chooseAnnotationType(method, sqlProviderAnnotationTypes)
  }

  private fun getSqlSourceFromAnnotations(
      method: Method,
      parameterType: Class<*>?,
      languageDriver: LanguageDriver
  ): SqlSource? {
    return try {
      val sqlAnnotationType = getSqlAnnotationType(method)
      val sqlProviderAnnotationType = getSqlProviderAnnotationType(method)
      if (sqlAnnotationType != null) {
        if (sqlProviderAnnotationType != null) {
          throw BindingException("You cannot supply both a static SQL and SqlProvider to method named " + method.name)
        }
        val sqlAnnotation = method.getAnnotation(sqlAnnotationType)
        val strings = sqlAnnotation.javaClass.getMethod("value").invoke(sqlAnnotation) as Array<String>
        return buildSqlSourceFromStrings(strings, parameterType, languageDriver)
      } else if (sqlProviderAnnotationType != null) {
        val sqlProviderAnnotation = method.getAnnotation(sqlProviderAnnotationType)
        return ProviderSqlSource(assistant.configuration, sqlProviderAnnotation, type, method)
      }
      null
    } catch (e: Exception) {
      throw BuilderException("Could not find value method on SQL annotation.  Cause: $e", e)
    }
  }

  private fun handleSelectKeyAnnotation(
      selectKeyAnnotation: SelectKey,
      baseStatementId: String,
      parameterTypeClass: Class<*>?,
      languageDriver: LanguageDriver
  ): KeyGenerator {
    var id: String? = baseStatementId + SelectKeyGenerator.SELECT_KEY_SUFFIX
    val resultTypeClass: Class<*> = selectKeyAnnotation.resultType.java
    val statementType = selectKeyAnnotation.statementType
    val keyProperty = selectKeyAnnotation.keyProperty
    val keyColumn = selectKeyAnnotation.keyColumn
    val executeBefore = selectKeyAnnotation.before

    // defaults
    val useCache = false
    val keyGenerator: KeyGenerator = NoKeyGenerator.INSTANCE
    val fetchSize: Int? = null
    val timeout: Int? = null
    val flushCache = false
    val parameterMap: String? = null
    val resultMap: String? = null
    val resultSetTypeEnum: ResultSetType? = null
    val sqlSource = buildSqlSourceFromStrings(selectKeyAnnotation.statement, parameterTypeClass, languageDriver)
    val sqlCommandType = SqlCommandType.SELECT
    assistant.addMappedStatement(
        id,
        sqlSource,
        statementType,
        sqlCommandType,
        fetchSize,
        timeout,
        parameterMap,
        parameterTypeClass,
        resultMap,
        resultTypeClass,
        resultSetTypeEnum,
        flushCache,
        useCache,
        false,
        keyGenerator,
        keyProperty,
        keyColumn,
        null,
        languageDriver,
        null
    )
    id = assistant.applyCurrentNamespace(id, false)
    val keyStatement = configuration.getMappedStatement(id, false)
    val answer = SelectKeyGenerator(keyStatement, executeBefore)
    configuration.addKeyGenerator(id, answer)
    return answer
  }

  private fun hasNestedSelect(result: Result?): Boolean {
    if (result!!.one.select.isNotEmpty() && result.many.select.isNotEmpty()) {
      throw BuilderException("Cannot use both @One and @Many annotation in the same @Result")
    }
    return result.one.select.isNotEmpty() || result.many.select.isNotEmpty()
  }

  private fun isLazy(result: Result?): Boolean {
    var isLazy = configuration.isLazyLoadingEnabled
    if (result!!.one.select.isNotEmpty() && FetchType.DEFAULT != result.one.fetchType) {
      isLazy = result.one.fetchType == FetchType.LAZY
    } else if (result.many.select.isNotEmpty() && FetchType.DEFAULT != result.many.fetchType) {
      isLazy = result.many.fetchType == FetchType.LAZY
    }
    return isLazy
  }

  private fun loadXmlResource() {
    // Spring may not know the real resource name so we check a flag
    // to prevent loading again a resource twice
    // this flag is set at XMLMapperBuilder#bindMapperForNamespace
    if (!configuration.isResourceLoaded("namespace:" + type.getName())) {
      val xmlResource = type.getName().replace('.', '/') + ".xml"
      var inputStream: InputStream? = null
      try {
        inputStream = Resources.getResourceAsStream(type.getClassLoader(), xmlResource)
      } catch (e: IOException) {
        // ignore, resource is not required
      }
      if (inputStream != null) {
        val xmlParser = XMLMapperBuilder(
            inputStream,
            assistant.configuration,
            xmlResource,
            configuration.sqlFragments,
            type.getName()
        )
        xmlParser.parse()
      }
    }
  }

  private fun nestedSelectId(result: Result?): String {
    var nestedSelect = result!!.one.select
    if (nestedSelect.isEmpty()) {
      nestedSelect = result.many.select
    }
    if (!nestedSelect.contains(StringPool.DOT)) {
      nestedSelect = type.getName() + StringPool.DOT + nestedSelect
    }
    return nestedSelect
  }

  private fun nullOrEmpty(value: String?): String? {
    return if (value == null || value.trim { it <= ' ' }.isEmpty()) {
      null
    } else {
      value
    }
  }

  private fun parseCache() {
    val cacheDomain = type.getAnnotation(CacheNamespace::class.java)
    if (cacheDomain != null) {
      val size = if (cacheDomain.size == 0) {
        null
      } else {
        cacheDomain.size
      }
      val flushInterval = if (cacheDomain.flushInterval == 0L) {
        null
      } else {
        cacheDomain.flushInterval
      }
      val props = convertToProperties(cacheDomain.properties)
      assistant.useNewCache(
          cacheDomain.implementation.java, cacheDomain.eviction.java, flushInterval, size,
          cacheDomain.readWrite, cacheDomain.blocking, props
      )
    }
  }

  private fun parseCacheRef() {
    val cacheDomainRef = type.getAnnotation(CacheNamespaceRef::class.java)
    if (cacheDomainRef != null) {
      val refType: Class<*> = cacheDomainRef.value.java
      val refName = cacheDomainRef.name
      if (refType == Void.TYPE && refName.isEmpty()) {
        throw BuilderException("Should be specified either value() or name() attribute in the @CacheNamespaceRef")
      }
      if (refType != Void.TYPE && refName.isNotEmpty()) {
        throw BuilderException("Cannot use both value() and name() attribute in the @CacheNamespaceRef")
      }
      val namespace = if (refType != Void.TYPE) {
        refType.getName()
      } else {
        refName
      }
      assistant.useCacheRef(namespace)
    }
  }

  private fun parsePendingMethods() {
    val incompleteMethods = configuration.incompleteMethods
    synchronized(configuration.incompleteMethods) {
      val iter = incompleteMethods.iterator()
      while (iter.hasNext()) {
        try {
          iter.next().resolve()
          iter.remove()
        } catch (ex: IncompleteElementException) {
          // This method is still missing a resource
        }
      }
    }
  }

  private fun parseResultMap(method: Method): String {
    val returnType = getReturnType(method)
    val args = method.getAnnotation(
        ConstructorArgs::class.java
    )
    val results = method.getAnnotation(
        Results::class.java
    )
    val typeDiscriminator: TypeDiscriminator? = method.getAnnotation(TypeDiscriminator::class.java)
    val resultMapId = generateResultMapName(method)
    applyResultMap(resultMapId, returnType, argsIf(args), resultsIf(results), typeDiscriminator)
    return resultMapId
  }

  private fun parseStatement(method: Method) {
    val parameterTypeClass = getParameterType(method)
    val languageDriver = getLanguageDriver(method)
    val sqlSource = getSqlSourceFromAnnotations(method, parameterTypeClass, languageDriver)
    if (sqlSource != null) {
      val options = method.getAnnotation(
          Options::class.java
      )
      val mappedStatementId = type.getName() + StringPool.DOT + method.name
      var fetchSize: Int? = null
      var timeout: Int? = null
      var statementType = StatementType.PREPARED
      var resultSetType = ResultSetType.FORWARD_ONLY
      val sqlCommandType = getSqlCommandType(method)
      val isSelect = sqlCommandType == SqlCommandType.SELECT
      var flushCache = !isSelect
      var useCache = isSelect
      val keyGenerator: KeyGenerator
      var keyProperty = "id"
      var keyColumn: String? = null
      if (SqlCommandType.INSERT == sqlCommandType || SqlCommandType.UPDATE == sqlCommandType) {
        // first check for SelectKey annotation - that overrides everything else
        val selectKey = method.getAnnotation(SelectKey::class.java)
        if (selectKey != null) {
          keyGenerator =
              handleSelectKeyAnnotation(selectKey, mappedStatementId, getParameterType(method), languageDriver)
          keyProperty = selectKey.keyProperty
        } else if (options == null) {
          keyGenerator = if (configuration.isUseGeneratedKeys) {
            Jdbc3KeyGenerator.INSTANCE
          } else {
            NoKeyGenerator.INSTANCE
          }
        } else {
          keyGenerator = if (options.useGeneratedKeys) {
            Jdbc3KeyGenerator.INSTANCE
          } else {
            NoKeyGenerator.INSTANCE
          }
          keyProperty = options.keyProperty
          keyColumn = options.keyColumn
        }
      } else {
        keyGenerator = NoKeyGenerator.INSTANCE
      }
      if (options != null) {
        if (FlushCachePolicy.TRUE == options.flushCache) {
          flushCache = true
        } else if (FlushCachePolicy.FALSE == options.flushCache) {
          flushCache = false
        }
        useCache = options.useCache
        fetchSize =
            if (options.fetchSize > -1 || options.fetchSize == Int.MIN_VALUE) {
              options.fetchSize
            } else {
              null
            } //issue #348
        timeout = if (options.timeout > -1) {
          options.timeout
        } else {
          null
        }
        statementType = options.statementType
        resultSetType = options.resultSetType
      }
      var resultMapId: String? = null
      val resultMapAnnotation = method.getAnnotation(
          ResultMap::class.java
      )
      if (resultMapAnnotation != null) {
        val resultMaps = resultMapAnnotation.value
        val sb = StringBuilder()
        for (resultMap in resultMaps) {
          if (sb.isNotEmpty()) {
            sb.append(StringPool.COMMA)
          }
          sb.append(resultMap)
        }
        resultMapId = sb.toString()
      } else if (isSelect) {
        resultMapId = parseResultMap(method)
      }
      assistant.addMappedStatement(
          mappedStatementId,
          sqlSource,
          statementType,
          sqlCommandType,
          fetchSize,
          timeout,  // ParameterMapID
          null,
          parameterTypeClass,
          resultMapId,
          getReturnType(method),
          resultSetType,
          flushCache,
          useCache,  // gcode issue #577
          false,
          keyGenerator,
          keyProperty,
          keyColumn,  // DatabaseID
          null,
          languageDriver,  // ResultSets
          if (options != null) {
            nullOrEmpty(options.resultSets)
          } else {
            null
          }
      )
    }
  }

  private fun resultsIf(results: Results?): Array<Result> {
    return results?.value ?: arrayOf()
  }

}

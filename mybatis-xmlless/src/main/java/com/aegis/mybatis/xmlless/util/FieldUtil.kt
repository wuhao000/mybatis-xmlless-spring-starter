package com.aegis.mybatis.xmlless.util

import com.aegis.kotlin.toWords
import com.aegis.mybatis.xmlless.annotations.*
import com.aegis.mybatis.xmlless.annotations.criteria.*
import com.aegis.mybatis.xmlless.enums.Operations
import com.aegis.mybatis.xmlless.model.*
import com.aegis.mybatis.xmlless.resolver.CriteriaResolver
import com.aegis.mybatis.xmlless.resolver.QueryResolver
import com.aegis.mybatis.xmlless.resolver.ValueHolder
import jakarta.persistence.Column
import jakarta.persistence.JoinColumn
import jakarta.persistence.Transient
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.data.annotation.CreatedDate
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Field

/**
 * Created by 吴昊 on 2023/12/8.
 */
object FieldUtil {


  fun canBeSelect(field: Field): Boolean {
    return !isTransient(field)
        && AnnotationUtils.findAnnotation(field, Count::class.java) == null
        && AnnotationUtils.findAnnotation(field, MyBatisIgnore::class.java)?.select != true
        && AnnotationUtils.findAnnotation(field, JoinColumn::class.java) == null
        && AnnotationUtils.findAnnotation(field, JoinObject::class.java) == null
        && AnnotationUtils.findAnnotation(field, JoinTableColumn::class.java) == null
        && AnnotationUtils.findAnnotation(field, JoinEntityProperty::class.java) == null
  }

  fun isInsertIgnore(field: Field): Boolean {
    return isTransient(field)
        || AnnotationUtils.findAnnotation(field, Column::class.java)?.insertable == false
        || AnnotationUtils.findAnnotation(field, JoinColumn::class.java)?.insertable == false
        || AnnotationUtils.findAnnotation(field, Count::class.java) != null
        || AnnotationUtils.findAnnotation(field, JoinObject::class.java) != null
        || AnnotationUtils.findAnnotation(field, JoinEntityProperty::class.java) != null
        || AnnotationUtils.findAnnotation(field, JoinTableColumn::class.java) != null
        || AnnotationUtils.findAnnotation(field, MyBatisIgnore::class.java)?.insert == true
  }

  fun isUpdateIgnore(field: Field): Boolean {
    return isTransient(field)
        || AnnotationUtils.findAnnotation(field, Column::class.java)?.updatable == false
        || AnnotationUtils.findAnnotation(field, JoinColumn::class.java)?.updatable == false
        || AnnotationUtils.findAnnotation(field, Count::class.java) != null
        || AnnotationUtils.findAnnotation(field, MyBatisIgnore::class.java)?.update == true
        || AnnotationUtils.findAnnotation(field, JoinObject::class.java) != null
        || AnnotationUtils.findAnnotation(field, JoinTableColumn::class.java) != null
        || AnnotationUtils.findAnnotation(field, JoinEntityProperty::class.java) != null
        || AnnotationUtils.findAnnotation(field, CreatedDate::class.java) != null
  }

  fun isSelectIgnore(field: Field): Boolean {
    return isTransient(field)
        || AnnotationUtils.findAnnotation(field, Count::class.java) != null
        || AnnotationUtils.findAnnotation(field, MyBatisIgnore::class.java)?.select == true
  }

  fun getCriteria(field: Field, methodInfo: MethodInfo): List<QueryCriteria> {
    val result = mutableListOf<QueryCriteria>()
    val criteria = field.getAnnotation(Criteria::class.java)
    if (criteria != null) {
      val properties = if (criteria.properties.isNotEmpty()) {
        criteria.properties.toList()
      } else {
        listOf(field.name)
      }
      val paramName = CriteriaResolver.chooseFromParameter(methodInfo, field.name, ValueHolder(0))
      result.addAll(
          properties.map {
            QueryCriteria(it, criteria.value, listOf(CriteriaParameter(paramName, field)), null, methodInfo)
          }
      )
    }
    return result.toList()
  }

  fun getCriteriaInfo(field: AnnotatedElement, methodInfo: MethodInfo): List<CriteriaInfo> {
    val list = field.getAnnotationsByType(TestCriteria::class.java)
    return list.map { criteria ->
      val expressionWords = QueryResolver.toPascalCaseName(criteria.expression).toWords()
      val queryCriteriaList = CriteriaResolver.resolveConditions(
          expressionWords, methodInfo
      )
      CriteriaInfo(
          QueryCriteriaGroup(queryCriteriaList),
          createTestInfo(criteria.testExpression, field),
      )
    }
  }

  private fun isTransient(field: Field): Boolean {
    return AnnotationUtils.findAnnotation(field, Transient::class.java) != null
  }

  private fun createTestInfo(expression: String, field: AnnotatedElement): TestInfo {
    return TestInfo(expression, field)
  }


}

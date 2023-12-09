package com.aegis.mybatis.xmlless.util

import com.aegis.mybatis.xmlless.annotations.*
import com.aegis.mybatis.xmlless.model.CriteriaInfo
import com.aegis.mybatis.xmlless.model.TestInfo
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
        && AnnotationUtils.findAnnotation(field, JoinProperty::class.java) == null
        && AnnotationUtils.findAnnotation(field, JoinTableColumn::class.java) == null
        && AnnotationUtils.findAnnotation(field, JoinEntityProperty::class.java) == null
  }

  fun isInsertIgnore(field: Field): Boolean {
    return isTransient(field)
        || AnnotationUtils.findAnnotation(field, Column::class.java)?.insertable == false
        || AnnotationUtils.findAnnotation(field, JoinColumn::class.java)?.insertable == false
        || AnnotationUtils.findAnnotation(field, Count::class.java) != null
        || AnnotationUtils.findAnnotation(field, JoinObject::class.java) != null
        || AnnotationUtils.findAnnotation(field, JoinProperty::class.java) != null
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
        || AnnotationUtils.findAnnotation(field, JoinProperty::class.java) != null
        || AnnotationUtils.findAnnotation(field, JoinTableColumn::class.java) != null
        || AnnotationUtils.findAnnotation(field, JoinEntityProperty::class.java) != null
        || AnnotationUtils.findAnnotation(field, CreatedDate::class.java) != null
  }

  fun isSelectIgnore(field: Field): Boolean {
    return isTransient(field)
        || AnnotationUtils.findAnnotation(field, Count::class.java) != null
        || AnnotationUtils.findAnnotation(field, MyBatisIgnore::class.java)?.select == true
  }

  fun getCriteriaInfo(field: AnnotatedElement): List<CriteriaInfo> {
    val list = field.getAnnotationsByType(Criteria::class.java)
    return list.map { criteria ->
      // todo
//      val expressionWords = QueryResolver.toPascalCaseName(criteria.expression).toWords()
//      CriteriaResolver.resolveConditions(
//          expressionWords, methodInfo, mappings, resolveTypeResult.type
//      )
      CriteriaInfo(
          criteria.property,
          criteria.expression,
          criteria.operator,
          createTestInfo(criteria.test, field),
      )
    }
  }

  private fun isTransient(field: Field): Boolean {
    return AnnotationUtils.findAnnotation(field, Transient::class.java) != null
  }

  private fun createTestInfo(test: TestExpression, field: AnnotatedElement): TestInfo {
    return TestInfo(test.value, test.expression, field)
  }


}

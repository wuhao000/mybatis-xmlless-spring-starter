package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.bean.Score
import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.bean.StudentVO
import com.aegis.mybatis.dao.BaseStudentDAO
import com.aegis.mybatis.dao.SimpleStudent
import com.aegis.mybatis.dao.StudentBaseMapperDAO
import com.aegis.mybatis.dao.StudentDAO
import com.aegis.mybatis.xmlless.model.MethodInfo
import com.aegis.mybatis.xmlless.model.Properties
import com.aegis.mybatis.xmlless.resolver.ColumnsResolver
import com.aegis.mybatis.xmlless.resolver.QueryResolver
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.reflect.jvm.javaMethod
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


/**
 *
 * Created by 吴昊 on 2018-12-09.
 *
 * @author 吴昊
 * @since 0.0.1
 */
class SimpleStudentDAOResolverTest : BaseResolverTest(
    StudentBaseMapperDAO::class.java,
    SimpleStudent::class.java,
) {

  @Test
  fun resolveFindByNameLikeAndAgeAndCreateTimeBetweenStartAndEnd() {
    val q = createQueryForMethod(StudentBaseMapperDAO::insert.javaMethod!!)
    println(q)
  }


}

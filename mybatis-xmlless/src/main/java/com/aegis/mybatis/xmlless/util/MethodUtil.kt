package com.aegis.mybatis.xmlless.util

import com.aegis.mybatis.xmlless.annotations.JsonResult
import org.springframework.core.annotation.AnnotationUtils
import java.lang.reflect.Method

/**
 * Created by 吴昊 on 2023/12/8.
 */
object MethodUtil {

  fun isJsonResult(method: Method): Boolean {
    return AnnotationUtils.findAnnotation(method, JsonResult::class.java) != null
  }

}

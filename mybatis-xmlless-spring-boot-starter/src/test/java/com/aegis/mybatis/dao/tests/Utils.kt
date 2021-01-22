package com.aegis.mybatis.dao.tests

import com.aegis.mybatis.dao.DogDAO
import org.junit.jupiter.api.Test
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.javaMethod


/**
 * Created by 吴昊 on 2018/12/17.
 */
open class Utils {

  @Test
  fun findAllPageable() {
    DogDAO::class.declaredFunctions.forEach {
      println(it.name)
    }
    println("========")
    DogDAO::class.functions.forEach {
      println(it.name)
      println(it.javaMethod?.declaringClass == Object::class.java)
    }
  }

}

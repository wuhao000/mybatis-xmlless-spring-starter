package com.aegis.mybatis.xmlless.enums


/**
 * Created by 吴昊 on 2018/12/19.
 */
enum class TestType(val expression: String) {

  EqFalse("== FALSE"),
  EqTrue(""),
  GtZero(" &gt; 0"),
  GteZero(" &gt;= 0"),
  IsNull(" == null"),
  LtZero(" &lt; 0"),
  LteZero(" &lt;= 0"),
  NotNull(" != null"),
  NotEmpty("");

}

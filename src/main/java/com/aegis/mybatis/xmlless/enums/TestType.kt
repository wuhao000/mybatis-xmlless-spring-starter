package com.aegis.mybatis.xmlless.enums

/**
 * Created by 吴昊 on 2018/12/19.
 */
enum class TestType(val expression:String) {

  NotNull(" != null"),
  IsNull(" = null"),
  CollectionNotEmpty(".size() > 0"),
  ArrayNotEmpty(".length > 0"),
  StringNotEmpty(".length() > 0"),
  GtZero(" > 0"),
  LtZero(" < 0"),
  GteZero(" >= 0"),
  LteZero(" <= 0")

}

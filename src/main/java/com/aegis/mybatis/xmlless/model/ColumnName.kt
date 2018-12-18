package com.aegis.mybatis.xmlless.model


/**
 * Created by 吴昊 on 2018/12/18.
 */
class ColumnName(name: String, alias: String) : NameAlias(name, alias) {

  companion object {
    fun resolve(columnString: String): ColumnName {
      val nameAlias = NameAlias.resolve(columnString)
      return ColumnName(nameAlias.name, nameAlias.alias)
    }
  }

}

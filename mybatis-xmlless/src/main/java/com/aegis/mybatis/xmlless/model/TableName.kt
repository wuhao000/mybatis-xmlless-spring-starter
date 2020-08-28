package com.aegis.mybatis.xmlless.model


/**
 * 数据库表名称
 * @author 吴昊
 * @since 0.0.3
 * @param name 表名称
 * @param alias 表别名
 */
class TableName(name: String, alias: String) : NameAlias(name, alias) {

  companion object {
    fun resolve(targetTable: String,
                aliasPrefix: String? = null): TableName {
      val nameAlias = NameAlias.resolve(targetTable, aliasPrefix)
      return TableName(nameAlias.name, nameAlias.alias)
    }
  }

}

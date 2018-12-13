package com.aegis.mybatis.xmlless.enums


/**
 * 数据库join操作的类型
 * @author 吴昊
 * @since 0.0.2
 */
enum class JoinType {

  Inner,
  /** 左连接 */
  Left,
  /** 右连接 */
  Right;

}

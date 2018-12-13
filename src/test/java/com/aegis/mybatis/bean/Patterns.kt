package com.aegis.mybatis.bean

/**
 * 请求路径配置
 * @author 吴昊
 * @since 0.0.2
 */
data class Patterns(
    /**  所有请求方式都不受保护的路径 */
    var all: List<String> = listOf(),
    /**  get请求不受保护的路径 */
    var get: List<String> = listOf(),
    /**  post请求不受保护的路径 */
    var post: List<String> = listOf(),
    /**  put请求不受保护的路径 */
    var put: List<String> = listOf(),
    /**  delete请求不受保护的路径 */
    var delete: List<String> = listOf()) {

  constructor() : this(listOf(), listOf(), listOf(), listOf(), listOf())

}

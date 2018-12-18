package com.aegis.mybatis.xmlless.constant


/**
 *
 * Created by 吴昊 on 2018-12-14.
 *
 * @author 吴昊
 * @since 0.0.8
 */
/**  insert 语句模板 */
const val BATCH_INSERT = """<script>
INSERT INTO
  %s(%s)
VALUES
  <foreach collection="list" item="item" separator=",">
    (%s)
  </foreach>
</script>
"""

/**  删除语句模板 */
const val DELETE = """<script>
DELETE FROM
  %s
%s
</script>"""

/**  单条数据插入语句模板 */
const val INSERT = """INSERT INTO
  %s(%s)
VALUE
  (%s)"""

/**  join表达式模板 */
const val JOIN = """
  %S JOIN
    %s
  ON
    %s.%s = %s.%s
"""

/**  limit 语句模板 */
const val LIMIT = "LIMIT #{%s}, #{%s}"

/**  Pageable参数中的排序 */
const val PAGEABLE_SORT = """<if test="%s.sort.isSorted">
  <foreach collection="%s.sort.get().toArray()" item="item" separator=",">
    ${'$'}{item.property} <if test="item.isAscending">ASC</if><if test="item.isDescending">DESC</if>
  </foreach>
</if>"""

/**  查询语句模板 */
const val SELECT = """<script>
SELECT
  %s
FROM
  %s %s
%s %s %s
%s
</script>"""

/**  count语句模板 */
const val SELECT_COUNT = """<script>
SELECT
  COUNT(*)
FROM
  %s
%s
</script>"""

/**  子查询构建模板 */
const val SUB_QUERY = """(SELECT
  *
FROM
  %s
%s
%s) AS %s"""

/**  更新语句模板 */
const val UPDATE = """<script>
UPDATE
  %s
  %s
%s
</script>"""

/**  where条件模板 */
const val WHERE = """<where>
  <trim suffixOverrides=" AND">
    <trim suffixOverrides=" OR">
      %s
    </trim>
  </trim>
</where>"""

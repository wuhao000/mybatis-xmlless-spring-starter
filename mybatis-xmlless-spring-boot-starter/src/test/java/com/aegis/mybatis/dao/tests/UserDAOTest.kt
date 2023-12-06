package com.aegis.mybatis.dao.tests

import com.aegis.mybatis.BaseTest
import com.aegis.mybatis.bean.Role
import com.aegis.mybatis.bean.User
import com.aegis.mybatis.dao.RoleDAO
import com.aegis.mybatis.dao.UserDAO
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import jakarta.annotation.Resource
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 *
 * Created by 吴昊 on 2018/12/14.
 *
 * @author 吴昊
 * @since 0.0.7
 */
class UserDAOTest : BaseTest() {

  private val testId = 16

  @Resource
  private lateinit var dao: UserDAO
  @Resource
  private lateinit var roleDAO: RoleDAO

  /**
   * 根据 entity 条件，删除记录
   *
   * @param wrapper 实体对象封装操作类（可以为 null）
   */
  fun delete() {
  }

  /**
   * 删除（根据ID 批量删除）
   *
   * @param idList 主键ID列表(不能为 null 以及 empty)
   */
  fun deleteBatchIds() {
  }

  /**
   * 根据 ID 删除
   *
   * @param id 主键ID
   */
  fun deleteById() {
  }

  /**
   * 根据 columnMap 条件，删除记录
   *
   * @param columnMap 表字段 map 对象
   */
  fun deleteByMap() {
  }

  @Test
  fun findAllNames() {
    val names = dao.findAllNames()
    log.info(names.toString())
    assert(names.size == dao.count())
  }

  @Test
  fun findAll() {
    dao.save(User(name = "w", deleted = false))
    dao.save(User(name = "wt", deleted = false))
    dao.save(User(name = "wtf", deleted = false))
    val page = dao.findAll(
        PageRequest.of(
            0, 20, Sort.Direction.DESC, "id"
        )
    )
    assert(page.content.isNotEmpty())
  }

  @Test
  fun pageable() {
    val role = Role(name = "我是管理员")
    roleDAO.save(role)
    val user = User(name = "w", deleted = false)
    user.roles = listOf(role.id)
    dao.save(user)
    val user2 = dao.findById(user.id)
    assertNotNull(user2)
    assertNotNull(user2.roles)
    assert(user2.roles.isNotEmpty())
    assert(user2.roleList.isNotEmpty())
  }

  @Test
  fun save() {
    val user = User(
        name = "w",
        deleted = false
    )
    dao.save(user)
    assert(user.id > 0)
    assertNotNull(dao.findSimpleUserById(user.id))
    dao.deleteById(user.id)
  }

  @Test
  fun findById() {
    val user = User(
        id = 99988,
        name = "b"
    )
    dao.save(user)
    assertEquals(99988, user.id)
    val user2 = User(name = "c")
    dao.save(user2)
    assert(user2.id > 0)
  }

  @Test
  fun saveAll() {
    val user1 = User(
        name = "test",
        deleted = false
    )
    val user2 = User(
        name = "w",
        deleted = false
    )
    dao.saveAll(listOf(user1, user2))
    assert(user1.id > 0)
    assert(user2.id > 0)
    dao.deleteById(user1.id)
    dao.deleteById(user2.id)
  }

  @Test
  fun saveBatch() {
    val user1 = User(
        name = "test",
        deleted = false
    )
    val user2 = User(
        name = "w",
        deleted = false
    )
    dao.saveBatch(listOf(user1, user2))
    assert(user1.id > 0)
    assert(user2.id > 0)
    dao.deleteById(user1.id)
    dao.deleteById(user2.id)
  }


  /**
   * 查询（根据 columnMap 条件）
   *
   * @param columnMap 表字段 map 对象
   */
  fun selectByMap() {
  }

  /**
   * 根据 Wrapper 条件，查询总记录数
   *
   * @param queryWrapper 实体对象封装操作类（可以为 null）
   */
  fun selectCount() {
  }

  /**
   * 根据 entity 条件，查询全部记录
   *
   * @param queryWrapper 实体对象封装操作类（可以为 null）
   */
  fun selectList() {
  }

  /**
   * 根据 Wrapper 条件，查询全部记录
   *
   * @param queryWrapper 实体对象封装操作类（可以为 null）
   */
  fun selectMaps() {
  }



  /**
   * 根据 whereEntity 条件，更新记录
   *
   * @param entity        实体对象 (set 条件值,可以为 null)
   * @param updateWrapper 实体对象封装操作类（可以为 null,里面的 entity 用于生成 where 语句）
   */
  fun update() {
  }

  /**
   * 根据 ID 修改
   *
   * @param entity 实体对象
   */
  fun updateById() {
  }

  companion object {
    private val log: Logger = LoggerFactory.getLogger(UserDAOTest::class.java)
  }

}

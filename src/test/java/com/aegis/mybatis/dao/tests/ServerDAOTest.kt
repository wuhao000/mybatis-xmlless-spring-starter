package com.aegis.mybatis.dao.tests

import com.aegis.mybatis.bean.Server
import com.aegis.mybatis.dao.ServerDAO
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


/**
 * Created by 吴昊 on 2018/12/17.
 */
class ServerDAOTest : BaseTest() {

  @Autowired
  private lateinit var serverDAO: ServerDAO

  @Test
  fun findById() {
    val server1 = serverDAO.findById(1)
    val server2 = serverDAO.findById(2)
    assertNotNull(server1)
    assertNotNull(server2)
    println(server1.provider)
    println(server2.provider)
    assertNotNull(server1.provider)
    assertNotNull(server2.provider)
    assertNotNull(server2.parent)
    assertNotNull(server2.parent)
    assertEquals(server1.name, server2.parent?.name)
  }

  @Test
  fun save() {
    val name = "server3"
    val server = Server(name = name, ip = "192.168.1.11", parentId = 2, providerId = 1)
    if (!serverDAO.existsByName(name)) {
      serverDAO.save(server)
    }
    val server3 = serverDAO.findByName(name)
    assertNotNull(server3)
    assertEquals(2, server3.parentId)
  }

  @Test
  fun update() {
    val name = "server4"
    val newName = "server6"
    val newIp = "114.114.114.114"
    createServerForName(name)
    val server4 = serverDAO.findByName(name)
    assertNotNull(server4)
    assertEquals(name, server4.name)
    val updateResult = serverDAO.update(
        Server(server4.id, newName, newIp, null, 2)
    )
    assertEquals(1, updateResult)
    val server5 = serverDAO.findById(server4.id)
    assertEquals(newName, server5?.name)
    assertEquals(newIp, server5?.ip)
    serverDAO.deleteByName(newName)
  }

  private fun createServerForName(name: String) {
    val server = Server(name = name, ip = "180.11.43.22", providerId = 2)
    if (!serverDAO.existsByName(name)) {
      serverDAO.save(server)
    }
  }

  @Test
  fun findAllSimple() {
  }
}

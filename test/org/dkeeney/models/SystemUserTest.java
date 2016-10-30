package org.dkeeney.models;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SystemUserTest {
  private String userName;
  private String password;
  private String salt;

  @Before
  public void before() {
    userName = RandomStringUtils.randomAlphanumeric(10);
    password = RandomStringUtils.randomAlphanumeric(10);
    salt = RandomStringUtils.randomAlphabetic(5);
  }

  private void assertSystemUser(SystemUser systemUser) {
    assertEquals("Wrong username saved", userName, systemUser.getUserName());
    assertEquals("Wrong password saved", password, systemUser.getEncryptedPassword());
    assertEquals("Wrong salt saved", salt, systemUser.getSalt());
  }

  @Test
  public void testSettersAndGetters() {
    SystemUser systemUser = new SystemUser();
    systemUser.setUserName(userName);
    systemUser.setEncryptedPassword(password);
    systemUser.setSalt(salt);

    assertSystemUser(systemUser);
  }

  @Test
  public void testConstructor() {
    SystemUser systemUser = new SystemUser(userName, password, salt);

    assertSystemUser(systemUser);
  }
}
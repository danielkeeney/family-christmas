package org.dkeeney.dao;

import org.dkeeney.models.SystemUser;
import org.dkeeney.utils.LoginDaoHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class LoginDaoTest {
  @Rule
  public final TemporaryFolder tempFolder = new TemporaryFolder();

  private LoginDaoHelper loginDaoHelper;
  private File tempFile;
  private LoginDao loginDao;

  @Before
  public void before() throws IOException {
    tempFile = tempFolder.newFile();
    loginDaoHelper = new LoginDaoHelper(tempFile);
    loginDaoHelper.prepareUsers(
        Arrays.asList(
            new SystemUser("username", "password", "salt1"),
            new SystemUser("daniel", "keeney", "salt2"))
    );
    loginDao = new LoginDao(tempFile.getPath());
  }

  @Test
  public void testMissingInputFile() {
    loginDao = new LoginDao("missing.json");
    List<SystemUser> allUsers = loginDao.getAllUsers();
    assertNotNull("Should still load an empty list of users", allUsers);
    assertTrue("Should not load any users from a missing file", allUsers.isEmpty());
  }

  @Test
  public void testUsersLoadedFromFile() throws IOException {
    assertEquals("Did not load all users", 2, loginDao.getAllUsers().size());
    loginDaoHelper.assertSavedUsers(loginDao.getAllUsers(),
        new SystemUser("username", "password", "salt1"),
        new SystemUser("daniel", "keeney", "salt2")
    );
  }

  @Test
  public void testChangePasswordSuccess() throws IOException {
    loginDao.changePassword("username", "password1", "salt3");
    loginDaoHelper.assertSavedUsers(loginDao.getAllUsers(),
        new SystemUser("username", "password1", "salt3"),
        new SystemUser("daniel", "keeney", "salt2")
    );
  }

  @Test
  public void testChangePasswordFailure() throws IOException {
    loginDao.changePassword("missingPerson", "newPassword", "salt0");
    loginDaoHelper.assertSavedUsers(loginDao.getAllUsers(),
        new SystemUser("username", "password", "salt1"),
        new SystemUser("daniel", "keeney", "salt2")
    );
  }

  @Test
  public void testRegisterSuccess() throws IOException {
    boolean registered = loginDao.registerUser("newUser", "newPassword", "salt3");
    assertTrue("Should return true on success", registered);
    loginDaoHelper.assertSavedUsers(loginDao.getAllUsers(),
        new SystemUser("username", "password", "salt1"),
        new SystemUser("daniel", "keeney", "salt2"),
        new SystemUser("newUser", "newPassword", "salt3")
    );
  }

  @Test
  public void testRegisterFailure() throws IOException {
    boolean registered = loginDao.registerUser("username", "differentPassword", "salt0");
    assertFalse("Should return false on failure", registered);
    loginDaoHelper.assertSavedUsers(loginDao.getAllUsers(),
        new SystemUser("username", "password", "salt1"),
        new SystemUser("daniel", "keeney", "salt2")
    );
  }
}
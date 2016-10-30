package org.dkeeney.services;

import org.dkeeney.dao.LoginDao;
import org.dkeeney.models.SystemUser;
import org.dkeeney.utils.LoginDaoHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import play.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.Assert.*;

public class LoginServiceTest {
  @Rule
  public final TemporaryFolder temporaryFolder = new TemporaryFolder();

  private LoginDaoHelper loginDaoHelper;
  private File tempFile;
  private LoginService loginService;
  private LoginDao loginDao;
  private SystemUser[] defaultUsers;

  @Before
  public void before() throws IOException {
    tempFile = temporaryFolder.newFile();
    loginDaoHelper = new LoginDaoHelper(tempFile);
    defaultUsers = new SystemUser[]{
        new SystemUser("username", "90a3bac72709a92dd39e2a666dd656f7", "1234"),
        new SystemUser("daniel", "733c9ab3da301af523ff61484297378f", "4321ae")
    };
    loginDaoHelper.prepareUsers(Arrays.asList(defaultUsers));
    loginDao = new LoginDao(tempFile.getPath());
    loginService = new LoginService(
        loginDao,
        new Configuration(new HashMap<>())
    );
  }

  @Test
  public void testSuccessfulLogin() {
    Optional<String> user = loginService.login("username", "password");
    assertTrue("Should return an optional on successful authentication", user.isPresent());
    assertEquals("Should return an authenticated username on success", "username", user.get());
  }

  @Test
  public void testLoginUnknownUser() {
    Optional<String> user = loginService.login("unknownUser", "password");
    assertFalse("Should return empty optional for unknown user", user.isPresent());
  }

  @Test
  public void testLoginInvalidPassword() {
    Optional<String> user = loginService.login("username", "password1");
    assertFalse("Should return empty optional for wrong password", user.isPresent());
  }

  @Test
  public void testRegisterBlankPassword() {
    Optional<String> user = loginService.register("newUser", "", "");
    assertFalse("Should return empty optional for blank password", user.isPresent());
    assertFalse("Should not be saved", loginDao.findUser("newUser").isPresent());
  }

  @Test
  public void testRegisterMismatchedPasswords() {
    Optional<String> user = loginService.register("newUser", "password1", "password2");
    assertFalse("Should return empty optional for mismatched passwords", user.isPresent());
    assertFalse("Should not be saved", loginDao.findUser("newUser").isPresent());
  }

  @Test
  public void testRegisterExistingUser() {
    Optional<String> user = loginService.register("username", "password", "password");
    assertFalse("Should return empty optional for already registered user", user.isPresent());
  }

  @Test
  public void testRegisterSuccess() {
    Optional<String> user = loginService.register("newUser", "newPassword", "newPassword");
    assertTrue("Should return a username when registration succeeds", user.isPresent());
    assertEquals("Should return the registered name when successful", "newUser", user.get());
    Optional<SystemUser> savedUserOptional = loginDao.findUser("newUser");
    assertTrue("Should be saved", savedUserOptional.isPresent());
    SystemUser savedUser = savedUserOptional.get();
    assertNotEquals("Should not save passwords in clear text", "newPassword", savedUser.getEncryptedPassword());
  }

  @Test
  public void testChangePasswordInvalidUser() throws IOException {
    boolean changed = loginService.changePassword("daniel1", "keeney", "keeney");
    assertFalse("Should not change passwords for unknown user", changed);
    loginDaoHelper.assertSavedUsers(loginDao.getAllUsers(), defaultUsers);
  }

  @Test
  public void testChangePasswordBlankPassword() throws IOException {
    boolean changed = loginService.changePassword("daniel", "", "");
    assertFalse("Should not change passwords for blank password", changed);
    loginDaoHelper.assertSavedUsers(loginDao.getAllUsers(), defaultUsers);
  }

  @Test
  public void testChangePasswordMismatchedPasswords() throws IOException {
    boolean changed = loginService.changePassword("daniel", "keeney1", "keeney");
    assertFalse("Should not change passwords for mismatched passwords", changed);
    loginDaoHelper.assertSavedUsers(loginDao.getAllUsers(), defaultUsers);
  }

  @Test
  public void testChangePasswordSuccess() throws IOException {
    boolean changed = loginService.changePassword("daniel", "keeney1", "keeney1");
    assertTrue("Should change passwords successfully", changed);
    SystemUser adjustedUser = loginDao.findUser("daniel").get();
    String newPassword = adjustedUser.getEncryptedPassword();
    String newSalt = adjustedUser.getSalt();
    assertNotEquals("Should not have the same encrypted password as before",
        "733c9ab3da301af523ff61484297378f", newPassword);
    assertNotEquals("Should not save new password in clear text",
        "keeney1", newPassword);
    loginDaoHelper.assertSavedUsers(loginDao.getAllUsers(),
        new SystemUser("username", "90a3bac72709a92dd39e2a666dd656f7", "1234"),
        new SystemUser("daniel", newPassword, newSalt)
    );
  }
}
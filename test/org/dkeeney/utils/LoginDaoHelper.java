package org.dkeeney.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dkeeney.models.SystemUser;
import org.dkeeney.models.wrapper.LoginWrapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LoginDaoHelper {
  private ObjectMapper objectMapper = new ObjectMapper();
  private File tempFile;

  public LoginDaoHelper(File tempFile) {
    this.tempFile = tempFile;
  }

  public void prepareUsers(List<SystemUser> users)
      throws IOException {
    LoginWrapper wrapper = new LoginWrapper(users);
    Files.write(
        tempFile.toPath(),
        objectMapper.valueToTree(wrapper)
            .toString()
            .getBytes("UTF-8")
    );
  }

  public List<SystemUser> readUsers() throws IOException {
    return objectMapper.readValue(
        Files.readAllBytes(Paths.get(tempFile.getAbsolutePath())),
        LoginWrapper.class
    ).getUsers();
  }

  public void assertSavedUsers(List<SystemUser> daoUsers, SystemUser... users) throws IOException {
    List<SystemUser> savedUsers = readUsers();
    assertEquals("Wrong number of users in memory", users.length, daoUsers.size());
    assertEquals("Wrong number of users in disk", users.length, savedUsers.size());
    for (SystemUser user : users) {
      assertEquals("Wrong password in memory for " + user.getUserName(),
          user.getEncryptedPassword(), findUser(user.getUserName(), daoUsers).getEncryptedPassword());
      assertEquals("Wrong salt in memory for " + user.getUserName(),
          user.getSalt(), findUser(user.getUserName(), daoUsers).getSalt());
      assertEquals("Wrong password in disk for " + user.getUserName(),
          user.getEncryptedPassword(), findUser(user.getUserName(), savedUsers).getEncryptedPassword());
      assertEquals("Wrong salt in disk for " + user.getUserName(),
          user.getSalt(), findUser(user.getUserName(), savedUsers).getSalt());
    }
  }

  private SystemUser findUser(String userName, List<SystemUser> users) {
    return users.stream()
        .filter(user -> userName.equals(user.getUserName()))
        .findFirst().get();
  }
}

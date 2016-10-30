package org.dkeeney.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.dkeeney.models.SystemUser;
import org.dkeeney.models.wrapper.LoginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Configuration;
import play.inject.ApplicationLifecycle;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Singleton
public class LoginDao {
  public static final String LOCATION_KEY = "login.save.location";
  private static final Logger LOGGER = LoggerFactory.getLogger(LoginDao.class);
  private final ObjectMapper objectMapper;
  private final String saveLocation;
  private List<SystemUser> users;

  public LoginDao() {
    this(new Configuration(new HashMap<>()));
  }

  @Inject
  public LoginDao(Configuration config, ApplicationLifecycle lifecycle) {
    this(config);
    lifecycle.addStopHook(() -> {
      this.save();
      return null;
    });
  }

  public LoginDao(Configuration config) {
    this(config.getString(LOCATION_KEY, "login.json"));
  }

  public LoginDao(String filePath) {
    this.saveLocation = filePath;
    this.objectMapper = new ObjectMapper();
    load();
  }

  public List<SystemUser> getAllUsers() {
    return users;
  }

  public boolean registerUser(String userName, String encryptedPassword, String salt) {
    synchronized (this) {
      if (!findUser(userName).isPresent()) {
        this.users.add(new SystemUser(userName, encryptedPassword, salt));
        save();
        return true;
      } else {
        LOGGER.warn("Attempt to register with an already taken username: {}", userName);
        return false;
      }
    }
  }

  public boolean changePassword(String userName, String newPassword, String newSalt) {
    synchronized (this) {
      Optional<SystemUser> userToChange = findUser(userName);
      if (userToChange.isPresent()) {
        userToChange.get().setEncryptedPassword(newPassword);
        userToChange.get().setSalt(newSalt);
        return save();
      } else {
        LOGGER.warn("Attempt to change password for unknown user {}", userName);
        return false;
      }
    }
  }

  public Optional<SystemUser> findUser(String userName) {
    return users.stream()
        .filter(user -> userName.equals(user.getUserName()))
        .findFirst();
  }

  private boolean save() {
    synchronized (this) {
      try {
        Files.write(
            Paths.get(saveLocation),
            objectMapper.valueToTree(new LoginWrapper(this.users))
                .toString()
                .getBytes("UTF-8")
        );
        return true;
      } catch (IOException e) {
        LOGGER.warn("Unable to save!", e);
        return false;
      }
    }
  }

  private void load() {
    synchronized (this) {
      this.users = new ArrayList<>();
      try {
        LoginWrapper loginWrapper =
            objectMapper.readValue(Files.readAllBytes(Paths.get(saveLocation)), LoginWrapper.class);
        this.users = loginWrapper.getUsers();
      } catch (IOException e) {
        LOGGER.warn("Unable to read {}: {}", saveLocation, e.getMessage());
      }
    }
  }
}

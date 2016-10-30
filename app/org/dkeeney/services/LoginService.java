package org.dkeeney.services;

import org.apache.commons.lang3.StringUtils;
import org.dkeeney.dao.LoginDao;
import org.dkeeney.models.SystemUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Configuration;

import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;

public class LoginService {
  private static final Logger LOGGER = LoggerFactory.getLogger(LoginService.class);
  private final String hashAlgorithm;
  private final String saltAlgorithm;
  private final LoginDao loginDao;

  @Inject
  public LoginService(LoginDao loginDao, Configuration config) {
    this.loginDao = loginDao;
    hashAlgorithm = config.getString("algorithm.hash", "MD5");
    saltAlgorithm = config.getString("algorithm.salt", "SHA1PRNG");
  }

  public Optional<String> register(String username, String password, String repeat) {
    if (StringUtils.isBlank(password)) {
      LOGGER.warn("Trying to register with a blank password");
      return Optional.empty();
    }
    if (!password.equals(repeat)) {
      LOGGER.info("Trying to register with mismatched passwords");
      return Optional.empty();
    }
    byte[] saltBytes = getSalt();
    String saltString = bytesToHexString(saltBytes);
    String encryptedPassword = generateHashedPassword(password, saltBytes);
    if (loginDao.registerUser(username, encryptedPassword, saltString)) {
      return Optional.of(username);
    } else {
      return Optional.empty();
    }
  }

  public Optional<String> login(String username, String password) {
    Optional<SystemUser> userOptional = loginDao.findUser(username);
    if (!userOptional.isPresent()) {
      LOGGER.warn("Attempting to log in with unrecognized user {}", username);
      return Optional.empty();
    }
    SystemUser user = userOptional.get();
    String encryptedPassword = generateHashedPassword(
        password,
        DatatypeConverter.parseHexBinary(user.getSalt())
    );
    if (encryptedPassword.equals(user.getEncryptedPassword())) {
      return Optional.of(username);
    } else {
      LOGGER.warn("Attempting to log in with invalid password for user {}", username);
    }
    return Optional.empty();
  }

  public boolean changePassword(String username, String newPassword, String repeat) {
    if (StringUtils.isBlank(newPassword)) {
      LOGGER.info("Trying to change password with blank password");
      return false;
    }
    if (!newPassword.equals(repeat)) {
      LOGGER.info("Trying to change password with mismatched passwords");
      return false;
    }
    byte[] saltBytes = getSalt();
    String saltString = bytesToHexString(saltBytes);
    String encryptedPassword = generateHashedPassword(newPassword, saltBytes);
    return loginDao.changePassword(username, encryptedPassword, saltString);
  }

  private String generateHashedPassword(String passwordToHash, byte[] salt) {
    String generatedPassword = "";
    if (salt == null) {
      return generatedPassword;
    }
    try {
      MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
      md.update(salt);
      byte[] bytes = md.digest(passwordToHash.getBytes());
      generatedPassword = bytesToHexString(bytes);
    } catch (NoSuchAlgorithmException e) {
      LOGGER.warn("Unsupported hash algorithm {}", hashAlgorithm, e);
    }
    return generatedPassword;
  }

  private String bytesToHexString(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < bytes.length; i++) {
      sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
    }
    return sb.toString();
  }

  private byte[] getSalt() {
    try {
      SecureRandom sr = SecureRandom.getInstance(saltAlgorithm);
      byte[] salt = new byte[16];
      sr.nextBytes(salt);
      return salt;
    } catch (NoSuchAlgorithmException e) {
      LOGGER.warn("Unsupported salt algorithm {}", saltAlgorithm, e);
      return null;
    }
  }
}

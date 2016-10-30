package org.dkeeney.models;

public class SystemUser {
  private String userName;
  private String encryptedPassword;
  private String salt;

  public SystemUser() {
  }

  public SystemUser(String userName, String encryptedPassword, String salt) {
    this.userName = userName;
    this.encryptedPassword = encryptedPassword;
    this.salt = salt;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getUserName() {
    return userName;
  }

  public void setEncryptedPassword(String encryptedPassword) {
    this.encryptedPassword = encryptedPassword;
  }

  public String getEncryptedPassword() {
    return encryptedPassword;
  }

  public String getSalt() {
    return salt;
  }

  public void setSalt(String salt) {
    this.salt = salt;
  }
}

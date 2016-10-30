package org.dkeeney.models.wrapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.dkeeney.models.SystemUser;

import java.util.List;

public class LoginWrapper {
  @JsonProperty("users")
  private List<SystemUser> users;

  public LoginWrapper() {
  }

  public LoginWrapper(List<SystemUser> users) {
    this.users = users;
  }

  public List<SystemUser> getUsers() {
    return users;
  }

  public void setUsers(List<SystemUser> users) {
    this.users = users;
  }
}

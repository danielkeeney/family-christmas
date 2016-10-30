package org.dkeeney.services;

import play.Configuration;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AccessControlService {
  private final List<String> superUsers;

  @Inject
  public AccessControlService(Configuration config) {
    superUsers = config.getStringList("super.user.names", new ArrayList<>())
        .stream()
        .map(this::clean)
        .collect(Collectors.toList());
  }

  private String clean(String input) {
    return input.trim().toLowerCase();
  }

  public boolean isSuperUser(String name) {
    return superUsers.contains(clean(name));
  }
}

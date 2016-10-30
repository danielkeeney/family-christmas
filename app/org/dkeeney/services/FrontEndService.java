package org.dkeeney.services;

import org.apache.commons.lang3.StringUtils;
import org.dkeeney.models.FamilyMember;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FrontEndService {
  public Map<String, String> prepareForDisplay(Map<List<FamilyMember>, FamilyMember> assignments) {
    Map<String, String> ret = new HashMap<>();
    assignments.forEach((givers, receiver) ->
        ret.put(StringUtils.join(
            givers.stream()
                .map(FamilyMember::getShortName)
                .collect(Collectors.toList()),
            ", "), receiver.getShortName()));
    return ret;
  }

  public Map<List<FamilyMember>, FamilyMember> filterResults(
      Map<List<FamilyMember>, FamilyMember> assignments,
      String user) {
    Map<List<FamilyMember>, FamilyMember> ret = new HashMap<>();
    assignments.forEach((givers, receiver) -> {
      if (givers.stream()
          .map(FamilyMember::getShortName)
          .map(this::clean)
          .filter(name -> name.equals(clean(user)))
          .findFirst()
          .isPresent()) {
        ret.put(givers, receiver);
      }
    });
    return ret;
  }

  private String clean(String input) {
    return input.trim().toLowerCase();
  }
}

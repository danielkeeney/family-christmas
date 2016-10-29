package org.dkeeney.services;

import org.dkeeney.dao.FamilyDao;
import org.dkeeney.models.AgeGroup;
import org.dkeeney.models.FamilyMember;
import org.dkeeney.models.Gender;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.dkeeney.models.AgeGroup.ADULT;

public class FamilyChristmas {
  private final List<FamilyMember> familyMembers;

  @Inject
  public FamilyChristmas(FamilyDao familyDao) throws IOException {
    familyMembers = familyDao.loadFamily();
  }

  public Set<FamilyMember> filterAge(AgeGroup ageGroup) {
    return filterAge(ageGroup, familyMembers);
  }

  private Set<FamilyMember> filterAge(AgeGroup ageGroup, List<FamilyMember> initialSet) {
    return initialSet.stream()
        .filter(member -> ageGroup.equals(member.getAgeGroup()))
        .collect(Collectors.toSet());
  }

  public Set<FamilyMember> filterGender(Gender gender) {
    return filterGender(gender, familyMembers);
  }

  private Set<FamilyMember> filterGender(Gender gender, List<FamilyMember> initialSet) {
    return initialSet.stream()
        .filter(member -> gender.equals(member.getGender()))
        .collect(Collectors.toSet());
  }

  public Map<FamilyMember, FamilyMember> assignAdults() {
    Map<FamilyMember, FamilyMember> ret = new HashMap<>();
    Set<FamilyMember> givers = filterAge(ADULT);
    Set<FamilyMember> receivers = filterAge(ADULT);
    Iterator<FamilyMember> giverator = givers.iterator();
    Iterator<FamilyMember> receiverator = receivers.iterator();

    while (giverator.hasNext() && receiverator.hasNext()) {
      ret.put(giverator.next(), receiverator.next());
    }

    return ret;
  }
}

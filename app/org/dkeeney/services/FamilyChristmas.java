package org.dkeeney.services;

import org.dkeeney.dao.FamilyDao;
import org.dkeeney.models.AgeGroup;
import org.dkeeney.models.FamilyMember;
import org.dkeeney.models.Gender;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static org.dkeeney.models.AgeGroup.ADULT;
import static org.dkeeney.models.AgeGroup.CHILD;
import static org.dkeeney.models.Gender.*;

public class FamilyChristmas {
  private final List<FamilyMember> familyMembers;
  private final Random random;

  @Inject
  public FamilyChristmas(FamilyDao familyDao, Random random) throws IOException {
    familyMembers = familyDao.loadFamily();
    this.random = random;
  }

  public List<FamilyMember> filterAge(AgeGroup ageGroup) {
    return filterAge(ageGroup, familyMembers);
  }

  public List<FamilyMember> filterGender(Gender gender) {
    return filterGender(gender, familyMembers);
  }

  public Map<FamilyMember, FamilyMember> assignAdults() {
    Map<FamilyMember, FamilyMember> ret = new HashMap<>();
    List<FamilyMember> givers = filterAge(ADULT);
    List<FamilyMember> receivers = filterAge(ADULT);
    Collections.shuffle(givers, random);
    Collections.shuffle(receivers, random);

    ret = assignAdultsRecursive(ret, givers, receivers);

    return ret;
  }

  public Map<FamilyMember, FamilyMember> assignChildren() {
    Map<FamilyMember, FamilyMember> ret = new HashMap<>();
    List<FamilyMember> givers = filterAge(ADULT);
    List<FamilyMember> receivers = filterAge(CHILD);
    Collections.shuffle(givers, random);
    Collections.shuffle(receivers, random);

    ret = assignChildrenRecursive(ret, givers, receivers);
    return ret;
  }

  private List<FamilyMember> filterAge(AgeGroup ageGroup, List<FamilyMember> initialSet) {
    return initialSet.stream()
        .filter(member -> ageGroup.equals(member.getAgeGroup()))
        .collect(Collectors.toList());
  }

  private List<FamilyMember> filterGender(Gender gender, List<FamilyMember> initialSet) {
    return initialSet.stream()
        .filter(member -> gender.equals(member.getGender()))
        .collect(Collectors.toList());
  }

  private Map<FamilyMember, FamilyMember> assignAdultsRecursive(
      Map<FamilyMember, FamilyMember> results, List<FamilyMember> givers, List<FamilyMember> receivers) {
    if (givers.isEmpty()) {
      return results;
    }
    FamilyMember giver = givers.get(0);
    for (int i = 0; i < receivers.size(); i++) {
      FamilyMember receiver = receivers.get(i);
      if (validAdultGiftingPair(giver, receiver)) {
        Map<FamilyMember, FamilyMember> newResults = new HashMap<>(results);
        newResults.put(giver, receiver);
        List<FamilyMember> newGivers = removeMember(givers, giver);
        List<FamilyMember> newReceivers = removeMember(receivers, receiver);
        Map<FamilyMember, FamilyMember> nextAttempt = assignAdultsRecursive(newResults, newGivers, newReceivers);
        if (nextAttempt != null) {
          return nextAttempt;
        }
      }
    }
    return null;
  }

  private boolean validAdultGiftingPair(FamilyMember giver, FamilyMember receiver) {
    if (giver.getShortName().equals(receiver.getShortName())) {
      // no self giving!
      return false;
    }
    if (giver.getSpouse().equals(receiver.getShortName())) {
      // no giving to your spouse!
      return false;
    }
    if (giver.getShortName().equals("Acha") && receiver.getGender() == FEMALE) {
      // let's make it easier for him
      return false;
    }
    if (giver.getParents().contains(receiver.getShortName())) {
      // giving to your parents is too easy!
      return false;
    }

    return true;
  }

  private List<FamilyMember> removeMember(List<FamilyMember> familyMembers, FamilyMember toRemove) {
    return familyMembers.stream()
        .filter(member -> !member.getShortName().equals(toRemove.getShortName()))
        .collect(Collectors.toList());
  }

  private Map<FamilyMember, FamilyMember> assignChildrenRecursive(
      Map<FamilyMember, FamilyMember> results, List<FamilyMember> givers, List<FamilyMember> receivers) {
    if (receivers.isEmpty()) {
      return results;
    }
    FamilyMember receiver = receivers.get(0);
    for (int i = 0; i < givers.size(); i++) {
      FamilyMember giver = givers.get(i);
      if (validChildGiftingPair(giver, receiver)) {
        Map<FamilyMember, FamilyMember> newResults = new HashMap<>(results);
        newResults.put(giver, receiver);
        List<FamilyMember> newGivers = removeMember(givers, giver);
        List<FamilyMember> newReceivers = removeMember(receivers, receiver);
        Map<FamilyMember, FamilyMember> nextAttempt = assignChildrenRecursive(newResults, newGivers, newReceivers);
        if (nextAttempt != null) {
          return nextAttempt;
        }
      }
    }
    return null;
  }

  private boolean validChildGiftingPair(FamilyMember giver, FamilyMember receiver) {
    if (receiver.getParents().contains(giver.getShortName())) {
      // no parents giving to their own kids!
      return false;
    }
    return true;
  }
}

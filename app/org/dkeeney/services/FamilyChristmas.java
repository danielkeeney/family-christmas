package org.dkeeney.services;

import org.apache.commons.lang3.StringUtils;
import org.dkeeney.dao.FamilyDao;
import org.dkeeney.models.AgeGroup;
import org.dkeeney.models.FamilyMember;
import org.dkeeney.models.Gender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Configuration;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.dkeeney.models.AgeGroup.ADULT;
import static org.dkeeney.models.AgeGroup.CHILD;
import static org.dkeeney.models.Gender.FEMALE;

public class FamilyChristmas {
  private static final Logger LOGGER = LoggerFactory.getLogger(FamilyChristmas.class);
  private final List<FamilyMember> familyMembers;
  private final int entropy;

  @Inject
  public FamilyChristmas(FamilyDao familyDao, Configuration config) throws IOException {
    familyMembers = familyDao.getFamily();
    entropy = config.getInt("christmas.entropy", 10);
  }

  public List<FamilyMember> filterAge(AgeGroup ageGroup) {
    return filterAge(ageGroup, familyMembers);
  }

  public List<FamilyMember> filterGender(Gender gender) {
    return filterGender(gender, familyMembers);
  }

  public Map<List<FamilyMember>, FamilyMember> assignAdults(Random random) {
    Map<List<FamilyMember>, FamilyMember> ret = new HashMap<>();
    List<List<FamilyMember>> givers = filterAge(ADULT)
        .stream()
        .map(Collections::singletonList)
        .collect(Collectors.toList());
    List<FamilyMember> receivers = filterAge(ADULT);
    for (int i = 0; i < entropy; i++) {
      Collections.shuffle(givers, random);
      Collections.shuffle(receivers, random);
    }

    ret = assignRecursive(ret, givers, receivers, this::validAdultGiftingPair);

    return ret;
  }

  public Map<List<FamilyMember>, FamilyMember> assignChildren(Random random) {
    Map<List<FamilyMember>, FamilyMember> ret = new HashMap<>();
    List<List<FamilyMember>> givers = splitByMarriage(filterAge(ADULT));
    List<FamilyMember> receivers = filterAge(CHILD);
    for (int i = 0; i < entropy; i++) {
      Collections.shuffle(givers, random);
      Collections.shuffle(receivers, random);
    }

    ret = assignRecursive(ret, givers, receivers, this::validChildGiftingPair);
    return ret;
  }

  private List<List<FamilyMember>> splitByMarriage(List<FamilyMember> family) {
    List<List<FamilyMember>> ret = new ArrayList<>();
    while(!family.isEmpty()) {
      List<FamilyMember> group = new ArrayList<>();
      FamilyMember currentFamily = family.get(0);
      group.add(currentFamily);
      family.remove(0);
      if (StringUtils.isNotBlank(currentFamily.getSpouse())) {
        int spouseIndex = findIndexOf(family, currentFamily.getSpouse());
        group.add(family.get(spouseIndex));
        family.remove(spouseIndex);
      }
      ret.add(group);
    }
    return ret;
  }

  private int findIndexOf(List<FamilyMember> family, String name) {
    for (int i = 0; i < family.size(); i++) {
      if (family.get(i).getShortName().equals(name)) {
        return i;
      }
    }
    LOGGER.warn("Unable to find spouse {} in family {}", name, family.stream().map(FamilyMember::getShortName).collect(Collectors.toSet()));
    return -1;
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

  private Map<List<FamilyMember>, FamilyMember> assignRecursive(
      Map<List<FamilyMember>, FamilyMember> results, List<List<FamilyMember>> givers, List<FamilyMember> receivers,
      BiFunction<List<FamilyMember>, FamilyMember, Boolean> validPair) {
    if (receivers.isEmpty()) {
      return results;
    }
    FamilyMember receiver = receivers.get(0);
    for (int i = 0; i < givers.size(); i++) {
      List<FamilyMember> giver = givers.get(i);
      if (validPair.apply(giver, receiver)) {
        Map<List<FamilyMember>, FamilyMember> newResults = new HashMap<>(results);
        newResults.put(giver, receiver);
        List<List<FamilyMember>> newGivers = removeMember(givers, giver);
        List<FamilyMember> newReceivers = removeMember(receivers, receiver);
        Map<List<FamilyMember>, FamilyMember> nextAttempt = assignRecursive(newResults, newGivers, newReceivers, validPair);
        if (nextAttempt != null) {
          return nextAttempt;
        }
      }
    }
    return null;
  }

  private boolean validAdultGiftingPair(List<FamilyMember> givers, FamilyMember receiver) {
    for (FamilyMember giver : givers) {
      if (giver.getShortName().equals(receiver.getShortName())) {
        LOGGER.debug("Preventing {} from giving to themselves", giver.getShortName());
        // no self giving!
        return false;
      }
      if (giver.getSpouse().equals(receiver.getShortName())) {
        LOGGER.debug("Preventing {} from giving to their spouse {}", giver.getShortName(), receiver.getShortName());
        // no giving to your spouse!
        return false;
      }
      if (giver.getShortName().equals("Acha") && receiver.getGender() == FEMALE) {
        LOGGER.debug("Preventing Acha from giving to {}", receiver.getShortName());
        // let's make it easier for him
        return false;
      }
      if (giver.getParents().contains(receiver.getShortName())) {
        LOGGER.debug("Preventing {} from giving to their parent {}", giver.getShortName(), receiver.getShortName());
        // giving to your parents is too easy!
        return false;
      }
    }
    return validChildGiftingPair(givers, receiver);
  }

  private boolean validChildGiftingPair(List<FamilyMember> givers, FamilyMember receiver) {
    for (FamilyMember giver : givers) {
      if (receiver.getParents().contains(giver.getShortName())) {
        LOGGER.debug("Preventing {} from giving to their child {}", giver.getShortName(), receiver.getShortName());
        // no parents giving to their own kids!
        return false;
      }
    }
    return true;
  }

  private List<FamilyMember> removeMember(List<FamilyMember> familyMembers, FamilyMember toRemove) {
    List<List<FamilyMember>> lists = removeMember(
        familyMembers.stream().map(Collections::singletonList).collect(Collectors.toList()),
        Collections.singletonList(toRemove)
    );
    if (lists.isEmpty()) {
      return new ArrayList<>();
    } else {
      return lists.stream().map(list -> list.get(0)).collect(Collectors.toList());
    }
  }

  private List<List<FamilyMember>> removeMember(List<List<FamilyMember>> familyMembers, List<FamilyMember> toRemove) {
    return familyMembers.stream()
        .filter(member -> {
          List<FamilyMember> overlap = new ArrayList<>(member);
          overlap.retainAll(toRemove);
          return overlap.isEmpty();
        })
        .collect(Collectors.toList());
  }
}

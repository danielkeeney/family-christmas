package org.dkeeney.services;

import org.apache.commons.lang3.StringUtils;
import org.dkeeney.dao.FamilyDao;
import org.dkeeney.models.FamilyMember;
import org.junit.Before;
import org.junit.Test;
import play.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.dkeeney.models.AgeGroup.ADULT;
import static org.dkeeney.models.AgeGroup.CHILD;
import static org.dkeeney.models.Gender.FEMALE;
import static org.dkeeney.models.Gender.MALE;
import static org.junit.Assert.*;

public class FamilyChristmasTest {
  private FamilyChristmas familyChristmas;
  private Map<List<FamilyMember>, FamilyMember> adultExchange;
  private Random random = new Random();

  @Before
  public void before() throws IOException {
    FamilyDao familyDao = new FamilyDao();
    familyChristmas = new FamilyChristmas(
        familyDao,
        new Configuration(new HashMap<>()));
    adultExchange = familyChristmas.assignAdults(random);
  }

  private void repeatTest(int repeats, Supplier<Void> test) {
    for (int i = 0; i < repeats; i++) {
      test.get();
    }
  }

  private List<String> groupToProperty(List<FamilyMember> group, Function<FamilyMember, String> mapper) {
    return group.stream().map(mapper).collect(Collectors.toList());
  }

  private String groupToString(List<FamilyMember> group) {
    return StringUtils.join(
        group.stream()
            .map(FamilyMember::getShortName)
            .collect(Collectors.toList()),
        ", ");
  }

  private void assertAdultExchange(Map<List<FamilyMember>, FamilyMember> adultExchange) {
    assertNotNull("Adult exchanges should be defined", adultExchange);
    assertEquals("Not all adults are giving gifts", 18, adultExchange.size());
    adultExchange.forEach((givers, receiver) -> {
      givers.forEach(giver ->
          assertEquals("Only adults should be giving in the adult exchange", ADULT, giver.getAgeGroup())
      );
      assertEquals("Only adults should be receiving in the adult exchange", ADULT, receiver.getAgeGroup());
    });
  }

  @Test
  public void testAgeGroupMap() {
    assertEquals("Wrong number of adults", 18, familyChristmas.filterAge(ADULT).size());
    assertEquals("Wrong number of children", 9, familyChristmas.filterAge(CHILD).size());
  }

  @Test
  public void testGenderMap() {
    assertEquals("Wrong number of men", 16, familyChristmas.filterGender(MALE).size());
    assertEquals("Wrong number of women", 11, familyChristmas.filterGender(FEMALE).size());
  }

  @Test
  public void testAssignAdultsHasNoSelfGiving() {
    repeatTest(100, () -> {
      adultExchange = familyChristmas.assignAdults(random);

      adultExchange.forEach((giver, receiver) ->
          assertNotEquals("Should not be giving gifts to yourself",
              giver,
              receiver));
      assertAdultExchange(adultExchange);
      return null;
    });
  }

  @Test
  public void testAssignAdultsHasNoSpouseGiving() {
    repeatTest(100, () -> {
      adultExchange = familyChristmas.assignAdults(random);

      adultExchange.forEach((giver, receiver) ->
          assertFalse("Should not be giving to your spouse: " + groupToString(giver),
              groupToProperty(giver, FamilyMember::getSpouse).contains(receiver.getShortName())));
      assertAdultExchange(adultExchange);
      return null;
    });
  }

  @Test
  public void testAssignAdultsGivesAchaMaleRecipient() {
    repeatTest(100, () -> {
      adultExchange = familyChristmas.assignAdults(random);

      adultExchange.entrySet().stream()
          .filter(entry -> entry.getKey().contains(new FamilyMember("Acha")))
          .forEach(entry -> assertEquals("Subramaniam should be gifting men",
              MALE,
              entry.getValue().getGender()));
      assertAdultExchange(adultExchange);
      return null;
    });
  }

  @Test
  public void testAssignAdultsHasNoParentalGiving() {
    repeatTest(100, () -> {
      adultExchange = familyChristmas.assignAdults(random);

      adultExchange.forEach((giver, receiver) -> {
        List<String> giversParents = giver.stream()
            .flatMap(person -> person.getParents().stream())
            .collect(Collectors.toList());
        assertFalse(groupToString(giver) + " is giving to their parent " + receiver.getShortName(),
            giversParents.contains(receiver.getShortName()));
      });
      assertAdultExchange(adultExchange);
      return null;
    });
  }

  @Test
  public void testAssignAdultsHasNoChildGiving() {
    repeatTest(100, () -> {
      adultExchange = familyChristmas.assignAdults(random);

      adultExchange.forEach((giver, receiver) -> {
        List<String> newParents = new ArrayList<>(receiver.getParents());
        newParents.retainAll(groupToProperty(giver, FamilyMember::getShortName));
        assertTrue(receiver.getShortName() + " is getting a gift from their parent " + groupToString(giver),
            newParents.isEmpty());
      });
      assertAdultExchange(adultExchange);
      return null;
    });
  }

  @Test
  public void testAssignChildrenHasCorrectAgeGroups() {
    repeatTest(100, () -> {
      Map<List<FamilyMember>, FamilyMember> childExchange = familyChristmas.assignChildren(random);
      assertEquals("Not all the children are receiving gifts", 9, childExchange.size());
      childExchange.forEach((givers, receiver) -> {
        givers.forEach(giver ->
            assertEquals("The givers for child exchange should be adults", ADULT, giver.getAgeGroup()));
        assertEquals("The receivers for child exchange should be children", CHILD, receiver.getAgeGroup());
        List<String> newParents = new ArrayList<>(receiver.getParents());
        newParents.retainAll(groupToProperty(givers, FamilyMember::getShortName));
        assertTrue(receiver.getShortName() + " is getting a gift from their parent " + groupToString(givers),
            newParents.isEmpty());
      });
      return null;
    });
  }
}
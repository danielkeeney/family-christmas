package org.dkeeney.services;

import org.dkeeney.dao.FamilyDao;
import org.dkeeney.injector.RandomModule;
import org.dkeeney.models.FamilyMember;
import org.junit.Before;
import org.junit.Test;
import play.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.dkeeney.models.AgeGroup.ADULT;
import static org.dkeeney.models.AgeGroup.CHILD;
import static org.dkeeney.models.Gender.FEMALE;
import static org.dkeeney.models.Gender.MALE;
import static org.junit.Assert.*;

public class FamilyChristmasTest {
  private FamilyChristmas familyChristmas;
  private Map<FamilyMember, FamilyMember> adultExchange;

  @Before
  public void before() throws IOException {
    FamilyDao familyDao = new FamilyDao();
    familyChristmas = new FamilyChristmas(
        familyDao,
        new RandomModule().getConfiguredRandom(),
        new Configuration(new HashMap<>()));
    adultExchange = familyChristmas.assignAdults();
  }

  private void repeatTest(int repeats, Supplier<Void> test) {
    for (int i = 0; i < repeats; i++) {
      test.get();
    }
  }

  private void assertAdultExchange(Map<FamilyMember, FamilyMember> adultExchange) {
    assertNotNull("Adult exchanges should be defined", adultExchange);
    assertEquals("Not all adults are giving gifts", 18, adultExchange.size());
    adultExchange.forEach((giver, receiver) -> {
      assertEquals("Only adults should be giving in the adult exchange", ADULT, giver.getAgeGroup());
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
      adultExchange = familyChristmas.assignAdults();

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
      adultExchange = familyChristmas.assignAdults();

      adultExchange.forEach((giver, receiver) ->
          assertNotEquals("Should not be giving to your spouse",
              giver.getSpouse(),
              receiver.getShortName()));
      assertAdultExchange(adultExchange);
      return null;
    });
  }

  @Test
  public void testAssignAdultsGivesAchaMaleRecipient() {
    repeatTest(100, () -> {
      adultExchange = familyChristmas.assignAdults();

      adultExchange.entrySet().stream()
          .filter(entry -> entry.getKey().getShortName().equals("Acha"))
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
      adultExchange = familyChristmas.assignAdults();

      adultExchange.forEach((giver, receiver) ->
          assertFalse(giver.getShortName() + " is giving to their parent " + receiver.getShortName(),
              giver.getParents().contains(receiver.getShortName())));
      assertAdultExchange(adultExchange);
      return null;
    });
  }

  @Test
  public void testAssignAdultsHasNoChildGiving() {
    repeatTest(100, () -> {
      adultExchange = familyChristmas.assignAdults();

      adultExchange.forEach((giver, receiver) ->
          assertFalse(receiver.getShortName() + " is getting a gift from their parent " + giver.getShortName(),
              receiver.getParents().contains(giver.getShortName())));
      assertAdultExchange(adultExchange);
      return null;
    });
  }

  @Test
  public void testAssignChildrenHasCorrectAgeGroups() {
    repeatTest(100, () -> {
      Map<FamilyMember, FamilyMember> childExchange = familyChristmas.assignChildren();
      assertEquals("Not all the children are receiving gifts", 9, childExchange.size());
      childExchange.forEach((giver, receiver) -> {
        assertEquals("The givers for child exchange should be adults", ADULT, giver.getAgeGroup());
        assertEquals("The receivers for child exchange should be children", CHILD, receiver.getAgeGroup());
        assertFalse(receiver.getShortName() + " is getting a gift from their parent " + giver.getShortName(),
            receiver.getParents().contains(giver.getShortName()));
      });
      return null;
    });
  }
}
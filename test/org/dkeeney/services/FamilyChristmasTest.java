package org.dkeeney.services;

import org.dkeeney.dao.FamilyDao;
import org.dkeeney.models.FamilyMember;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.dkeeney.models.AgeGroup.ADULT;
import static org.dkeeney.models.AgeGroup.CHILD;
import static org.dkeeney.models.Gender.FEMALE;
import static org.dkeeney.models.Gender.MALE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class FamilyChristmasTest {
  private FamilyChristmas familyChristmas;
  private Map<FamilyMember, FamilyMember> adultExchange;

  @Before
  public void before() throws IOException {
    FamilyDao familyDao = new FamilyDao();
    familyChristmas = new FamilyChristmas(familyDao);
    adultExchange = familyChristmas.assignAdults();
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
  public void testAssignAdultsIncludesEveryone() {
    assertNotNull("Adult exchanges should be defined", adultExchange);
    assertEquals("Not all adults are giving gifts", 18, adultExchange.size());
  }

  @Ignore
  @Test
  public void testAssignAdultsHasNoSelfGiving() {
    adultExchange.entrySet().forEach(entry ->
        assertNotEquals("Should not be giving gifts to yourself",
            entry.getKey(),
            entry.getValue()));
  }
}
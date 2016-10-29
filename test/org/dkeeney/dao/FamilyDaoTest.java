package org.dkeeney.dao;

import org.apache.commons.lang3.StringUtils;
import org.dkeeney.models.FamilyMember;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.dkeeney.models.AgeGroup.ADULT;
import static org.dkeeney.models.AgeGroup.CHILD;
import static org.junit.Assert.*;

public class FamilyDaoTest {
  private FamilyDao familyDao;
  private List<FamilyMember> family;

  @Before
  public void before() throws IOException {
    familyDao = new FamilyDao();
    family = familyDao.loadFamily();
  }

  private Set<String> getShortNames() {
    return family.stream()
        .map(FamilyMember::getShortName)
        .collect(Collectors.toSet());
  }

  @Test
  public void testPeopleSetExists() {
    assertNotNull("Family should be defined", family);
    assertFalse("Family members should exist", family.isEmpty());
    assertEquals("Wrong family size", 27, family.size());
  }

  @Test
  public void testPeopleConfiguration() {
    family.forEach(member -> {
      assertTrue("First name missing for " + member.getShortName(), StringUtils.isNotBlank(member.getFirstName()));
      assertTrue("Last name missing for " + member.getShortName(), StringUtils.isNotBlank(member.getLastName()));
      assertTrue("Short name missing for " + member.getFirstName(), StringUtils.isNotBlank(member.getShortName()));
      assertNotEquals("Should not be married to yourself", member.getShortName(), member.getSpouse());
      assertNotNull("Missing age group for " + member.getShortName(), member.getAgeGroup());
      assertNotNull("Missing gender for " + member.getShortName(), member.getGender());
    });
  }

  @Test
  public void testShortNameDuplicates() {
    Set<String> shortNames = getShortNames();
    assertEquals("Duplicate short names detected", family.size(), shortNames.size());
  }

  @Test
  public void testSpouseConfiguration() {
    Set<String> shortNames = getShortNames();
    family.stream()
        .filter(member -> StringUtils.isNotBlank(member.getSpouse()))
        .forEach(member -> assertTrue("Spouse for " + member.getShortName() + " is not valid: " + member.getSpouse(),
            shortNames.contains(member.getSpouse())));
  }

  @Test
  public void testParentConfigurationForChildren() {
    Set<String> shortNames = getShortNames();
    family.stream()
        .filter(member -> CHILD == member.getAgeGroup())
        .forEach(member -> {
          assertNotNull(member.getShortName() + " has misconfigured parents", member.getParents());
          assertEquals(member.getShortName() + " should have two parents", 2, member.getParents().size());
          assertTrue(member.getShortName() + " has parents with mismatched short nameS",
              shortNames.containsAll(member.getParents()));
        });
  }

  @Test
  public void testParentConfigurationForAdults() {
    Set<String> shortNames = getShortNames();
    List<String> adultsWithNoParents = Arrays.asList("Acha", "Vijaya", "Shekaran", "Kana", "Sakina", "Shanti",
        "Ravi", "Valli", "Rathi", "Daniel");
    family.stream()
        .filter(member -> ADULT == member.getAgeGroup())
        .filter(member -> !adultsWithNoParents.contains(member.getShortName()))
        .forEach(member -> {
          assertNotNull(member.getShortName() + " has misconfigured parents", member.getParents());
          assertFalse(member.getShortName() + " should have at least one parent", member.getParents().isEmpty());
          assertTrue(member.getShortName() + " has parents with mismatched short nameS",
              shortNames.containsAll(member.getParents()));
        });
  }
}
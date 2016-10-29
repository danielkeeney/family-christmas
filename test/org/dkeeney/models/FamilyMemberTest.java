package org.dkeeney.models;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class FamilyMemberTest {
  private Random random = new Random();
  private String firstName;
  private String lastName;
  private String shortName;
  private String spouse;
  private AgeGroup ageGroup;
  private Gender gender;

  @Before
  public void before() {
    firstName = RandomStringUtils.randomAlphabetic(10);
    lastName = RandomStringUtils.randomAlphabetic(10);
    shortName = RandomStringUtils.randomAlphabetic(10);
    spouse = RandomStringUtils.randomAlphabetic(10);
    ageGroup = random.nextBoolean() ? AgeGroup.ADULT : AgeGroup.CHILD;
    gender = random.nextBoolean() ? Gender.FEMALE : Gender.MALE;
  }

  private void assertFamilyMember(FamilyMember familyMember) {
    assertEquals("Wrong age group", ageGroup, familyMember.getAgeGroup());
    assertEquals("Wrong first name", firstName, familyMember.getFirstName());
    assertEquals("Wrong last name", lastName, familyMember.getLastName());
    assertEquals("Wrong short name", shortName, familyMember.getShortName());
    assertEquals("Wrong spouse", spouse, familyMember.getSpouse());
    assertEquals("Wrong gender", gender, familyMember.getGender());
  }

  @Test
  public void testSettersAndGetters() {
    FamilyMember familyMember = new FamilyMember();
    familyMember.setAgeGroup(ageGroup);
    familyMember.setFirstName(firstName);
    familyMember.setLastName(lastName);
    familyMember.setShortName(shortName);
    familyMember.setSpouse(spouse);
    familyMember.setGender(gender);

    assertFamilyMember(familyMember);
  }

  @Test
  public void testConstructor() {
    FamilyMember familyMember = new FamilyMember(firstName, lastName, shortName, spouse, ageGroup, gender);

    assertFamilyMember(familyMember);
  }

  @Test
  public void testEquals() {
    FamilyMember familyMember1 = new FamilyMember(firstName, lastName, shortName, spouse, ageGroup, gender);
    FamilyMember familyMember2 = new FamilyMember(shortName);

    assertEquals("Equals function should only use short name", familyMember1, familyMember2);
    assertEquals("Hash function should only use short name", familyMember1.hashCode(), familyMember2.hashCode());
  }

  @Test
  public void testMapKeyUse() {
    FamilyMember familyMember1 = new FamilyMember(firstName, lastName, shortName, spouse, ageGroup, gender);
    FamilyMember familyMember2 = new FamilyMember(shortName);

    Map<FamilyMember, String> map = new HashMap<>();
    String testMessage = "Hello, world";
    map.put(familyMember1, testMessage);
    assertEquals("FamilyMember should be a valid key in a map", map.get(familyMember1), testMessage);
    assertEquals("FamilyMember with only a short name should be usable as a key",
        map.get(familyMember1), map.get(familyMember2));
  }
}
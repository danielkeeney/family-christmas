package org.dkeeney.models;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class FamilyMember {
  private String firstName = "";
  private String lastName = "";
  private String shortName = "";
  private String spouse = "";
  private AgeGroup ageGroup;
  private Gender gender;
  private List<String> parents = new ArrayList<>();

  public FamilyMember() {
  }

  public FamilyMember(String shortName) {
    this.shortName = shortName;
  }

  public FamilyMember(String firstName, String lastName, String shortName, String spouse, AgeGroup ageGroup,
                      Gender gender, List<String> parents) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.shortName = shortName;
    this.spouse = spouse;
    this.ageGroup = ageGroup;
    this.gender = gender;
    this.parents = parents;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  public String getSpouse() {
    return spouse;
  }

  public void setSpouse(String spouse) {
    this.spouse = spouse;
  }

  public AgeGroup getAgeGroup() {
    return ageGroup;
  }

  public void setAgeGroup(AgeGroup ageGroup) {
    this.ageGroup = ageGroup;
  }

  public Gender getGender() {
    return gender;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }

  public List<String> getParents() {
    return parents;
  }

  public void setParents(List<String> parents) {
    this.parents = parents;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FamilyMember that = (FamilyMember) o;

    return shortName.equals(that.shortName);

  }

  @Override
  public int hashCode() {
    return shortName.hashCode();
  }

  @Override
  public String toString() {
    return "{" +
        "\"firstName\":\"" + firstName + '\"' +
        ", \"lastName\":\"" + lastName + '\"' +
        ", \"shortName\":\"" + shortName + '\"' +
        ", \"spouse\":\"" + spouse + '\"' +
        ", \"ageGroup\":\"" + ageGroup + '\"' +
        ", \"gender\":\"" + gender + '\"' +
        ", \"parents\":\"[\"" + StringUtils.join(parents, "\",\"") + "\"" +
        "}";
  }
}

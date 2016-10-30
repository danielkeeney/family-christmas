package org.dkeeney.models.wrapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.dkeeney.models.FamilyMember;

import java.util.List;

public class FamilyWrapper {
  @JsonProperty("family")
  private List<FamilyMember> family;

  public List<FamilyMember> getFamily() {
    return family;
  }

  public void setFamily(List<FamilyMember> family) {
    this.family = family;
  }
}

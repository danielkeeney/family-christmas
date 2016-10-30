package org.dkeeney.services;

import org.dkeeney.models.FamilyMember;
import org.joda.time.DateTime;

import javax.inject.Inject;
import java.util.Map;
import java.util.Random;

public class PersistenceService {
  private FamilyChristmas familyChristmas;

  @Inject
  public PersistenceService(FamilyChristmas familyChristmas) {
    this.familyChristmas = familyChristmas;
  }

  public Map<FamilyMember, FamilyMember> getAdultExchange() {
    return getAdultExchange(DateTime.now().getYear());
  }

  public Map<FamilyMember, FamilyMember> getAdultExchange(int seed) {
    return familyChristmas.assignAdults(new Random(seed));
  }

  public Map<FamilyMember, FamilyMember> getChildrenExchange() {
    return getChildrenExchange(DateTime.now().getYear());
  }

  public Map<FamilyMember, FamilyMember> getChildrenExchange(int seed) {
    return familyChristmas.assignChildren(new Random(seed));
  }
}

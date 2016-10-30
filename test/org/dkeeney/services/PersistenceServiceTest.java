package org.dkeeney.services;

import org.dkeeney.dao.FamilyDao;
import org.dkeeney.models.FamilyMember;
import org.junit.Before;
import org.junit.Test;
import play.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class PersistenceServiceTest {
  private PersistenceService persistenceService;

  @Before
  public void before() throws IOException {
    persistenceService = new PersistenceService(new FamilyChristmas(new FamilyDao(), new Configuration(new HashMap<>())));
  }

  @Test
  public void testMultipleAdultGetsAreEqual() {
    Map<List<FamilyMember>, FamilyMember> firstCall = persistenceService.getAdultExchange();
    Map<List<FamilyMember>, FamilyMember> secondCall = persistenceService.getAdultExchange();
    Map<List<FamilyMember>, FamilyMember> thirdCall = persistenceService.getAdultExchange();
    Map<List<FamilyMember>, FamilyMember> fourthCall = persistenceService.getAdultExchange();

    assertEquals("First and second call should be the same", firstCall, secondCall);
    assertEquals("Second and third call should be the same", secondCall, thirdCall);
    assertEquals("Third and fourth call should be the same", thirdCall, fourthCall);
  }

  @Test
  public void testAdultGetsWithDifferentSeedsAreDifferent() {
    Map<List<FamilyMember>, FamilyMember> seed1 = persistenceService.getAdultExchange(1);
    Map<List<FamilyMember>, FamilyMember> seed2 = persistenceService.getAdultExchange(2);

    assertNotEquals("Should generate different maps with different seeds", seed1, seed2);
  }

  @Test
  public void testMultipleChildGetsAreEqual() {
    Map<List<FamilyMember>, FamilyMember> firstCall = persistenceService.getChildrenExchange();
    Map<List<FamilyMember>, FamilyMember> secondCall = persistenceService.getChildrenExchange();
    Map<List<FamilyMember>, FamilyMember> thirdCall = persistenceService.getChildrenExchange();
    Map<List<FamilyMember>, FamilyMember> fourthCall = persistenceService.getChildrenExchange();

    assertEquals("First and second call should be the same", firstCall, secondCall);
    assertEquals("Second and third call should be the same", secondCall, thirdCall);
    assertEquals("Third and fourth call should be the same", thirdCall, fourthCall);
  }

  @Test
  public void testChildGetsWithDifferentSeedsAreDifferent() {
    Map<List<FamilyMember>, FamilyMember> seed1 = persistenceService.getChildrenExchange(1);
    Map<List<FamilyMember>, FamilyMember> seed2 = persistenceService.getChildrenExchange(2);

    assertNotEquals("Should generate different maps with different seeds", seed1, seed2);
  }
}
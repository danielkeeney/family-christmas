package org.dkeeney.services;

import com.typesafe.config.ConfigFactory;
import org.junit.Before;
import org.junit.Test;
import play.Configuration;

import static org.junit.Assert.*;

public class AccessControlServiceTest {
  private AccessControlService accessControlService;

  @Before
  public void before() {
    accessControlService = new AccessControlService(
        new Configuration(ConfigFactory.load("application.conf"))
    );
  }

  @Test
  public void testDanielIsSuperUser() {
    assertTrue("Daniel should be a super user", accessControlService.isSuperUser("daniel"));
  }

  @Test
  public void testAshaIsSuperUser() {
    assertTrue("Asha should be a super user", accessControlService.isSuperUser("asha"));
  }

  @Test
  public void testSuperUserComparisonIgnoresCase() {
    String[] inputs = {"aSHa", "ASHA ", "Daniel", "   DaNiEl"};
    for (String input : inputs) {
      assertTrue(input + " should be a super user", accessControlService.isSuperUser(input));
    }
  }
}
package controllers;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http.Cookie;
import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.WithApplication;

import java.util.Optional;

import static org.junit.Assert.*;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;

public class XmasControllerTest extends WithApplication {
  @Before
  public void before() {
    route(fakeRequest()
        .method("POST")
        .path("/register")
        .bodyForm(new ImmutableMap.Builder<String, String>()
            .put("user", "daniel")
            .put("password", "asdf")
            .put("repeat", "asdf")
            .build())
    );
  }

  private Result successfulLoginAttempt() {
    RequestBuilder requestBuilder = fakeRequest()
        .method("POST").path("/login").bodyForm(new ImmutableMap.Builder<String, String>()
            .put("user", "daniel")
            .put("password", "asdf")
            .build());
    return route(requestBuilder);
  }

  @Test
  public void testLoginFlow() {
    Result response = successfulLoginAttempt();
    assertEquals("Successful login should redirect", 303, response.status());
    Optional<String> locationHeader = response.header("Location");
    Cookie cookie = response.cookie("PLAY_SESSION");
    assertTrue("Redirect header should be present", locationHeader.isPresent());
    assertEquals("Redirect should go to /view", "/view", locationHeader.get());
    assertNotNull("Play cookie should be returned", cookie);
    assertTrue("Should return a PLAY_SESSION", StringUtils.isNotBlank(cookie.value()));
  }

  @Test
  public void testWithoutAuthentication() {
    RequestBuilder requestBuilder = fakeRequest()
        .method("GET").path("/view");
    Result response = route(requestBuilder);
    assertEquals("Should reject a view request without authorization", 303, response.status());
  }

  @Test
  public void testWithAuthentication() {
    Result response = successfulLoginAttempt();
    String redirectedLocation = response.header("Location").get();
    Cookie cookie = response.cookie("PLAY_SESSION");

    RequestBuilder requestBuilder = fakeRequest()
        .method("GET").path(redirectedLocation).cookie(cookie);
    Result viewResponse = route(requestBuilder);
    assertEquals("View request with authenticated header should be successful", 200, viewResponse.status());
  }

  @Test
  public void testLogout() {
    Result response = successfulLoginAttempt();
    Cookie authenticated = response.cookie("PLAY_SESSION");

    RequestBuilder requestBuilder = fakeRequest()
        .method("GET").path("/logout").cookie(authenticated);
    Result logoutResponse = route(requestBuilder);
    Cookie newCookie = logoutResponse.cookie("PLAY_SESSION");
    assertEquals("Logout should not fail", 200, logoutResponse.status());
    assertNotEquals("Logout should return a new cookie", authenticated.value(), newCookie.value());
  }
}

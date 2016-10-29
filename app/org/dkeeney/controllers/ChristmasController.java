package org.dkeeney.controllers;

import org.dkeeney.services.FamilyChristmas;
import play.mvc.Controller;
import play.mvc.Result;

import views.html.*;

import javax.inject.Inject;

public class ChristmasController extends Controller {
  @Inject
  private FamilyChristmas christmasService;

  public Result index() {
    return ok(results.render(christmasService.assignAdults(), christmasService.assignChildren()));
  }
}

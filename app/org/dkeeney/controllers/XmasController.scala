package org.dkeeney.controllers

import javax.inject.Inject

import org.dkeeney.services.{AccessControlService, FrontEndService, LoginService, PersistenceService}
import play.api.mvc.{Action, Controller}
import views.html._

import scala.util.Random

class XmasController @Inject()(familyChristmas: PersistenceService,
                               loginService: LoginService,
                               accessControlService: AccessControlService,
                               frontEndService: FrontEndService)
  extends Controller {
  def loginView = Action { request =>
    Ok(login.render(""))
  }

  def loginAction = Action { request =>
    request.body.asFormUrlEncoded match {
      case Some(formData) =>
        val user = loginService.login(
          extractJavaStringFromMap(formData, "user"),
          extractJavaStringFromMap(formData, "password"))
        if (user.isPresent) {
          Redirect("/view").withSession("user" -> user.get)
        } else {
          BadRequest(login.render("Unable to log in, sorry!"))
        }

      case _ => BadRequest(login.render("Unable to log in, sorry!"))
    }
  }

  def registerView = Action { request =>
    Ok(register.render(""))
  }

  def registerAction = Action { request =>
    request.body.asFormUrlEncoded match {
      case Some(formData) =>
        val user = loginService.register(
          extractJavaStringFromMap(formData, "user"),
          extractJavaStringFromMap(formData, "password"),
          extractJavaStringFromMap(formData, "repeat")
        )
        if (user.isPresent) {
          Ok(login.render(s"Created user ${user.get}, please log in."))
        } else {
          BadRequest(register.render("failed, sorry"))
        }
      case _ => BadRequest(register.render("Failed to register, sorry"))
    }
  }

  def view = Action { request =>
    request.session.get("user").map {
      user =>
        val adults = familyChristmas.getAdultExchange
        val children = familyChristmas.getChildrenExchange
        val isSuperUser = accessControlService.isSuperUser(user)
        if (isSuperUser) {
          Ok(results.render(
            frontEndService.prepareForDisplay(adults),
            frontEndService.prepareForDisplay(children),
            isSuperUser
          ))
        } else {
          Ok(results.render(
            frontEndService.prepareForDisplay(frontEndService.filterResults(adults, user)),
            frontEndService.prepareForDisplay(frontEndService.filterResults(children, user)),
            isSuperUser
          ))
        }
    }.getOrElse {
      Redirect("/").withNewSession
    }
  }

  def random = Action { request =>
    request.session.get("user").map {
      user =>
        val isSuperUser = accessControlService.isSuperUser(user)
        if (isSuperUser) {
          val random = new Random
          Ok(results.render(
            frontEndService.prepareForDisplay(familyChristmas.getAdultExchange(random.nextInt)),
            frontEndService.prepareForDisplay(familyChristmas.getChildrenExchange(random.nextInt)),
            isSuperUser
          ))
        } else {
          Redirect("/view")
        }
    }.getOrElse{
      Redirect("/view")
    }
  }

  def logout = Action { request =>
    Ok(login.render("")).withNewSession
  }

  private def extractJavaStringFromMap(data: Map[String, Seq[String]], key: String): String = {
    val default: String = ""
    data.get(key) match {
      case Some(sequence) => sequence.headOption match {
        case Some(value) => value
        case _ => default
      }
      case _ => default
    }
  }
}

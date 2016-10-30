package org.dkeeney.controllers

import javax.inject.Inject

import org.dkeeney.models.FamilyMember
import org.dkeeney.services.{AccessControlService, LoginService, PersistenceService}
import play.api.mvc.{Action, Controller}
import views.html._

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Random

class XmasController @Inject()(familyChristmas: PersistenceService,
                               loginService: LoginService,
                               accessControlService: AccessControlService)
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
        val adults: mutable.Map[FamilyMember, FamilyMember] = familyChristmas.getAdultExchange.asScala
        val children: mutable.Map[FamilyMember, FamilyMember] = familyChristmas.getChildrenExchange.asScala
        val isSuperUser = accessControlService.isSuperUser(user)
        if (isSuperUser) {
          Ok(results.render(
            adults,
            children,
            isSuperUser
          ))
        } else {
          Ok(results.render(
            adults.filter(member => member._1.getShortName.equalsIgnoreCase(user)),
            children.filter(member => member._1.getShortName.equalsIgnoreCase(user)),
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
            familyChristmas.getAdultExchange(random.nextInt).asScala,
            familyChristmas.getChildrenExchange(random.nextInt).asScala,
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

package controllers

import javax.inject.Inject
import models._

import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Admin @Inject()(val messagesApi: MessagesApi, formEg: UserForm,
                                userRepo: UserRepository,
                                userToHobby : UserToHobbyServices,
                                hobbyServices: HobbyServices)
  extends Controller with I18nSupport {

  implicit val messages: MessagesApi = messagesApi

  def userEnableUpdate: Action[AnyContent] = Action.async{ implicit request =>
    val userEmail = request.session.get("user")
    userEmail match {
      case Some(user)=>
        userRepo.checkAdmin(user).flatMap{
          case true => userRepo.retrieveAll().map {
            userList =>
              val onlyUsers: List[UserDataModel] = userList.filter(!_.isAdmin)
              Ok(views.html.UserList(onlyUsers))
          }
          case false =>   Future.successful(Redirect(routes.Application.login()).flashing("Error"->"No Session").withNewSession)
        }

      case None => Future.successful(Redirect(routes.Application.login()).flashing("Error"->"No Session").withNewSession)
    }
  }



  def disableUser(userEmail:String) = Action.async { implicit request=>
    val adminEmail = request.session.get("user")
    adminEmail match {
      case Some(user)=>
        userRepo.checkAdmin(user).flatMap{
          case true => userRepo.makeDisable(userEmail).map{
            case true => Redirect(routes.Admin.userEnableUpdate()).flashing("Success"->"Successfully Enabled")
            case false =>  Redirect(routes.Admin.userEnableUpdate()).flashing("Error"-> s"Error while Enabling $userEmail")
          }

          case false =>   Future.successful(Redirect(routes.Application.login()).flashing("Error"->"No Session").withNewSession)
        }

      case None => Future.successful(Redirect(routes.Application.login()).flashing("Error"->"No Session").withNewSession)
    }

  }

  def enableUser(userEmail:String) = Action.async { implicit request=>
    val adminEmail = request.session.get("user")
    adminEmail match {
      case Some(user)=>
        userRepo.checkAdmin(user).flatMap{
          case true => userRepo.makeEnable(userEmail).map{
            case true => Redirect(routes.Admin.userEnableUpdate()).flashing("Success"->s"Successfully Enabled $userEmail")
            case false =>  Redirect(routes.Admin.userEnableUpdate()).flashing("Error"-> s"Error while Enabling $userEmail")
          }

          case false =>   Future.successful(Redirect(routes.Application.login()).flashing("Error"->"No Session").withNewSession)
        }

      case None => Future.successful(Redirect(routes.Application.login()).flashing("Error"->"No Session").withNewSession)
    }


  }
}

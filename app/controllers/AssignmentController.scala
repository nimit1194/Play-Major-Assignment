package controllers

import javax.inject.Inject

import models._
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AssignmentController @Inject()(val messagesApi: MessagesApi,
                                     form: UserForm,
                                     assignment:AssignmentServices,
                                     userRepo: UserRepository)
  extends Controller with I18nSupport {

  implicit val messages: MessagesApi = messagesApi

  def addAssignment(): Action[AnyContent] = Action.async { implicit request =>
    val userEmail = request.session.get("user")
    userEmail match {
      case Some(user) => userRepo.checkAdmin(user).flatMap {
        case true =>
          form.AssignmentConstraints.bindFromRequest.fold(
            formWithErrors => {
              Logger.error("Error while adding an assignemnt :" + formWithErrors)
              Future.successful(Redirect(routes.Application.showAddAssignment()).flashing("Error" -> "Fill Form Correctly"))
            },
            assignmentData => {
              assignment.store(Assignment(0,assignmentData.objective,assignmentData.instructions)).map{
                case true => Redirect(routes.Application.showAddAssignment()).flashing("Success"->"Assignment SuccessFully Added")
                case false => Redirect(routes.Application.showAddAssignment()).flashing("Error"->"Error While Storing")
              }
            })

        case false => Future.successful(Redirect(routes.Application.login()).flashing("Error" -> "No Session").withNewSession)

      }
      case None => Future.successful(Redirect(routes.Application.login()).flashing("Error" -> "No Session").withNewSession)
    }
  }

  def viewAssignmentUser: Action[AnyContent] =Action.async{ implicit request=>
    val userEmail = request.session.get("user")
    userEmail match {
      case Some(user) => userRepo.checkAdmin(user).flatMap {
        case true => Future.successful(Redirect(routes.Application.login()).flashing("Error" -> "Try to Login as User"))

        case false => assignment.returnAll.map {
          case assignmensts: List[Assignment] => Ok(views.html.ViewAssignemntUser(assignmensts))
          case Nil => Redirect(routes.UserController.showProfile()).flashing("Error" -> "No Assignment, Happy")
        }

        case false => Future.successful(Redirect(routes.Application.login()).flashing("Error" -> "No Session").withNewSession)

      }
      case None => Future.successful(Redirect(routes.Application.login()).flashing("Error" -> "No Session").withNewSession)
    }
  }


  def viewAssignmentAdmin: Action[AnyContent] =Action.async{ implicit request =>
    val userEmail = request.session.get("user")
    userEmail match {
      case Some(user) => userRepo.checkAdmin(user).flatMap {
        case true =>
          assignment.returnAll().map {
            case assignmensts: List[Assignment] => Ok(views.html.ViewAssignmentAdmin(assignmensts))
            case Nil => Redirect(routes.UserController.showProfile()).flashing("Error" -> "No Assignment, Happy")
          }

        case false => Future.successful(Redirect(routes.Application.login()).flashing("Error" -> "Try to Login as Admin").withNewSession)


        case false => Future.successful(Redirect(routes.Application.login()).flashing("Error" -> "No Session").withNewSession)

      }
      case None => Future.successful(Redirect(routes.Application.login()).flashing("Error" -> "No Session").withNewSession)
    }
  }

  def deleteAssignment(id: Int): Action[AnyContent] = Action.async{ implicit request  =>
    val userEmail = request.session.get("user")
    userEmail match {
      case Some(user) => userRepo.checkAdmin(user).flatMap {
        case true =>
          assignment.delete(id).map {
            case true => Redirect(routes.AssignmentController.viewAssignmentAdmin()).flashing("Success"->"SuccessFully Deleted")
            case false => Redirect(routes.AssignmentController.viewAssignmentAdmin()).flashing("Error" -> "Error While Deleting")
          }

        case false => Future.successful(Redirect(routes.Application.login()).flashing("Error" -> "Try to Login as Admin").withNewSession)


        case false => Future.successful(Redirect(routes.Application.login()).flashing("Error" -> "No Session").withNewSession)

      }
      case None => Future.successful(Redirect(routes.Application.login()).flashing("Error" -> "No Session").withNewSession)
    }
  }

}






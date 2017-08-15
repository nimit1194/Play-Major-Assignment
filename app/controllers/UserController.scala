package controllers

import javax.inject._
import models._
import org.mindrot.jbcrypt.BCrypt
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Controller, Request}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class UserController @Inject()(val messagesApi: MessagesApi,
                               form: UserForm, userRepo: UserRepository,
                               userToHobby : UserToHobbyServices,
                               hobby: HobbyServices)
  extends Controller with I18nSupport {

  implicit val messages: MessagesApi = messagesApi

  def showProfile: Action[AnyContent] = Action.async{ implicit request: Request[AnyContent] =>

    val user = request.session.get("user")
    user match {
      case Some(userEmail) => userRepo.retrieve(userEmail).flatMap {
        case Nil =>
          Logger.info("No user by this UserName")
          Future.successful(Redirect(routes.Application.index()))

        case userList: List[UserDataModel] =>
          val user = userList.head
          userToHobby.getUserHobby(user.id).flatMap {
            case Nil =>
              Logger.info("Did not receive any hobbies for the user!")
              Future.successful(Redirect(routes.Application.index()))
            case hobbies: List[Int] =>
              val userProfile = UserProfileData(user.firstName, user.middleName, user.lastName, user.mobileNo,
                user.age, user.gender, hobbies)
              hobby.returnAll().flatMap{ hobby =>
                userRepo.checkAdmin(userEmail).map{
                  case true =>Ok(views.html.updateForm(form.userProfileDataForm.fill(userProfile),hobby,true))
                  case false =>Ok(views.html.updateForm(form.userProfileDataForm.fill(userProfile),hobby,false))
                }

              }
          }
      }

      case None => Future.successful(Redirect(routes.Application.index1())
        .flashing("unauthorised" -> "You need to log in first!"))
    }
  }


  def updateUserProfile(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    val userEmail = request.session.get("user")
    userEmail match {

      case Some(user) =>
        form.userProfileDataForm.bindFromRequest.fold(
          formWithErrors => {
            Logger.info("dvdvd" + formWithErrors)
            hobby.returnAll().flatMap {
              hobbies =>
                userRepo.checkAdmin(user).map {
                  case true => BadRequest(views.html.updateForm(formWithErrors, hobbies, true))
                  case false => BadRequest(views.html.updateForm(formWithErrors, hobbies, false))
                }
            }
          },
          userProfile => {
            Logger.info("Updating Your Data")
            userRepo.updateInfo(userProfile, user).flatMap {
              case true => userRepo.findByUserEmailGetId(user).flatMap{
                case Some(id)=> userToHobby.updateUserHobby(id,userProfile.hobbies).map{
                  case true => Logger.info("Updated hobbies")
                    Redirect(routes.UserController.showProfile()).flashing("Success"->"Your Profile is updated")
                  case false=>Redirect(routes.UserController.showProfile()).flashing("Error"->"Error while Updating your hobbies")
                }

                case None => Future.successful(Redirect(routes.Application.index()).withNewSession)
              }
              case false => Future.successful(Redirect(routes.Application.index()).withNewSession)
            }
          })


      case None => Future.successful(Redirect(routes.Application.index1()).flashing("unauthorised" -> "You need to log in first!").withNewSession)
    }
  }

  def updatePassword(): Action[AnyContent] =Action.async{ implicit request =>
    form.updatePasswordConstraints.bindFromRequest.fold(
      formWithErrors => {
        Logger.error("Error while creating an account :" + formWithErrors)
        Future.successful(Redirect(routes.Application.updatePass()).flashing("Error" -> "Fill Form Correctly"))
      },
      userUpdatePassword => {
        userRepo.findByUserEmail(userUpdatePassword.userEmail).flatMap {

          case Some(userEmailFromData) => Logger.info("Username is found" + userEmailFromData)

            val encryptPassword = BCrypt.hashpw(userUpdatePassword.password, BCrypt.gensalt)

            userRepo.updateUserPassword(encryptPassword,userUpdatePassword.userEmail).map{

              case true => Logger.info("Password changed")
                Redirect(routes.Application.login()).withNewSession

              case false => Logger.error("Error while updating Password")
                Redirect(routes.Application.updatePass()).flashing("Error"->"Try Again")
            }
          case None => Logger.info("No User by this username")
            Future.successful(Redirect(routes.Application.updatePass()).flashing("Error" -> "No User by this username"))
        }

      })

  }

}

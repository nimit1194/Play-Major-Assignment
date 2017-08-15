package controllers
import javax.inject.Inject
import models._
import play.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.api.mvc.AnyContent
import org.mindrot.jbcrypt.BCrypt
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class ClientProfile (firstName: String, lastName: String,
                          age: Int, email: String)

class ClientControllers @Inject()
(val messagesApi: MessagesApi,
 val userRepo: UserRepository,
 val userToHobby:UserToHobbyServices,
 val form: UserForm)
  extends Controller with I18nSupport {

  def loginUser: Action[AnyContent] = Action.async { implicit request =>
    form.loginConstraints.bindFromRequest.fold(
      formWithErrors => {
        Logger.error("Error while login : " + formWithErrors)
        Future.successful(BadRequest(views.html.Login(formWithErrors)).flashing("Error" -> "Fill Form Correctly"))
      },
      userData => {
        userRepo.findByUserEmail(userData.userEmail).flatMap {

          case Some(userEmail) =>
            Logger.info("Checking In Database For login" + userEmail)

            userRepo.checkLoginValue(userData.userEmail, userData.password).flatMap {
              case true => userRepo.checkAdmin(userData.userEmail).flatMap {

                case true => Future.successful(Redirect(routes.Application.index1()).flashing("Success" -> "Thank You for registration As admin").withSession("user" -> userData.userEmail))

                case false => userRepo.checkEnable(userData.userEmail).map {

                  case true => Redirect(routes.Application.loginUser()).flashing("Success" -> "Thank You for registration as user").withSession("user" -> userData.userEmail)

                  case false => Redirect(routes.Application.login()).flashing("Error" -> "Your are disable")
                }

              }
              case false => Future.successful(Redirect(routes.Application.login()).flashing("Error" -> "Wrong Username or Password"))
            }
          case None => Future.successful(Redirect(routes.Application.login()).flashing("Error" -> "No user by this username"))
        }
      })
  }

  def registerUser: Action[AnyContent] = Action.async { implicit request =>
    form.userConstraints.bindFromRequest.fold(
      formWithErrors => {
        Logger.error("Error while creating an account :" + formWithErrors)
        Future.successful(Redirect(routes.Application.signUp()).flashing("Error" -> "Fill Form Correctly"))
      },
      userData => {
        userRepo.findByUserEmail(userData.userEmail).flatMap {

          case Some(userEmailFromData) => Logger.error("Username already exist, try with new one" + userEmailFromData)
            Future.successful(Redirect(routes.Application.signUp()).flashing("Error" -> "userEmail Already exists"))

          case None => Logger.info("UserInfo after creating an account : " + userData)
            val encryptPassword = BCrypt.hashpw(userData.password, BCrypt.gensalt)
            userRepo.store(UserDataModel(0, userData.firstName, userData.middleName, userData.lastName,
              userData.userEmail, encryptPassword, userData.mobileNo, userData.gender, userData.age, false, true))
              .flatMap {
                case true =>
                  userRepo.findByUserEmailGetId(userData.userEmail).flatMap {
                    case Some(id) => userToHobby.store(id, userData.hobbyID).map {
                      case true => Redirect(routes.Application.index1()).flashing("Success" -> "Thank You for registration").withSession("user" -> userData.userEmail)
                      case false => Redirect(routes.Application.index1()).flashing("Error" -> "Error while linking hobbies")
                    }
                    case None => Future.successful(Redirect(routes.Application.index1()).flashing("Error" -> "Error while registration"))
                  }
                case false => Future.successful(Redirect(routes.Application.index1()).flashing("Error" -> "Error while registration"))
              }
        }
      })

  }

}
package controllers
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints.{max, min}
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

import scala.util.matching.Regex

case class UserData (firstName: String, middleName: Option[String], lastName: String, userEmail: String,
                     password: String, confirmPassword: String, mobileNo: Long, gender: String,
                     age: Int, hobbyID: List[Int])

case class LoginUser (userEmail: String, password: String)

case class UserProfileData (firstName: String, middleName: Option[String], lastName: String, mobileNo: Long,
                            age: Int, gender: String, hobbies: List[Int])

case class UpdatePassword (userEmail: String, password: String, confirmPassword: String)

case class AssignmentForm (objective: String, instructions: String)

/*object UserProfileData {
  def apply (list: List[(String, Option[String], String, Long, Int, String, List[Int])]): UserProfileData = {
    val firstName = list.head._1
    val middleName = list.head._2
    val lastName = list.head._3
    val mobileNo = list.head._4
    val age = list.head._5
    val gender = list.head._6
    val hobbies = list.head._7
    new UserProfileData(firstName, middleName, lastName, mobileNo, age, gender, hobbies)
  }
}*/

class UserForm{
  val Numbers: Regex = """\d*""".r
  val Letters: Regex = """[A-Za-z]*""".r
  val mobileNoValidationConstraint: Constraint[Long] = Constraint[Long]("constraint.mobileNo.check") {
    number =>
      if (number.toString.length != 10) Invalid(ValidationError("Mobile No should be of 10 digits "))
      else Valid
  }

  def passwordValidationConstraint: Constraint[String] =
    Constraint[String]("constraints.password.check") {
      case "" => Invalid(ValidationError("Password missing"))
      case Numbers() => Invalid(ValidationError("Password is all numbers"))
      case Letters() => Invalid(ValidationError("Password is all letters"))
      case password if password.length() < 5 => Invalid(ValidationError("Password is short"))
      case _ => Valid
    }

  val userConstraints: Form[UserData] = Form(mapping(
    "firstName" -> nonEmptyText.verifying("Please enter first name", firstName => !firstName.isEmpty),
    "middleName" -> optional(text),
    "lastName" -> nonEmptyText.verifying("Please enter first name", lastName => !lastName.isEmpty),
    "userEmail" -> nonEmptyText,
    "password" -> nonEmptyText.verifying(passwordValidationConstraint),
    "confirmPassword" -> nonEmptyText,
    "mobileNo" -> longNumber.verifying(mobileNoValidationConstraint),
    "gender" -> text,
    "age" -> number.verifying(min(18), max(75)),
    "hobbyID" -> list(number)
  )(UserData.apply)(UserData.unapply)
    verifying("Failed form constraints!", userInfo => userInfo.password equals userInfo.confirmPassword))

  val loginConstraints: Form[LoginUser] = Form(mapping(
    "userEmail" -> nonEmptyText,
    "password" -> nonEmptyText.verifying(passwordValidationConstraint)
  )(LoginUser.apply)(LoginUser.unapply))

  val userProfileDataForm = Form(mapping
  ("firstName" -> text.verifying("Please enter first name", firstName => !firstName.isEmpty),
    "middleName" -> optional(text),
    "lastName" -> text.verifying("Please enter last name", lastName => !lastName.isEmpty),
    "mobileNo" -> longNumber.verifying(mobileNoValidationConstraint),
    "age" -> number(min = 18, max = 75),
    "gender" -> nonEmptyText,
    "hobbies" -> list(number)
  )(UserProfileData.apply)(UserProfileData.unapply))

  val updatePasswordConstraints: Form[UpdatePassword] = Form(mapping(
    "userName" -> nonEmptyText,
    "password" -> nonEmptyText.verifying(passwordValidationConstraint),
    "confirmPassword" -> nonEmptyText.verifying(passwordValidationConstraint)
  )(UpdatePassword.apply)(UpdatePassword.unapply)
    verifying("Failed form constraints!", data => data.password equals data.confirmPassword))

  val AssignmentConstraints: Form[AssignmentForm] = Form(mapping(
    "objective" -> nonEmptyText,
    "instructions" -> nonEmptyText
  )(AssignmentForm.apply)(AssignmentForm.unapply))

}


/*
case class CustomerData (firstName: String,
                         middleName: Option[String],
                         lastName: String,
                         userEmail: String,
                         password: String,
                         confirmPassword: String,
                         mobile: Int,
                         gender: String,
                         age: Int)

case class LoginData (
                       userEmail: String,
                       password: String
                     )

case class UpdateCustomerForm (firstName: String,
                               middleName: Option[String],
                               lastName: String,
                               userEmail: String,
                               gender: String,
                               mobile: Int,
                               age: Int)
*/

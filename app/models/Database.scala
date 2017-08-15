package models
import javax.inject.Inject
import controllers.UserProfileData
import org.mindrot.jbcrypt.BCrypt
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.i18n.MessagesApi
import slick.driver.JdbcProfile
import slick.lifted.ProvenShape
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class UserDataModel(id: Int, firstName: String, middleName: Option[String], lastName: String,
                    userEmail: String, password: String, mobileNo: Long,
                    gender: String, age: Int,isAdmin : Boolean, isEnable : Boolean)

class UserRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,val messagesApi: MessagesApi)
  extends UserDataRepository {

  implicit val messages: MessagesApi = messagesApi

  import driver.api._

  def store(userData: UserDataModel): Future[Boolean] = {
    db.run(userInfoQuery += userData).map(_ > 0)
  }

  def findByUserEmail(userEmail: String): Future[Option[String]] = {
    db.run(userInfoQuery.filter(_.userEmail === userEmail).map(_.userEmail).result.headOption)
  }

  def findByUserEmailGetId(userEmail: String): Future[Option[Int]] = {
    db.run(userInfoQuery.filter(_.userEmail === userEmail).map(_.id).result.headOption)
  }

  def checkAdmin(userEmail:String): Future[Boolean] ={
    db.run(userInfoQuery.filter(_.userEmail === userEmail).map(_.isAdmin).result.head)
  }

  def checkEnable(userEmail:String): Future[Boolean] = {
    db.run(userInfoQuery.filter(_.userEmail === userEmail).map(_.isEnable).result.head)
  }

  def checkLoginValue(userEmail:String, password : String): Future[Boolean] ={

    val users: Future[List[UserDataModel]] = db.run(userInfoQuery.filter(_.userEmail === userEmail).to[List].result)
    users.map { user =>
      if (user.isEmpty) {
        false
      }
      else if (BCrypt.checkpw(password, user.head.password)) {
        true
      }
      else {
        false
      } }
  }

  def retrieve(userEmail: String): Future[List[UserDataModel]] = {
    db.run(userInfoQuery.filter(_.userEmail === userEmail).to[List].result)

  }

  def retrieveAll(): Future[List[UserDataModel]] ={
    db.run(userInfoQuery.to[List].result)
  }

  def updateInfo(userProfileData: UserProfileData,userEmail:String): Future[Boolean] ={
    db.run(userInfoQuery.filter(_.userEmail===userEmail).map(user=> (user.firstName,user.middleName,user.lastName,
      user.mobileNo,user.age,user.gender)).update(userProfileData.firstName,userProfileData.middleName,
      userProfileData.lastName,userProfileData.mobileNo,userProfileData.age,userProfileData.gender)).map(_ > 0)
  }

  def makeDisable(userEmail:String): Future[Boolean] ={
    db.run(userInfoQuery.filter(_.userEmail=== userEmail).map(_.isEnable).update(false)).map(_ > 0)
  }

  def makeEnable(userEmail:String): Future[Boolean] ={
    db.run(userInfoQuery.filter(_.userEmail=== userEmail).map(_.isEnable).update(true)).map(_ > 0)
  }

  def updateUserPassword(userEmail: String,password: String): Future[Boolean] ={
    db.run(userInfoQuery.filter(_.userEmail===userEmail).map(_.password).update(password)).map(_ > 0)
  }
}

trait UserDataRepository extends HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  val userInfoQuery: TableQuery[UserDataTable] = TableQuery[UserDataTable]

  class UserDataTable(tag: Tag) extends Table[UserDataModel](tag, "userdata") {

    def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def firstName: Rep[String] = column[String]("firstname")

    def middleName: Rep[Option[String]] = column[Option[String]]("middlename")

    def lastName: Rep[String] = column[String]("lastname")

    def userEmail: Rep[String] = column[String]("useremail")

    def password: Rep[String] = column[String]("password")

    def mobileNo: Rep[Long] = column[Long]("mobileno")

    def gender: Rep[String] = column[String]("gender")

    def age: Rep[Int] = column[Int]("age")

    def isAdmin: Rep[Boolean] = column[Boolean]("isadmin")

    def isEnable: Rep[Boolean] = column[Boolean]("isenable")

    def * : ProvenShape[UserDataModel] = (id, firstName, middleName, lastName, userEmail, password, mobileNo, gender,
      age,isAdmin,isEnable) <> (UserDataModel.tupled, UserDataModel.unapply)
  }

}

/*
import javax.inject.Inject
import play.api.db.slick._
import slick.driver.JdbcProfile
import slick.lifted.ProvenShape
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.Logger


case class ClientData (id: Int,
                       firstName: String,
                       middleName: Option[String],
                       lastName: String,
                       userEmail: String,
                       password: String,
                       mobile: Long,
                       gender: String,
                       age: Int)

case class UpdateClientData (
                             firstName: String,
                             middleName: Option[String],
                             lastName: String,
                             mobile: Int,
                             gender: String,
                             age: Int)

class ClientRepository @Inject()
(protected val dbConfigProvider: DatabaseConfigProvider)
  extends Client {

  import driver.api._

  def store (client: ClientData): Future[Boolean] = {
    db.run(clientQuery += client).map(_ > 0)
  }
  def checkIfUserExists(email: String, password: String): Future[Boolean] = {
    Logger.info("Checking if user exists in Database")
    val userList = db.run(clientQuery.filter(user =>user.userEmail === email && user.password === password).to[List].result)
    userList.map { user =>
      if (user.isEmpty) {
        false
      }
      else {
        true
      }
    }
  }
  def getUserID(email: String): Future[List[Int]] = {
    Logger.info("Getting user ID based on user E-mail")
    db.run(clientQuery.filter(_.userEmail === email).map(_.id).to[List].result)
  }

  def findByEmail (email: String): Future[Option[String]] = {
    val query = clientQuery.filter(_.userEmail === email).map(_.userEmail).result.headOption
    db.run(query)
  }
  def checkEmailForUpdate(email: String, id: Int): Future[Boolean] = {
    val emailList = db.run(clientQuery.filter(user => user.userEmail === email && user.id =!= id).to[List].result)
    emailList.map { email =>
      if (email.isEmpty) true else false
    }
  }

  def updateUser (updateUser: UpdateClientData,id:Int): Future[Boolean] = {
    Logger.info("PAss")
    val querry = clientQuery.filter(_.id === id).map(user => (user.firstName, user.middleName, user.lastName,
      user.mobile, user.gender, user.age)).update((updateUser.firstName, updateUser.middleName,
      updateUser.lastName,updateUser.mobile,  updateUser.gender, updateUser.age))
    Logger.info("PAss2")
    db.run(querry).map(_ > 0)
  }

}

trait Client extends HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  val clientQuery: TableQuery[clientTableMapping] = TableQuery[clientTableMapping]

  class clientTableMapping (tag: Tag) extends Table[ClientData](tag, "userdata") {
    def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def firstName: Rep[String] = column[String]("firstname")

    def middleName: Rep[Option[String]] = column[Option[String]]("middlename")

    def lastName: Rep[String] = column[String]("lastname")

    def userEmail: Rep[String] = column[String]("useremail")

    def password: Rep[String] = column[String]("password")

    def mobile: Rep[Long] = column[Long]("mobile")

    def gender: Rep[String] = column[String]("gender")

    def age: Rep[Int] = column[Int]("age")

    def * : ProvenShape[ClientData] = (id, firstName, middleName, lastName, userEmail, password, mobile, gender, age) <> (ClientData.tupled, ClientData.unapply)
  }

}
*/

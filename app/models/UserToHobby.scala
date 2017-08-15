package models

import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.i18n.MessagesApi
import slick.driver.JdbcProfile
import slick.lifted.ProvenShape
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class UsertoHobby (id: Int, userId: Int, hobbyId: Int)

class UserToHobbyServices @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, val messagesApi: MessagesApi)
  extends userToHobbyRepository {

  implicit val messages: MessagesApi = messagesApi

  import driver.api._

  def store (userID: Int, hobbyID: List[Int]): Future[Boolean] = {
    val listValidHobbyID = hobbyID.filter(_ != Nil)
    val listOfResult: List[Future[Boolean]] = listValidHobbyID.map(
      hobbyID => db.run(userTohobbyInfoQuery += UsertoHobby(0, userID, hobbyID)).map(_ > 0)
    )
    Future.sequence(listOfResult).map {
      result =>
        if (result.contains(false)) false else true
    }
  }

  def getUserHobby (userTd: Int): Future[List[Int]] = {
    db.run(userTohobbyInfoQuery.filter(_.userId === userTd).map(_.hobbyId).to[List].result)
  }

  def updateUserHobby (userId: Int, hobbyId: List[Int]): Future[Boolean] = {
    db.run(userTohobbyInfoQuery.filter(_.userId === userId).delete).map(_ > 0)
    store(userId, hobbyId)
  }
}

trait userToHobbyRepository extends HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  val userTohobbyInfoQuery: TableQuery[UserToHobbyTable] = TableQuery[UserToHobbyTable]

  class UserToHobbyTable (tag: Tag) extends Table[UsertoHobby](tag, "usertohobby") {

    def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def userId: Rep[Int] = column[Int]("userid")

    def hobbyId: Rep[Int] = column[Int]("hobbyid")

    def * : ProvenShape[UsertoHobby] = (id, userId, hobbyId) <> (UsertoHobby.tupled, UsertoHobby.unapply)
  }

}

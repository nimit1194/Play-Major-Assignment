package models
import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.i18n.MessagesApi
import slick.driver.JdbcProfile
import slick.lifted.ProvenShape
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class Hobby(id: Int, hobbyText: String)

trait HobbyRepository extends HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  val hobbyInfoQuery: TableQuery[HobbyTable] = TableQuery[HobbyTable]

  class HobbyTable(tag: Tag) extends Table[Hobby](tag, "hobby") {

    def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def hobbyText: Rep[String] = column[String]("hobbytext")

    def * : ProvenShape[Hobby] = (id, hobbyText) <> (Hobby.tupled, Hobby.unapply)

  }

}
class HobbyServices @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,val messagesApi: MessagesApi)
  extends HobbyRepository {

  implicit val messages: MessagesApi = messagesApi

  import driver.api._

  def store(hobby: Hobby): Future[Boolean] = {
    db.run(hobbyInfoQuery += hobby).map(_ > 0)
  }

  def  returnAll(): Future[List[Hobby]] = {
    db.run(hobbyInfoQuery.to[List].result)
  }

}


package models

import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.i18n.MessagesApi
import slick.driver.JdbcProfile
import slick.lifted.ProvenShape
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class Assignment (id: Int, title: String, description: String)

  class AssignmentServices @Inject() (protected val dbConfigProvider: DatabaseConfigProvider,
                                      val messagesApi: MessagesApi)
    extends AssignmentRepository {

    implicit val messages: MessagesApi = messagesApi

    import driver.api._

    def store (assignment: Assignment): Future[Boolean] = {
      db.run(AssignmentInfoQuery += assignment).map(_ > 0)
    }

    def returnAll (): Future[List[Assignment]] = {
      db.run(AssignmentInfoQuery.to[List].result)
    }

    def delete (id: Int): Future[Boolean] = {
      db.run(AssignmentInfoQuery.filter(_.id === id).delete).map(_ > 0)
    }
  }
trait AssignmentRepository extends HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  val AssignmentInfoQuery: TableQuery[AssignmentTable] = TableQuery[AssignmentTable]

  class AssignmentTable (tag: Tag) extends Table[Assignment](tag, "assignment") {

    def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def objective: Rep[String] = column[String]("objective")

    def instructions: Rep[String] = column[String]("instructions")

    def * : ProvenShape[Assignment] = (id, objective, instructions) <> (Assignment.tupled, Assignment.unapply)
  }


}

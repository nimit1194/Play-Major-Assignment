package models
import scala.concurrent.duration._
import play.api.Application
import play.api.test.WithApplicationLoader
import scala.concurrent.{Await,Future}
import scala.reflect.ClassTag

class ModelsTest[T: ClassTag] extends WithApplicationLoader {
  lazy val app2dao = Application.instanceCache[T]

  lazy val repository: T = app2dao(app)

  def result[R](response: Future[R]): R =
    Await.result(response, 2.seconds)
}





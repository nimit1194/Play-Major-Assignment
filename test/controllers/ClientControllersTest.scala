package controllers
import akka.stream.Materializer
import models.{ClientData, ClientRepository}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.test.Helpers._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ClientControllersTest extends PlaySpec with MockitoSugar with GuiceOneAppPerSuite {
  implicit lazy val materializer: Materializer = app.materializer

  val m: MessagesApi = mock[MessagesApi]
  val c: ClientRepository = mock[ClientRepository]
  val f: ClientForm = mock[ClientForm]
  val clientControllerObj = new ClientControllers(m, c, f)

  "Client-Controller" should {

    "be able to create customer account" in {
      val customer1 = CustomerData("Shubham",Option("K"), "Verma","shubh@gmail.com", "password123", "password123",
        45661, "Male",23)
      val form = new ClientForm().customerFormConstraints.fill(customer1)
      when(f.customerFormConstraints).thenReturn(form)
      when(c.findByEmail("shubh@gmail.com")).thenReturn(Future(None))
      when(c.store(ClientData(0, "Shubham",Option("K"),"Verma","shubh@gmail.com", "password123",45661,"Male",23)))
        .thenReturn(Future(true))

      val result = clientControllerObj.registrationValidation.apply(FakeRequest(POST, "/customer_account")
        .withFormUrlEncodedBody(
        "firstName"->"Shubham",
        "middleName"->"K",
        "lastName"->"Verma",
        "userEmail" -> "shubh@gmail.com",
        "password" -> "password123",
        "confirmPassword"->"password123",
        "mobile"->"45661",
        "gender"->"Male",
        "age"->"23"))

      redirectLocation(result) mustBe Some("/success")
    }
  }
}

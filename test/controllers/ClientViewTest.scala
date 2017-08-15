package controllers
import models.ClientRepository
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.test.Helpers._

/**
  * Created by knoldus on 11/8/17.
  */
class ClientViewTest extends PlaySpec with MockitoSugar {
  val m: MessagesApi = mock[MessagesApi]
  val c: ClientRepository = mock[ClientRepository]
  "Client-View" should {
    val controller = new ClientView(m, c)

    "render user to the main page" in {
      val result = controller.main()(FakeRequest())
      status(result) mustBe OK
    }
    "render user to the login page" in {
      val result = controller.login()(FakeRequest())
      status(result) mustBe OK
    }
    "render user to the registration page" in {
      val result = controller.register()(FakeRequest())
      status(result) mustBe OK
    }
    "render user to the successful registration page" in {
      val result = controller.success()(FakeRequest())
      status(result) mustBe OK
    }
    "Error in the information given by user" in {
      val result = controller.errors()(FakeRequest())
      status(result) mustBe OK
    }
  }
}

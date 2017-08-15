package models

import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

class DatabaseTest extends PlaySpec with MockitoSugar {

  val clientRepository = new ModelsTest[ClientRepository]

  val client1 = ClientData(12, "prab", Option(""), "kuamr", "fhkjda@gmail.com", "hello", 9717857151L,
    "M", 23)


  "Testing-User-Databse" should {
    "Storing DB" in {
      val result = clientRepository.result(clientRepository.repository.store(client1))

    }

  }
}





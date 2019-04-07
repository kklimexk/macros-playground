import ToStringObfuscateMain.Obfuscated.{TestCreditCard, TestPassword, UserPassword}

object ToStringObfuscateMain extends App {
  object Obfuscated {

    @ToStringObfuscate("password", "pinCode")
    case class TestPassword(name: String, username: String, password: String, pinCode: String)

    @ToStringObfuscate("cardNumber")
    case class TestCreditCard(cardNumber: String, cvv: Int, endDate: String)

    @ToStringObfuscate("password")
    case class UserPassword(username: String, password: String)

  }

  val testPassword = TestPassword("n1", "u1", "ppp1", "cccc1")
  val testCreditCard = TestCreditCard("c2", 2, "endDate2")
  val userPassword = UserPassword("u3", "password")

  println(testPassword)
  println(testCreditCard)
  println(userPassword)
}

package sns.lando.system

import org.scalatest.{FunSpec, GivenWhenThen}

class FirstSpec extends FunSpec with GivenWhenThen {

  def listenToAcceptedMessages() = {

  }

  def writeValidOperatorMessageToSns() = {

  }

  def verifySnsHasAcceptedTheMessage() = {

  }

  describe("SNS") {
    it("should accept valid operator messages") {
      listenToAcceptedMessages()

      When("a valid operator message is received by SNS")
      writeValidOperatorMessageToSns()

      Then("SNS accepts the valid message")
      verifySnsHasAcceptedTheMessage()
    }
  }
}

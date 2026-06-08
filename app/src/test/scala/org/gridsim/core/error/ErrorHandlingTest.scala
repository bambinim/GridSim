package org.gridsim.core.error

import cats.Id
import cats.syntax.all.*
import org.gridsim.core.common.Reporter
import org.gridsim.core.model.error.DomainError
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class ErrorHandlingTest extends AnyFlatSpec with Matchers {
  val error: DomainError = DomainError.ValueMustBePositive("Test", -5)

  "ErrorRenderer" should "correctly format ValueMustBePositive Error" in {
    error.show should include ("[ERROR] Field 'Test' cannot be negative. Provided: -5")
  }

  "Reporter" should "send formatted error to the provided printer" in {
    var capturedOutput = ""
    val testPrinter: String => Id[Unit] = (s: String) => capturedOutput = s
    val reporter = Reporter.console[Id, DomainError](testPrinter)

    reporter.report(error)
    capturedOutput should include ("[ERROR] Field 'Test' cannot be negative. Provided: -5")
  }

  it should "send formatted errors to the provided printer" in {
    var capturedOutput = List.empty[String]
    val testPrinter: String => Id[Unit] = (s: String) => {
      capturedOutput = capturedOutput :+ s
      ()
    }
    val reporter = Reporter.console[Id, DomainError](testPrinter)

    val error2 = DomainError.InvalidId("Test", "n")
    val errL = List(error,error2)

    reporter.reportAll(errL)

    capturedOutput should not be empty
    capturedOutput.size shouldBe 2

    capturedOutput.head should include("[ERROR] Field 'Test' cannot be negative. Provided: -5")
    capturedOutput.last should include(s"[ERROR] Identifier 'Test' for n is invalid")
  }

}

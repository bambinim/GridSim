package org.gridsim.core.statistics

import cats.kernel.Monoid
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FoldSpec extends AnyFlatSpec with Matchers:

  // Local, additive Int monoid: keeps this spec fully independent of any
  // domain type (FlowStatistic etc.), so it only exercises Fold's own machinery.
  private given Monoid[Int] with
    def empty: Int = 0
    def combine(a: Int, b: Int): Int = a + b

  private def runFold[In, Out](fold: Fold[In, Out], inputs: List[In]): Out =
    fold.extract(inputs.foldLeft(fold.initial)(fold.step))

  "Fold.monoidal" should "start at the monoid's empty value" in:
    val sumFold = Fold.monoidal[Int, Int](identity)
    sumFold.extract(sumFold.initial) shouldBe 0

  it should "combine every sampled input via the monoid" in:
    val sumFold = Fold.monoidal[Int, Int](identity)
    runFold(sumFold, List(1, 2, 3, 4)) shouldBe 10

  it should "apply the sampling function before combining, not after" in:
    val doubleSumFold = Fold.monoidal[Int, Int](_ * 2)
    runFold(doubleSumFold, List(1, 2, 3)) shouldBe 12

  "Fold.unfold" should "thread arbitrary, non-monoidal state through step" in:
    val lastSeenFold = Fold.unfold[Int, Option[Int], Option[Int]](None)((_, in) => Some(in))(identity)
    runFold(lastSeenFold, List(1, 2, 3)) shouldBe Some(3)

  it should "start at the given initial state when no input has arrived" in:
    val lastSeenFold = Fold.unfold[Int, Option[Int], Option[Int]](None)((_, in) => Some(in))(identity)
    lastSeenFold.extract(lastSeenFold.initial) shouldBe None

  it should "let extract's output shape differ from the internal accumulator's shape" in:
    val countFold = Fold.unfold[Int, List[Int], Int](Nil)((acc, in) => in :: acc)(_.size)
    runFold(countFold, List(10, 20, 30)) shouldBe 3

  "Fold.map" should "transform only the extracted output, not the accumulation itself" in:
    val sumFold = Fold.monoidal[Int, Int](identity)
    val labeledFold = sumFold.map(total => s"total=$total")
    runFold(labeledFold, List(1, 2, 3)) shouldBe "total=6"

  it should "compose left-to-right across multiple calls" in:
    val sumFold = Fold.monoidal[Int, Int](identity)
    val composed = sumFold.map(_ + 1).map(_ * 10)
    runFold(composed, List(1, 2, 3)) shouldBe 70 // (6 + 1) * 10

  "Fold.contramap" should "adapt a fold built for a narrow input to a wider one" in:
    case class Event(value: Int, noise: String)
    val sumFold = Fold.monoidal[Int, Int](identity)
    val adapted: Fold[Event, Int] = sumFold.contramap(_.value)
    runFold(adapted, List(Event(1, "x"), Event(2, "y"), Event(3, "z"))) shouldBe 6

  it should "give the same result as pre-mapping the input list by hand" in:
    val sumFold = Fold.monoidal[Int, Int](identity)
    val adapted = sumFold.contramap[String](_.length)
    val inputs = List("a", "bb", "ccc")
    runFold(adapted, inputs) shouldBe runFold(sumFold, inputs.map(_.length))

  "independent Fold instances built from the same constructor" should "not share state" in:
    val a = Fold.monoidal[Int, Int](identity)
    val b = Fold.monoidal[Int, Int](identity)
    val steppedA = a.step(a.initial, 100)
    a.extract(steppedA) shouldBe 100
    b.extract(b.initial) shouldBe 0

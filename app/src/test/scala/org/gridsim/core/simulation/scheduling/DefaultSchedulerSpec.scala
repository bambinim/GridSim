package org.gridsim.core.simulation.scheduling

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.concurrent.duration.*
import java.util.concurrent.atomic.AtomicInteger

class DefaultSchedulerSpec extends AnyFlatSpec with Matchers:

  "DefaultScheduler" should "execute a delayed task exactly once" in:
    val count = new AtomicInteger(0)
    val scheduler = DefaultScheduler()

    scheduler.scheduleOnce(() => {
      count.incrementAndGet()
    }, 20.millis)

    Thread.sleep(80)
    scheduler.stop()

    count.get() shouldBe 1

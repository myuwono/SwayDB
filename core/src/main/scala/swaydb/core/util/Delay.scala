/*
 * Copyright (C) 2018 Simer Plaha (@simerplaha)
 *
 * This file is a part of SwayDB.
 *
 * SwayDB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * SwayDB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with SwayDB. If not, see <https://www.gnu.org/licenses/>.
 */

package swaydb.core.util

import java.util.{Timer, TimerTask}

import scala.concurrent._
import scala.concurrent.duration.FiniteDuration
import scala.util.Try
import TryUtil._

private[swaydb] object Delay {

  private val timer = new Timer(true)

  private def runWithDelay[T](delayFor: FiniteDuration)(block: => Future[T])(implicit ctx: ExecutionContext): Future[T] = {
    val promise = Promise[T]()
    val task =
      new TimerTask {
        def run() {
          ctx.execute(
            new Runnable {
              override def run(): Unit =
                promise.completeWith(block)
            }
          )
        }
      }
    timer.schedule(task, delayFor.toMillis)
    promise.future
  }

  def apply[T](delayFor: FiniteDuration)(block: => Future[T])(implicit ctx: ExecutionContext): Future[T] =
    runWithDelay(delayFor)(block)

  def future[T](delayFor: FiniteDuration)(block: => T)(implicit ctx: ExecutionContext): Future[T] =
    runWithDelay(delayFor)(Future(block))

  def futureFromTry[T](delayFor: FiniteDuration)(block: => Try[T])(implicit ctx: ExecutionContext): Future[T] =
    runWithDelay(delayFor)(block.tryInFuture)
}

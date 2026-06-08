package org.gridsim.core.common

import cats.{Foldable, Monad, Show}
import cats.syntax.all.*

trait Reporter[F[_], A]:
  def report(item: A): F[Unit]
  def reportAll[G[_]: Foldable](items: G[A]): F[Unit]

object Reporter:
  def console[F[_]: Monad, A: Show](printer: String => F[Unit]): Reporter[F, A] = new Reporter[F, A]:
    def report(item: A): F[Unit] =
      printer(item.show)

    def reportAll[G[_] : Foldable](items: G[A]): F[Unit] =
      items.traverse_(item => report(item))



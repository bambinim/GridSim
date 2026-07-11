package org.gridsim.statistics

final case class Registration[In, A](key: StatKey[A], fold: Fold[In, A])

opaque type StatsBoard = Map[StatKey[?], Any]
object StatsBoard:
  def fromMap(map: Map[StatKey[?], Any]): StatsBoard = map
  extension (board: StatsBoard)
    def get[A](key: StatKey[A]): A = board(key).asInstanceOf[A] // safe: only the engine builds this map

object StatisticsEngine:
  def build[In](registrations: List[Registration[In, ?]]): Fold[In, StatsBoard] =
    new Fold[In, StatsBoard]:
      type State = Map[StatKey[?], Any]
      def initial: Map[StatKey[?], Any] = registrations.map(r => r.key -> r.fold.initial).toMap
      def step(s: State, in: In): Map[StatKey[?], Any] = registrations.map { r =>
          r.key -> r.fold.step(s(r.key).asInstanceOf[r.fold.State], in)
        }.toMap
      def extract(s: State): StatsBoard = StatsBoard.fromMap(registrations.map { r =>
          r.key -> r.fold.extract(s(r.key).asInstanceOf[r.fold.State])
        }.toMap)

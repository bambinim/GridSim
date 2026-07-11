package org.gridsim.statistics

/** A purely functional, single-pass accumulator over a stream of `In`. */
trait Fold[-In, +Out]:
  type State
  def initial: State
  def step(state: State, in: In): State
  def extract(state: State): Out

  final def map[NewOut](mapper: Out => NewOut): Fold[In, NewOut] =
    val self = this
    new Fold[In, NewOut]:
      type State = self.State
      def initial: self.State = self.initial
      def step(s: State, in: In): self.State = self.step(s, in)
      def extract(s: State): NewOut = mapper(self.extract(s))

  final def contramap[newIn](mapper: newIn => In): Fold[newIn, Out] =
    val self = this
    new Fold[newIn, Out]:
      type State = self.State
      def initial: self.State = self.initial
      def step(s: State, in2: newIn): self.State = self.step(s, mapper(in2))
      def extract(s: State): Out = self.extract(s)

object Fold:
  /** Any Monoid-based accumulator, it is a fold for free. */
  def monoidal[In, A](sample: In => A)(using monoid: cats.kernel.Monoid[A]): Fold[In, A] =
    new Fold[In, A]:
      type State = A
      def initial: A = monoid.empty
      def step(s: A, in: In): A = monoid.combine(s, sample(in))
      def extract(s: A): A = s

  /** General case, covers for example NetFlowHistory, running stats, anything with non monoidal state. */
  def unfold[In, S, Out](init: S)(stepFunction: (S, In) => S)(extractFunction: S => Out): Fold[In, Out] =
    new Fold[In, Out]:
      type State = S
      def initial: S = init
      def step(s: S, in: In): S = stepFunction(s, in)
      def extract(s: S): Out = extractFunction(s)

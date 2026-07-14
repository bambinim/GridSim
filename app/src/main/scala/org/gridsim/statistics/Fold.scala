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

  final def contramap[OldIn](mapper: OldIn => In): Fold[OldIn, Out] =
    val self = this
    new Fold[OldIn, Out]:
      type State = self.State
      def initial: self.State = self.initial
      def step(s: State, in2: OldIn): self.State = self.step(s, mapper(in2))
      def extract(s: State): Out = self.extract(s)

object Fold:
  def monoidal[In, A](sample: In => A)(using monoid: cats.kernel.Monoid[A]): Fold[In, A] =
    new Fold[In, A]:
      type State = A
      def initial: A = monoid.empty
      def step(s: A, in: In): A = monoid.combine(s, sample(in))
      def extract(s: A): A = s

  def unfold[In, S](init: S)(stepFunction: (S, In) => S): Fold[In, S] =
    unfold(init)(stepFunction)(identity)

  def unfold[In, S, Out](init: S)(stepFunction: (S, In) => S)(extractFunction: S => Out): Fold[In, Out] =
    new Fold[In, Out]:
      type State = S
      def initial: S = init
      def step(s: S, in: In): S = stepFunction(s, in)
      def extract(s: S): Out = extractFunction(s)

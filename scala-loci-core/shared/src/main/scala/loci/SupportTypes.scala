package loci

import typeconstraints._
import scala.annotation.implicitNotFound
import scala.language.experimental.macros

protected trait NotNothing[T]

protected object NotNothing {
  implicit def nothing[T]
    (implicit
        ev: T =:= Nothing): NotNothing[T] = `#macro`
  implicit def nothingAmbiguousEvidence[T]
    (implicit
        ev: T =:= T): NotNothing[T] = `#macro`
}


@implicitNotFound("Expression not placeable on peer")
final abstract class PlacingTypes[P <: Peer, T, +U]

protected trait PlacingTypesFallback {
  implicit def nothingOrNotInferred[P <: Peer, T, NothingButLessSpecific]:
    PlacingTypes[P, T, NothingButLessSpecific] = `#macro`
}

protected object PlacingTypes extends PlacingTypesFallback {
  implicit def localPlacedType[P <: Peer, P0 <: Peer, T, U]
    (implicit
        ev0: T <:< (U localOn P0),
        ev1: P <:< P0): PlacingTypes[P, T, U] = `#macro`
  implicit def remotePlacedType[P <: Peer, P0 <: Peer, T, U]
    (implicit
        ev0: T <:< (U localOn P0),
        ev1: P <:!< P0): PlacingTypes[P, T, Unit] = `#macro`
  implicit def nonPlacedType[P <: Peer, T]
    (implicit
        ev: T <:!< (_ localOn _)): PlacingTypes[P, T, T] = `#macro`
}


@implicitNotFound("Expression not placeable on peer")
final abstract class RemotePlacingTypes[T, +U]

protected object RemotePlacingTypes {
  implicit def placedType[T, U]
    (implicit
        ev: T <:< (U localOn _)): RemotePlacingTypes[T, U] = `#macro`
  implicit def nonPlacedType[T]
    (implicit
        ev: T <:!< (_ localOn _)): RemotePlacingTypes[T, T] = `#macro`
}


@implicitNotFound("Subjective type not inferable")
final abstract class SubjectiveTypes[R <: Peer, T, +U]

protected object SubjectiveTypes {
  implicit def subjectiveType[R <: Peer, T]
    (implicit
        ev0: T <:!< (Remote[R] <=> _),
        ev1: T <:!< (Remote[R] <-> _),
        ev2: T <:!< (Remote[R] => _)): SubjectiveTypes[R, T, Remote[R] <-> T] = `#macro`
  implicit def controlledSubjectiveType[R <: Peer, T, U]
    (implicit
        ev: T <:< (Remote[R] => U)): SubjectiveTypes[R, T, Remote[R] <=> U] = `#macro`
  implicit def subjectiveTypePassed[R <: Peer, T, U]
    (implicit
        ev: T <:< (Remote[R] <-> U)): SubjectiveTypes[R, T, Remote[R] <-> U] = `#macro`
  implicit def controlledSubjectiveTypePassed[R <: Peer, T, U]
    (implicit
        ev: T <:< (Remote[R] <=> U)): SubjectiveTypes[R, T, Remote[R] <=> U] = `#macro`
}


protected final abstract class ValueTypes[T, R <: Remote[Peer], U, V]

protected trait ValueTypesNothingOrNotInferred {
  implicit def nothingOrNotInferred[T, U]
    (implicit ev: DummyImplicit): ValueTypes[T, Nothing, U, T] = `#macro`
}

protected trait ValueTypesIdentity extends ValueTypesNothingOrNotInferred {
  implicit def identity[T, U]
    (implicit ev: T =:= U): ValueTypes[T, Nothing, U, U] = `#macro`
}

protected object ValueTypes extends ValueTypesIdentity with ValueTypesHigherKinds {
  implicit def placedValue[T, U, V, Dummy]
    (implicit
        ev0: T <:< (U localOn _),
        ev1: U <:!< (_ <=> _),
        ev2: U <:!< (_ <-> _),
        ev3: ValueTypes[U, _, Dummy, V]): ValueTypes[T, Remote[Peer], V, V] = `#macro`
  implicit def placedValueControlledSubjective[R <: Remote[Peer], T, U, V, I, Dummy]
    (implicit
        ev0: NotNothing[T],
        ev1: T <:< (U localOn _),
        ev2: U <:< (R <=> I),
        ev3: ValueTypes[I, _, Dummy, V]): ValueTypes[T, R, V, R => V] = `#macro`
  implicit def placedValueSubjective[R <: Remote[Peer], T, U, V, I, Dummy]
    (implicit
        ev0: NotNothing[T],
        ev1: T <:< (U localOn _),
        ev2: U <:< (R <-> I),
        ev3: ValueTypes[I, _, Dummy, V]): ValueTypes[T, R, V, V] = `#macro`
  implicit def selectedValue[T]
    (implicit
        ev0: NotNothing[T],
        ev1: T <:< (_ from _)): ValueTypes[T, Nothing, Unit, Unit] = `#macro`
  implicit def selectedSingleValue[T]
    (implicit
        ev0: NotNothing[T],
        ev1: T <:< (_ fromSingle _)): ValueTypes[T, Nothing, Unit, Unit] = `#macro`
  implicit def selectedMultipleValue[T]
    (implicit
        ev0: NotNothing[T],
        ev1: T <:< (_ fromMultiple _)): ValueTypes[T, Nothing, Unit, Unit] = `#macro`
}


final abstract class LocalValueTypes[T, U]

protected trait LocalValueTypesFastMacro {
  implicit def materializeLocalValueTypes[T, U]
  : LocalValueTypes[T, U] =
    macro impl.LocalValueTypes.impl[T, U]
}

protected object LocalValueTypes extends
  loci.ide.intellij.LocalValueTypes with
  LocalValueTypesFastMacro


final abstract class RemoteValueTypes[T, R <: Remote[Peer], U]

protected trait RemoteValueTypesFastMacro {
  implicit def materializeRemoteValueTypes[T, R <: Remote[Peer], U]
  : RemoteValueTypes[T, R, U] =
    macro impl.RemoteValueTypes.impl[T, R, U]
}

protected object RemoteValueTypes extends
  loci.ide.intellij.RemoteValueTypes with
  RemoteValueTypesFastMacro


// type-level implementation for `LocalValueTypes` and `RemoteValueTypes`
// distinctly slower as compared to the macro implementation
//
// final abstract class LocalValueTypes[T, U]
//
// protected trait LocalValueTypesFallback {
//   implicit def nothing: LocalValueTypes[Nothing, Nothing] = `#macro`
// }
//
// protected object LocalValueTypes extends LocalValueTypesFallback {
//   implicit def localValueTypes[T, U, Dummy]
//     (implicit ev: ValueTypes[T, _, Dummy, U]): LocalValueTypes[T, U] = `#macro`
// }
//
//
// final abstract class RemoteValueTypes[T, R <: Remote[Peer], U]
//
// protected trait RemoteValueTypesFallback {
//   implicit def nothing: RemoteValueTypes[Nothing, Nothing, Nothing] = `#macro`
// }
//
// protected object RemoteValueTypes extends RemoteValueTypesFallback {
//   implicit def remoteValueTypes[T, R <: Remote[Peer], U, Dummy]
//     (implicit ev: ValueTypes[T, R, U, Dummy]): RemoteValueTypes[T, R, U] = `#macro`
// }

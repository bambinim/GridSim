package org.gridsim.core.model.error

import cats.Show
import cats.implicits.toShow

/** Represents the Algebraic Data Type of all possible domain-level failures.
  */
enum DomainError:
  /** Indicates that a numerical value that must be strictly positive was zero
    * or negative.
    *
    * @param field
    *   The name of the property that failed validation.
    * @param value
    *   The invalid value provided.
    */
  case ValueMustBePositive(field: String, value: Double)

  /** Indicates that a value fell outside its bounds.
    *
    * @param field
    *   The name of the property.
    * @param value
    *   The invalid value provided.
    * @param min
    *   The lower bound of the allowed range.
    * @param max
    *   The upper bound of the allowed range.
    */
  case OutOfRange(field: String, value: Double, min: Double, max: Double)

  /** Indicates that an identifier does not meet the required format or length
    * constraints.
    *
    * @param field
    *   The name of the identifier property.
    * @param str
    *   The invalid string that was provided.
    */
  case InvalidId(field: String, str: String)

  /** Indicates that an identifier appears more than once in a collection that
    * requires unique IDs.
    *
    * @param field
    *   The collection or property where the duplicate was found.
    * @param id
    *   The duplicated identifier.
    */
  case DuplicateId(field: String, id: String)

  /** Indicates that a referenced identifier has no matching domain object.
    *
    * @param id
    *   The missing identifier.
    */
  case IdNotFound(id: String)

  /** Indicates that a state entry is indexed by a key different from the
    * state's own entity identifier.
    *
    * @param key
    *   The key used in the simulation state's entity-state map.
    * @param entityId
    *   The identifier stored inside the entity state.
    */
  case EntityStateKeyMismatch(key: String, entityId: String)

  /** Indicates that the initial simulation state contains an entity state whose
    * identifier does not exist in the selected simulation model.
    *
    * @param entityId
    *   The state identifier that could not be matched to a model node.
    */
  case EntityStateWithoutModel(entityId: String)

  /** Indicates that a stored entity flow references an entity that is not
    * present in the selected simulation model.
    *
    * @param entityId
    *   The flow identifier that could not be matched to a model node.
    */
  case EntityFlowWithoutModel(entityId: String)

  /** Indicates that a stored cable load references a cable that is not present
    * in the selected simulation model.
    *
    * @param e1
    *   One endpoint of the unknown cable.
    * @param e2
    *   The other endpoint of the unknown cable.
    */
  case CableLoadWithoutCable(e1: String, e2: String)

  /** Indicates a validation failure regarding the grid graph topology.
    *
    * @param message
    *   A description of the topology error.
    */
  case TopologyError(message: String)

  /** Wraps another domain error to associate it with a specific entity
    * identifier.
    *
    * @param entityId
    *   the identifier of the entity that failed validation.
    * @param error
    *   the underlying domain error.
    */
  case EntityError(entityId: String, error: DomainError)

object DomainError:
  /** The implicit Cats Show instance for [[DomainError]]. By placing this
    * within the companion object, the compiler will automatically find it
    * whenever `.show` is called on a [[DomainError]] instance, completely
    * eliminating the need for explicit imports in other files.
    */
  given Show[DomainError] = Show.show {
    case TopologyError(message) =>
      s"[ERROR] Topology validation failed: $message"
    case EntityError(entityId, err) =>
      val innerMsg = err.show.replace("[ERROR] ", "").replace("[Error] ", "")
      s"[ERROR] Entity '$entityId': $innerMsg"
    case DomainError.InvalidId(f, s) =>
      s"[ERROR] Identifier '$f' for $s is invalid"
    case DuplicateId(f, id) =>
      s"[ERROR] Duplicate identifier '$id' found in '$f'"
    case ValueMustBePositive(f, v) =>
      s"[ERROR] Field '$f' cannot be negative. Provided: $v"
    case OutOfRange(f, v, min, max) =>
      s"[Error] Field '$f' must be in range between $min and $max. Provided: $v"
    case IdNotFound(i) =>
      s"[ERROR] Identifier match not found for id '$i'"
    case EntityStateKeyMismatch(key, entityId) =>
      s"[ERROR] Entity state map key '$key' does not match state entityId '$entityId'"
    case EntityStateWithoutModel(entityId) =>
      s"[ERROR] Entity state '$entityId' does not match any model node"
    case EntityFlowWithoutModel(entityId) =>
      s"[ERROR] Entity flow '$entityId' does not match any model node"
    case CableLoadWithoutCable(e1, e2) =>
      s"[ERROR] Cable load that connects '$e1' and '$e2' does not match any model cable"
  }

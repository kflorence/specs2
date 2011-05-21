package org.specs2
package matcher

import org.specs2.internal.scalaz.{Monoid, Reducer}

/**
 * MatchResultMessages are used to accumulate the results of several matches into a "summary" one
 */
private[specs2]
trait MatchResultMessages {

  implicit def MatchResultMessageReducer[T] = new Reducer[MatchResult[T], MatchResultMessage] {
    override def unit(r: MatchResult[T]) = r match {
      case MatchSuccess(ok, ko, e)    => SuccessMessage(ok, ko)
      case MatchFailure(ok, ko, e, d) => FailureMessage(ok, ko)
      case _                          => NeutralMessage(r.message)
    }
  }
  implicit val MatchResultMessageMonoid = new Monoid[MatchResultMessage] {
    val zero = new EmptySuccessMessage()
    def append(r1: MatchResultMessage, r2: =>MatchResultMessage) = r1 append r2
  }

  /**
   * A MatchResultMessage represents an accumulation of messages from different matches.
   *
   * It can be appended another MatchResultMessage so that there is a Semigroup for this class
   */
  sealed trait MatchResultMessage {
    def append(m2: MatchResultMessage): MatchResultMessage
    def isSuccess: Boolean = true
  }
  case class SuccessMessage(okMessage: String, koMessage: String) extends MatchResultMessage {
    def append(m2: MatchResultMessage) = {
      m2 match {
        case SuccessMessage(ok, ko) => SuccessMessage(okMessage+"; "+ok, koMessage+"; "+ko)
        case FailureMessage(ok, ko) => FailureMessage(okMessage+"; "+ok, koMessage+"; "+ko)
        case NeutralMessage(m)      => SuccessMessage(okMessage+"; "+m, koMessage+"; "+m)
        case _ => this
      }
    }
  }
  case class FailureMessage(okMessage: String, koMessage: String) extends MatchResultMessage {
    def append(m2: MatchResultMessage) = {
      m2 match {
        case SuccessMessage(ok, ko) => FailureMessage(okMessage+"; "+ok, koMessage+"; "+ko)
        case FailureMessage(ok, ko) => FailureMessage(okMessage+"; "+ok, koMessage+"; "+ko)
        case NeutralMessage(m)      => FailureMessage(okMessage+"; "+m, koMessage+"; "+m)
        case _ => this
      }
    }
    override def isSuccess: Boolean = false
  }
  case class NeutralMessage(message: String) extends MatchResultMessage {
    def append(m2: MatchResultMessage) = {
      m2 match {
        case SuccessMessage(ok, ko) => SuccessMessage(message+"; "+ok, message+"; "+ko)
        case FailureMessage(ok, ko) => FailureMessage(message+"; "+ok, message+"; "+ko)
        case NeutralMessage(m)      => NeutralMessage(message+"; "+m)
        case _ => this
      }
    }
  }
  case class EmptySuccessMessage() extends MatchResultMessage {
    def append(m2: MatchResultMessage) = m2
  }
}
private[specs2]
object MatchResultMessages extends MatchResultMessages

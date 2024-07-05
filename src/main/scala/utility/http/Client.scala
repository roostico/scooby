package org.unibo.scooby
package utility.http

import utility.http.Configuration.ClientConfiguration

import org.unibo.scooby.utility.http.Configuration.Property.NetworkTimeout

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.reflect.ClassTag

/**
 * Core of the HTTP logic. A [[HttpClient]] can mix-in a Backend to gain the corresponding, underlying implementation of
 * HTTP calls. Each Backend must specify the type of the generated HTTP responses: it can also (and in fact should) be a
 * type that does not depend on the underlying HTTP library.
 *
 * Note that while the [[Request]] type is used inside all the backends, the response's one [[R]] is specific to the
 * used Backend.
 *
 * @tparam R
 *   type of the HTTP responses generated by the Backend
 */
trait Backend[R] extends HttpClient:
  /**
   * Sends the provided [[Request]] using the Backend implementation
   *
   * @param request
   *   request to be sent
   * @return
   *   a response of type [[R]], depending on the Backend implementation
   */
  def send(request: Request): R

/**
 * Empty trait that represents an HTTP client. To be useful, you need to mix-in [[Backend]] Traits
 */
trait HttpClient(val configuration: ClientConfiguration)

object Clients:
  import utility.http.backends.SttpSyncBackend

  /**
   * Simple HTTP client used for synchronous HTTP calls with the sttp library as backend
   */
  class SimpleHttpClient(configuration: ClientConfiguration = Configuration.default)
      extends HttpClient(configuration) with SttpSyncBackend

type Client[R] = HttpClient & Backend[R]

object Configuration:
  import scala.concurrent.duration.DurationInt

  enum Property[T](val value: T):
    case NetworkTimeout(override val value: FiniteDuration) extends Property[FiniteDuration](value)
    case MaxRequests(override val value: Int) extends Property[Int](value)

  class ClientConfiguration(properties: Seq[Property[?]]):
    def property[T <: Property[R], R](implicit tag: ClassTag[T]): Option[R] =
      properties.collectFirst { case p if tag.runtimeClass.isInstance(p) =>
          p.asInstanceOf[T].value
        }

  object ClientConfiguration:
    def apply(properties: Property[?]*): ClientConfiguration = new ClientConfiguration(properties.toSeq)

  def default: ClientConfiguration = ClientConfiguration(NetworkTimeout(5.seconds))

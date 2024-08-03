package org.unibo.scooby
package utility.http

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration.DurationInt

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
   *   a response of type [[R]], depending on the [[Backend]] implementation
   */
  def send(request: Request): R

  /**
   * Sends the provided [[Request]] and automatically increments the amount of sent requests
   * @param request 
   *   request to be sent
   * @return
   *   a response of type [[R]], depending on the [[Backend]] implementation
   */
  private[http] def sendAndIncrement(request: Request): R =
    nRequests = nRequests + 1
    send(request)

/**
 * Empty trait that represents an HTTP client. To be useful, you need to mix-in [[Backend]] Traits
 */
trait HttpClient(val configuration: ClientConfiguration):
  protected var nRequests: Int = 0

  /**
   * Counter for requests made within this client
   */
  def requestCount: Int = nRequests

/**
 * Collection of useful clients types, using different backends.
 */
object Clients:
  import utility.http.backends.SttpSyncBackend

  /**
   * Simple HTTP client used for synchronous HTTP calls with the sttp library as backend
   */
  class SimpleHttpClient(configuration: ClientConfiguration = ClientConfiguration.default)
      extends HttpClient(configuration) with SttpSyncBackend

/**
 * Type alias to represent a [[Client]] with a [[Backend]]
 * @tparam R type of the [[Backend]]
 */
type Client[R] = HttpClient & Backend[R]

/**
 * Class representing the configuration of a [[Client]].
 * @param networkTimeout maximum network timeout for this client
 * @param maxRequests maximum number of requests allowed for this client
 */
case class ClientConfiguration(networkTimeout: FiniteDuration = 5.seconds,
                               maxRequests: Int = Int.MaxValue,
                               headers: Headers = Map.empty)
  
/**
 * Companion object for the [[ClientConfiguration]] that contains its `apply` method
 */
object ClientConfiguration:
  /**
   * Configuration used by default by the [[Client]]s
   * @return the default [[ClientConfiguration]]
   */
  def default: ClientConfiguration = ClientConfiguration()

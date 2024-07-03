package org.unibo.scooby
package utility.http

import utility.http.HttpStatus.INVALID

/**
 * Core of the HTTP logic. A [[HttpClient]] can mix-in a Backend to gain the corresponding, underlying implementation of
 * HTTP calls. Each Backend must specify the type of the generated HTTP responses: it can also (and in fact should) be a
 * type that does not depend on the underlying HTTP library.
 *
 * Note that while the [[Request]] type is used inside all the
 * backends, the response's one [[R]] is specific to the used Backend.
 * @tparam R type of the HTTP responses generated by the Backend
 */
trait Backend[R] extends HttpClient:
  /**
   * Sends the provided [[Request]] using the Backend implementation
   * @param request request to be sent
   * @return a response of type [[R]], depending on the Backend implementation
   */
  def send(request: Request): R

/**
 * Collection of useful backends
 */
object Backends:
  /**
   * Simple, synchronous HTTP client backend that utilizes the sttp library under the hood
   */
  trait SttpBackend extends Backend[Response]:
    import HttpMethod.*
    import sttp.client3
    import sttp.client3.{HttpClientSyncBackend, SttpBackendOptions, UriContext, asString, basicRequest}

    import scala.concurrent.duration.DurationInt

    private type SttpRequest = client3.Request[_, Any]
    private type SttpResponse = client3.Response[_]
    private type SttpURI = sttp.model.Uri

    extension(url: URL)
      private def asSttpURI: SttpURI = uri"${url.toString}"

    extension(originalRequest: Request)
      private def asSttpRequest: SttpRequest =
        val request = originalRequest.method match
          case GET => basicRequest.get(originalRequest.url.asSttpURI)
          case POST => basicRequest.post(originalRequest.url.asSttpURI)
          case PUT => basicRequest.put(originalRequest.url.asSttpURI)
          case DELETE => basicRequest.delete(originalRequest.url.asSttpURI)
        request.headers(originalRequest.headers).body(originalRequest.body.getOrElse("")).response(asString)

    extension (response: SttpResponse)
      private def asResponse: Response =
        Response(
          HttpStatus.of(response.code.code).getOrElse(INVALID),
          response.headers.map(header => (header.name, header.value)).toMap,
          response.body match
            case Right(x: String) => Some(x)
            case _ => None
        )

    private lazy val actualBackend = HttpClientSyncBackend(options = SttpBackendOptions.connectionTimeout(5.seconds))

    /**
     * Sends the [[Request]] synchronously and blocks until a [[Response]] is generated.
     * @param request request to be sent
     *  @return a response of type [[R]], depending on the Backend implementation
     */
    override def send(request: Request): Response =
      request.asSttpRequest.send(actualBackend).asResponse


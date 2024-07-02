package org.unibo.scooby
package utility.http

import utility.CucumberTestWithMockServer
import utility.http.Clients.SimpleHttpClient
import utility.http.Request.RequestBuilder

import org.scalatest.Assertions.*


object BasicUsageStepDefinitions extends CucumberTestWithMockServer:

  var request: RequestBuilder = Request.builder
  var response: Either[String, Response] = Left("Empty response")
  val httpClient: SimpleHttpClient = SimpleHttpClient()
  

  Given("""a simple {string} request"""): (requestType: String) =>
    request = request.method(HttpMethod.valueOf(requestType))

  Given("""a URL {string}"""): (url: String) =>
    request = request.at(url)


  When("""i make the HTTP call"""): () =>
    response = request.build match
      case Left(message: String) => fail("Invalid URL")
      case Right(request: Request) => request.send(httpClient)


  Then("""the returned content should be not empty"""): () =>
    response.fold(message => fail(message), _ => null)
    assert(response.isRight)
    assert(response.fold(_ => false, _.body.nonEmpty))

  Then("""it should return an error"""): () =>
    assert(response.isLeft)


  Then("""the status code should be {int} and the header content-type {string}"""):
    (statusCode: Int, contentType: String) =>
    assert(response.fold(message => fail(message), _.status.code) == statusCode)
    assert(response.fold(message => fail(message), _.headers("content-type")) === contentType)


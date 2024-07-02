package org.unibo.scooby
package utility

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object MockServer {

  sealed trait Command
  sealed case class Start(replyTo: ActorRef[Command]) extends Command
  case object Stop extends Command
  case object ServerStarted extends Command

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    implicit val system: ActorSystem[_] = context.system
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    val route: Route =
      pathEndOrSingleSlash {
        get {
          complete(
            HttpResponse(
              entity = HttpEntity(
                ContentTypes.`text/html(UTF-8)`,
                """<html>
                  |<head><title>Simple Akka HTTP Server</title></head>
                  |<body><a href="https://www.fortest.it">Test Link</a></body>
                  |</html>""".stripMargin
              ),
              status = StatusCodes.OK
            )
          )
        }
      }

    val json: Route =
      path("json") {
        post {
          complete(
            HttpResponse(
              entity = HttpEntity(
                ContentTypes.`application/json`,
                "{}"
              ),
              status = StatusCodes.OK
            )
          )
        }
      }

    val notFound: Route =  extractRequest { request =>
        complete(
          HttpResponse(
            entity = HttpEntity(
              ContentTypes.`text/html(UTF-8)`,
              "<html><h1>Not Found</h1></html>"
            ),
            status = StatusCodes.NotFound
          )
        )
      }


    def running(bindingFuture: Future[Http.ServerBinding]): Behavior[Command] =
      Behaviors.receiveMessage {
        case Start(_) =>
          context.log.info("Server is already running")
          Behaviors.same

        case Stop =>
          val log = context.log
          bindingFuture.flatMap(_.unbind()).onComplete {
            case Success(_) =>
              log.info("Server stopped")
              system.terminate()
            case Failure(ex) =>
              log.error("Failed to unbind server", ex)
              system.terminate()
          }
          Behaviors.stopped
      }

    Behaviors.receiveMessage {
      case Start(replyTo) =>
        val bindingFuture = Http().newServerAt("localhost", 8080).bind(route ~ json ~ notFound)
        val log = context.log
        bindingFuture.onComplete {
          case Success(_) =>
            log.info("Server started at http://localhost:8080/")
            replyTo ! ServerStarted  // Invia direttamente il ServerStarted
          case Failure(ex) =>
            log.error("Failed to bind HTTP endpoint, terminating system", ex)
            system.terminate()
        }
        running(bindingFuture)

      case Stop =>
        context.log.info("Server is not running")
        Behaviors.same
    }
  }
}
package com.atm

import cats.effect._
import cats.syntax.all._
import com.atm.config.Database
import com.atm.repository._
import com.atm.service._
import com.atm.api._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import org.http4s.HttpRoutes
import com.comcast.ip4s._

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    Database.transactor[IO].use { xa =>
      val usuarioRepo = new UsuarioRepo
      val cuentaRepo  = new CuentaRepo
      val transRepo   = new TransaccionRepo
      val logRepo     = new LogRepo

      val authService = new AuthService(usuarioRepo, logRepo, cuentaRepo, xa)
      val atmService  = new ATMService(cuentaRepo, transRepo, logRepo, xa)

      val api = new ATMApi(authService, atmService)

      // Middleware CORS
      val cors = CORS.policy.withAllowOriginAll

      // Rutas con captura de errores
      val httpRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] { case req =>
        api.routes.run(req).value.flatMap {
          case Some(resp) => IO.pure(resp)  // si todo va bien
          case None       => IO.pure(org.http4s.Response[IO](org.http4s.Status.NotFound))
        }.handleErrorWith(ErrorHandler.handleThrowable _) // captura errores globales
      }

      val httpApp = cors(Router("/api" -> httpRoutes).orNotFound)

      EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(httpApp)
        .build
        .useForever
        .as(ExitCode.Success)
    }
}

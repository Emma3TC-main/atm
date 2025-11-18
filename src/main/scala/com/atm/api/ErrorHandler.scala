package com.atm.api

import cats.effect.IO
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.circe._
import io.circe.Json
import com.atm.domain.DomainError

object ErrorHandler {

  // Convierte DomainError a Response con JSON
  def fromDomainError(err: DomainError): IO[Response[IO]] = {
    val status = err match {
      case DomainError.PasswordIncorrecta  => Status.Unauthorized
      case DomainError.UsuarioNoEncontrado => Status.NotFound
      case DomainError.CuentaNoEncontrada  => Status.NotFound
      case DomainError.SaldoInsuficiente   => Status.BadRequest
      case DomainError.MontoInvalido       => Status.BadRequest
      case DomainError.EmailInvalido       => Status.BadRequest
      case DomainError.NombreInvalido      => Status.BadRequest
      case DomainError.UsuarioBloqueado    => Status.Locked
      case DomainError.DbError(_)          => Status.InternalServerError
      case DomainError.Other(_)            => Status.InternalServerError
    }

    val json: Json = Json.obj(
      "error" -> Json.fromString(err.msg)
    )

    IO.pure(Response[IO](status).withEntity(json))
  }

  // Handler global para cualquier Throwable
  def handleThrowable(e: Throwable): IO[Response[IO]] = e match {
    case de: DomainError => fromDomainError(de)
    case t: Throwable =>
      val json: Json = Json.obj(
        "error" -> Json.fromString("Error interno del servidor"),
        "detalle" -> Json.fromString(Option(t.getMessage).getOrElse("Unknown error"))
      )
      IO.pure(Response[IO](Status.InternalServerError).withEntity(json))
  }
}


package com.atm.api

import cats.effect.IO
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.circe._
import io.circe.generic.auto._
import io.circe.syntax._

import com.atm.service._
import com.atm.model._
import com.atm.domain.DomainError

class ATMApi(auth: AuthService, atm: ATMService) {

  // Decoders para JSON
  implicit val decoderDeposito: EntityDecoder[IO, DepositoRequest] = jsonOf[IO, DepositoRequest]
  implicit val decoderRetiro: EntityDecoder[IO, RetiroRequest] = jsonOf[IO, RetiroRequest]
  implicit val decoderRegistro: EntityDecoder[IO, RegistroRequest] = jsonOf[IO, RegistroRequest]

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    // -----------------------
    // LOGIN
    // -----------------------
    case POST -> Root / "login" :? EmailQueryParam(email) +& PassQueryParam(pass) =>
      auth.login(email, pass).flatMap {
        case Right(usuario) =>
          Ok(UserResponse(usuario.id, usuario.nombre).asJson)
        case Left(err) => err match {
          case DomainError.UsuarioBloqueado | DomainError.UsuarioNoEncontrado | DomainError.PasswordIncorrecta =>
            Forbidden(Map("error" -> err.msg).asJson)
          case _ => InternalServerError(Map("error" -> err.msg).asJson)
        }
      }

    // -----------------------
    // REGISTRO
    // -----------------------
    case req @ POST -> Root / "registro" =>
      req.as[RegistroRequest].flatMap { data =>
        auth.registrar(data).flatMap {
          case Right(id) => Ok(IdResponse(id).asJson)
          case Left(err)  => BadRequest(Map("error" -> err.msg).asJson)
        }
      }

    // -----------------------
    // SALDO
    // -----------------------
    case GET -> Root / "saldo" / IntVar(idUsuario) =>
      atm.consultarSaldo(idUsuario).flatMap {
        case Right(saldo) => Ok(Map("saldo" -> saldo).asJson)
        case Left(err)    => BadRequest(Map("error" -> err.msg).asJson)
      }

    // -----------------------
    // LISTAR CUENTAS
    // -----------------------
    case GET -> Root / "cuentas" / IntVar(idUsuario) =>
      atm.listarCuentas(idUsuario).flatMap(cuentas => Ok(cuentas.asJson))

    // -----------------------
    // DEPOSITAR
    // -----------------------
    case req @ POST -> Root / "depositar" =>
      req.as[DepositoRequest].flatMap { data =>
        atm.depositar(data.idCuenta, data.monto).flatMap {
          case Right(_)  => Ok(MessageResponse("Depósito exitoso").asJson)
          case Left(err) => BadRequest(Map("error" -> err.msg).asJson)
        }
      }

    // -----------------------
    // RETIRAR
    // -----------------------
    case req @ POST -> Root / "retirar" =>
      req.as[RetiroRequest].flatMap { data =>
        atm.retirar(data.idCuenta, data.monto).flatMap {
          case Right(_)  => Ok(MessageResponse("Retiro exitoso").asJson)
          case Left(err) => BadRequest(Map("error" -> err.msg).asJson)
        }
      }

    // -----------------------
    // TRANSACCIONES
    // -----------------------
    case GET -> Root / "transacciones" / IntVar(idCuenta) =>
      atm.listarTransacciones(idCuenta).flatMap(trxs => Ok(trxs.asJson))



  }


  // Query param matchers
  object EmailQueryParam extends QueryParamDecoderMatcher[String]("email")
  object PassQueryParam extends QueryParamDecoderMatcher[String]("password")
}

package com.atm.service

import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._
import java.time.LocalDateTime
import com.atm.repository._
import com.atm.model._
import com.atm.domain._
import doobie.util.transactor.Transactor

class ATMService(
                  cuentaRepo: CuentaRepo,
                  transRepo: TransaccionRepo,
                  logRepo: LogRepo,
                  xa: Transactor[IO]
                ) {

  import DomainError._

  // -----------------------
  // DEPOSITAR
  // -----------------------

  def depositar(idCuenta: Int, monto: BigDecimal): IO[Either[DomainError, Unit]] =
    Domain.validarDeposito(monto) match {
      case Left(err) => IO.pure(Left(err))

      case Right(montoValid) =>
        val program: ConnectionIO[Either[DomainError, Unit]] =
          for {
            maybeCuenta <- cuentaRepo.findByIdForUpdate(idCuenta)

            // convertimos aquí fuera del for
            result <- maybeCuenta match {

              case None =>
                // devolvemos ConnectionIO[Either[DomainError, Unit]]
                (Left(CuentaNoEncontrada): Either[DomainError, Unit]).pure[ConnectionIO]

              case Some(c) =>
                val nuevoSaldo = Domain.aplicarDeposito(c.saldo, montoValid)
                for {
                  _ <- cuentaRepo.updateSaldo(idCuenta, nuevoSaldo)
                  _ <- transRepo.crear(
                    Transaccion(0, "DEPOSITO", montoValid, LocalDateTime.now(), "EXITOSO", idCuenta)
                  )
                  _ <- logRepo.logAcceso(Some(idCuenta), s"EXITOSO DEPOSITO $montoValid")
                } yield (Right(()): Either[DomainError, Unit])
            }

          } yield result

        program
          .handleErrorWith { e =>
            logRepo.logError(s"ERROR DEPOSITO: ${e.getMessage}", Some(idCuenta)) *>
              (Left(DbError(e.getMessage)): Either[DomainError, Unit]).pure[ConnectionIO]
          }
          .transact(xa)
    }

  // -----------------------
  // RETIRAR
  // -----------------------
  def retirar(idCuenta: Int, monto: BigDecimal): IO[Either[DomainError, Unit]] = {
    val program: ConnectionIO[Either[DomainError, Unit]] =
      for {
        maybeCuenta <- cuentaRepo.findByIdForUpdate(idCuenta)

        result <- maybeCuenta match {
          case None =>
            (Left(CuentaNoEncontrada): Either[DomainError, Unit]).pure[ConnectionIO]

          case Some(c) =>
            Domain.validarRetiro(c.saldo, monto) match {
              case Left(err) => (Left(err): Either[DomainError, Unit]).pure[ConnectionIO]

              case Right(montoValid) =>
                val nuevoSaldo = Domain.aplicarRetiro(c.saldo, montoValid)
                for {
                  _ <- cuentaRepo.updateSaldo(idCuenta, nuevoSaldo)
                  _ <- transRepo.crear(
                    Transaccion(0, "RETIRO", montoValid, LocalDateTime.now(), "EXITOSO", idCuenta)
                  )
                  _ <- logRepo.logAcceso(Some(idCuenta), s"EXITOSO RETIRO $montoValid")
                } yield (Right(()): Either[DomainError, Unit])
            }
        }

      } yield result

    program
      .handleErrorWith { e =>
        logRepo.logError(s"ERROR RETIRO: ${e.getMessage}", Some(idCuenta)) *>
          (Left(DbError(e.getMessage)): Either[DomainError, Unit]).pure[ConnectionIO]
      }
      .transact(xa)
  }

  // -----------------------
  // TRANSFERENCIA
  // -----------------------
  def transferir(idOrigen: Int, idDestino: Int, monto: BigDecimal): IO[Either[DomainError, Unit]] =
    Domain.validarDeposito(monto) match {

      case Left(err) => IO.pure(Left(err))

      case Right(montoValid) =>
        val program: ConnectionIO[Either[DomainError, Unit]] =
          for {
            oOpt <- cuentaRepo.findByIdForUpdate(idOrigen)
            dOpt <- cuentaRepo.findByIdForUpdate(idDestino)

            res <- (oOpt, dOpt) match {

              case (None, _) =>
                (Left(CuentaNoEncontrada): Either[DomainError, Unit]).pure[ConnectionIO]

              case (_, None) =>
                (Left(CuentaNoEncontrada): Either[DomainError, Unit]).pure[ConnectionIO]

              case (Some(origen), Some(destino)) =>
                if (origen.saldo < montoValid)
                  (Left(SaldoInsuficiente): Either[DomainError, Unit]).pure[ConnectionIO]
                else {
                  val nuevoO = Domain.aplicarRetiro(origen.saldo, montoValid)
                  val nuevoD = Domain.aplicarDeposito(destino.saldo, montoValid)
                  val now = LocalDateTime.now()

                  for {
                    _ <- cuentaRepo.updateSaldo(idOrigen, nuevoO)
                    _ <- cuentaRepo.updateSaldo(idDestino, nuevoD)
                    _ <- transRepo.crear(Transaccion(0, "TRANSFERENCIA", montoValid, now, "EXITOSO", idOrigen))
                    _ <- transRepo.crear(Transaccion(0, "TRANSFERENCIA", montoValid, now, "EXITOSO", idDestino))
                    _ <- logRepo.logAcceso(Some(idOrigen), s"EXITOSO TRANSFERENCIA $montoValid de $idOrigen a $idDestino")
                  } yield (Right(()): Either[DomainError, Unit])
                }
            }

          } yield res

        program
          .handleErrorWith { e =>
            logRepo.logError(s"ERROR TRANSFERENCIA: ${e.getMessage}", Some(idOrigen)) *>
              (Left(DbError(e.getMessage)): Either[DomainError, Unit]).pure[ConnectionIO]
          }
          .transact(xa)
    }

  // -----------------------
  // CONSULTAS
  // -----------------------
  def consultarSaldo(idUsuario: Int): IO[Either[DomainError, BigDecimal]] =
    cuentaRepo.findByUser(idUsuario).transact(xa).map {
      case h :: _ => Right(h.saldo)
      case Nil    => Left(CuentaNoEncontrada)
    }

  def listarCuentas(idUsuario: Int): IO[List[Cuenta]] =
    cuentaRepo.findByUser(idUsuario).transact(xa)

  def listarTransacciones(idCuenta: Int): IO[List[Transaccion]] =
    transRepo.listarPorCuenta(idCuenta).transact(xa)
}

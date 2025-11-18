package com.atm.service

import cats.effect.IO
import cats.syntax.applicative._
import cats.syntax.functor._
import doobie.implicits._
import doobie.util.transactor.Transactor
import com.atm.repository.LogRepo

class AuditService(logRepo: LogRepo, xa: Transactor[IO]) {

  /** Registra un error en la tabla errorlog. idUsuario opcional por si es sistema */
  def registrarError(descripcion: String, idUsuario: Option[Int] = None): IO[Unit] =
    logRepo.logError(descripcion, idUsuario).transact(xa).attempt.void

  /** Registra un acceso en la tabla accesolog. idUsuario opcional para eventos anónimos */
  def registrarAcceso(resultado: String, idUsuario: Option[Int] = None): IO[Unit] =
    logRepo.logAcceso(idUsuario, resultado).transact(xa).attempt.void
}

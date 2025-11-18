package com.atm.config

import cats.effect._
import doobie._
import doobie.hikari._
import doobie.util.ExecutionContexts

object Database {

  // Tomar variables de entorno
  private val driver   = sys.env.getOrElse("DB_DRIVER", "org.postgresql.Driver")
  private val url      = sys.env.getOrElse("DB_URL", "")
  private val user     = sys.env.getOrElse("DB_USER", "")
  private val password = sys.env.getOrElse("DB_PASSWORD", "")

  def transactor[F[_]: Async]: Resource[F, HikariTransactor[F]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool(32)
      xa <- HikariTransactor.newHikariTransactor[F](
        driver,
        url,
        user,
        password,
        ce
      )
    } yield xa
}

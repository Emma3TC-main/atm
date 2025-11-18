package com.atm.config

import cats.effect._
import doobie._
import doobie.hikari._
import doobie.util.ExecutionContexts

object Database {

  // Tomar variables de entorno, con valores genéricos seguros para Git
  private val driver   = sys.env.getOrElse("DB_DRIVER", "org.postgresql.Driver")
  private val host     = sys.env.getOrElse("DB_HOST", "localhost")
  private val port     = sys.env.getOrElse("DB_PORT", "5432")
  private val dbname   = sys.env.getOrElse("DB_NAME", "atm")
  private val user     = sys.env.getOrElse("DB_USER", "user")
  private val password = sys.env.getOrElse("DB_PASSWORD", "pass")
  private val sslmode  = sys.env.getOrElse("DB_SSLMODE", "disable")

  // Construir la URL JDBC usando las variables de entorno
  private val url = s"jdbc:postgresql://$host:$port/$dbname?sslmode=$sslmode"

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

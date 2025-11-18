package com.atm.config

import cats.effect._
import doobie._
import doobie.hikari._
import doobie.util.ExecutionContexts
import com.typesafe.config.ConfigFactory

object Database {
  private val conf = ConfigFactory.load().getConfig("db")

  def transactor[F[_]: Async]: Resource[F, HikariTransactor[F]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool(32)
      xa <- HikariTransactor.newHikariTransactor[F](
        conf.getString("driver"),
        conf.getString("url"),
        conf.getString("user"),
        conf.getString("password"),
        ce
      )
    } yield xa
}

package com.atm.repository

import doobie._
import doobie.implicits._
import doobie.postgres.implicits._ // si usas tipos postgres; no es obligatorio aquí
import java.time.LocalDateTime

class AuditRepo {

  def insertErrorLog(descripcion: String, idUsuario: Option[Int]): ConnectionIO[Int] =
    sql"""
      INSERT INTO errorlog (fecha, descripcion, id_usuario)
      VALUES (CURRENT_TIMESTAMP, $descripcion, $idUsuario)
    """.update.run

  def insertAccesoLog(resultado: String, idUsuario: Option[Int]): ConnectionIO[Int] =
    sql"""
      INSERT INTO accesolog (fecha, resultado, id_usuario)
      VALUES (CURRENT_TIMESTAMP, $resultado, $idUsuario)
    """.update.run
}

package com.atm.repository

import doobie._
import doobie.implicits._
import cats.effect.IO

class LogRepo {

  // Registrar errores
  def logError(descripcion: String, idUsuario: Option[Int]): ConnectionIO[Int] =
    sql"""
      INSERT INTO errorlog (fecha, descripcion, id_usuario)
      VALUES (CURRENT_TIMESTAMP, $descripcion, $idUsuario)
    """.update.run

  // Registrar accesos exitosos o transacciones
  def logAcceso(idUsuario: Option[Int], resultado: String): ConnectionIO[Int] =
    sql"""
      INSERT INTO accesolog (fecha, resultado, id_usuario)
      VALUES (CURRENT_TIMESTAMP, $resultado, $idUsuario)
    """.update.run
}

package com.atm.repository

import doobie._
import doobie.implicits._
import doobie.postgres.implicits._   // <- importante: Meta para java.time
import com.atm.model.Transaccion
import java.time.LocalDateTime

class TransaccionRepo {

  def crear(t: Transaccion): ConnectionIO[Int] =
    sql"""
      INSERT INTO transaccion(tipo, monto, fecha, estado, id_cuenta)
      VALUES (${t.tipo}, ${t.monto}, ${t.fecha}, ${t.estado}, ${t.idCuenta})
    """.update.run

  def listarPorCuenta(idCuenta: Int): ConnectionIO[List[Transaccion]] =
    sql"""
      SELECT id_transaccion, tipo, monto, fecha, estado, id_cuenta
      FROM transaccion WHERE id_cuenta = $idCuenta
      ORDER BY fecha DESC
    """.query[Transaccion].to[List]
}

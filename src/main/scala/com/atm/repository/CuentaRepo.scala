package com.atm.repository

import doobie._
import doobie.implicits._
import com.atm.model.Cuenta

class CuentaRepo {

  def findByUser(idUsuario: Int): ConnectionIO[List[Cuenta]] =
    sql"""
      SELECT id_cuenta, numero_cuenta, saldo, tipo, id_usuario
      FROM cuenta WHERE id_usuario = $idUsuario
    """.query[Cuenta].to[List]

  def findById(idCuenta: Int): ConnectionIO[Option[Cuenta]] =
    sql"""
      SELECT id_cuenta, numero_cuenta, saldo, tipo, id_usuario
      FROM cuenta WHERE id_cuenta = $idCuenta
    """.query[Cuenta].option

  def findByIdForUpdate(idCuenta: Int): ConnectionIO[Option[Cuenta]] =
    sql"""
      SELECT id_cuenta, numero_cuenta, saldo, tipo, id_usuario
      FROM cuenta WHERE id_cuenta = $idCuenta FOR UPDATE
    """.query[Cuenta].option

  def updateSaldo(idCuenta: Int, nuevoSaldo: BigDecimal): ConnectionIO[Int] =
    sql"""
      UPDATE cuenta SET saldo = $nuevoSaldo WHERE id_cuenta = $idCuenta
    """.update.run

  def insertCuenta(c: Cuenta): ConnectionIO[Int] =
    sql"""
    INSERT INTO cuenta (numero_cuenta, saldo, tipo, id_usuario)
    VALUES (${c.numeroCuenta}, ${c.saldo}, ${c.tipo}, ${c.idUsuario})
  """
      .update
      .withUniqueGeneratedKeys[Int]("id_cuenta")

}

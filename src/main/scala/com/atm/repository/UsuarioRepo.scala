package com.atm.repository

import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import com.atm.model.Usuario
import java.time.LocalDateTime

class UsuarioRepo {
  def findByEmail(email: String): ConnectionIO[Option[Usuario]] =
    sql"""
      SELECT id_usuario, nombre, email, password_hash, estado, intentos_fallidos, rol
      FROM usuario WHERE email = $email
    """.query[Usuario].option

  def insert(usuario: Usuario): ConnectionIO[Int] =
    sql"""
      INSERT INTO usuario(nombre, email, password_hash, estado, intentos_fallidos, rol)
      VALUES (${usuario.nombre}, ${usuario.email}, ${usuario.passwordHash}, ${usuario.estado},
              ${usuario.intentosFallidos}, ${usuario.rol})
    """.update.withGeneratedKeys[Int]("id_usuario").compile.lastOrError

  def ultimoIntentoFallido(idUsuario: Int): ConnectionIO[Option[LocalDateTime]] =
    sql"""
      SELECT fecha
      FROM accesolog
      WHERE id_usuario = $idUsuario
        AND resultado LIKE 'FALLIDO%'
      ORDER BY fecha DESC
      LIMIT 1
     """.query[LocalDateTime].option

  def aumentarIntentos(idUsuario: Int): ConnectionIO[Int] =
    sql"""
      UPDATE usuario
      SET intentos_fallidos = intentos_fallidos + 1
      WHERE id_usuario = $idUsuario
     """.update.run

  def resetIntentos(idUsuario: Int): ConnectionIO[Int] =
    sql"""
      UPDATE usuario
      SET intentos_fallidos = 0
      WHERE id_usuario = $idUsuario
     """.update.run

  def bloquearUsuario(idUsuario: Int): ConnectionIO[Int] =
    sql"""
      UPDATE usuario
      SET estado = 'BLOQUEADO'
      WHERE id_usuario = $idUsuario
     """.update.run

  def activarUsuario(idUsuario: Int): ConnectionIO[Int] =
    sql"""
      UPDATE usuario
      SET estado = 'ACTIVO'
      WHERE id_usuario = $idUsuario
     """.update.run
}


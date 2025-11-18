package com.atm.service

import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._
import org.mindrot.jbcrypt.BCrypt
import java.time.{LocalDateTime, Duration}

import com.atm.repository._
import com.atm.model._
import com.atm.domain._
import com.atm.api.RegistroRequest

class AuthService(
                   usuarioRepo: UsuarioRepo,
                   logRepo: LogRepo,
                   cuentaRepo: CuentaRepo,
                   xa: Transactor[IO]
                 ) {

  private val MAX_INTENTOS = 3
  private val BLOQUEO_HORAS = 2

  // -----------------------
  // LOGIN
  // -----------------------
  def login(email: String, password: String): IO[Either[DomainError, Usuario]] = {
    val program: ConnectionIO[Either[DomainError, Usuario]] =
      for {
        maybeUsuario <- usuarioRepo.findByEmail(email)
        res <- maybeUsuario match {
          case None =>
            logRepo.logError(s"Login fallido: usuario no existe: $email", None) *>
              Either.left[DomainError, Usuario](DomainError.UsuarioNoEncontrado).pure[ConnectionIO]

          case Some(u) =>
            if (u.estado == "BLOQUEADO")
              manejarUsuarioBloqueado(u, password)
            else
              manejarLoginActivo(u, password)
        }
      } yield res

    program.transact(xa)
  }

  // Manejo de login para usuario activo
  private def manejarLoginActivo(u: Usuario, password: String): ConnectionIO[Either[DomainError, Usuario]] = {
    if (BCrypt.checkpw(password, u.passwordHash)) {
      for {
        _ <- usuarioRepo.resetIntentos(u.id)
        _ <- logRepo.logAcceso(Some(u.id), "LOGIN_OK")
      } yield Either.right[DomainError, Usuario](u) // devolvemos todo el Usuario
    } else {
      manejarPasswordIncorrecto(u)
    }
  }

  // Manejo de login para usuario bloqueado
  private def manejarUsuarioBloqueado(u: Usuario, password: String): ConnectionIO[Either[DomainError, Usuario]] = {
    for {
      ultima <- usuarioRepo.ultimoIntentoFallido(u.id)
      res <- ultima match {
        case None =>
          Either.left[DomainError, Usuario](DomainError.UsuarioBloqueado).pure[ConnectionIO]

        case Some(fechaUltimo) =>
          val horasTranscurridas = Duration.between(fechaUltimo, LocalDateTime.now()).toHours
          if (horasTranscurridas < BLOQUEO_HORAS)
            logRepo.logAcceso(Some(u.id), "BLOQUEADO_INTENTO") *>
              Either.left[DomainError, Usuario](DomainError.UsuarioBloqueado).pure[ConnectionIO]
          else
            for {
              _ <- usuarioRepo.resetIntentos(u.id)
              _ <- usuarioRepo.activarUsuario(u.id)
              r <- manejarLoginActivo(u.copy(intentosFallidos = 0), password)
            } yield r
      }
    } yield res
  }

  // Manejo de password incorrecta
  private def manejarPasswordIncorrecto(u: Usuario): ConnectionIO[Either[DomainError, Usuario]] = {
    val intentosActualizados = u.intentosFallidos + 1
    val bloquear = intentosActualizados >= MAX_INTENTOS

    for {
      _ <- usuarioRepo.aumentarIntentos(u.id)
      _ <- logRepo.logError(s"Password incorrecto para usuario: ${u.email}", Some(u.id))
      _ <- logRepo.logAcceso(Some(u.id), "LOGIN_FALLIDO")
      _ <- if (bloquear) usuarioRepo.bloquearUsuario(u.id).void else ().pure[ConnectionIO]
    } yield Either.left[DomainError, Usuario](DomainError.PasswordIncorrecta)
  }

  // -----------------------
  // REGISTRO
  // -----------------------
  def registrar(req: RegistroRequest): IO[Either[DomainError, Int]] = {
    Domain.validarRegistro(req.nombre, req.email, req.password) match {

      case Left(err) =>
        val program =
          logRepo.logError(s"Registro inválido: ${err.msg}", None) *>
            Either.left[DomainError, Int](err).pure[ConnectionIO]

        program.transact(xa)

      case Right(_) =>
        val usuario = Usuario(
          id = 0,
          nombre = req.nombre,
          email = req.email,
          passwordHash = BCrypt.hashpw(req.password, BCrypt.gensalt(12)),
          estado = "ACTIVO",
          intentosFallidos = 0,
          rol = "CLIENTE"
        )

        val program: ConnectionIO[Either[DomainError, Int]] =
          for {
            // 1️⃣ Insertar usuario
            idUsuario <- usuarioRepo.insert(usuario)

            // 2️⃣ Generar número de cuenta
            numeroCuenta = s"ACC-$idUsuario-${System.currentTimeMillis().toString.takeRight(4)}"

            // 3️⃣ Insertar cuenta inicial
            _ <- cuentaRepo.insertCuenta(
              Cuenta(
                id = 0,
                numeroCuenta = numeroCuenta,
                saldo = BigDecimal(0),
                tipo = "AHORROS",
                idUsuario = idUsuario
              )
            )

            // 4️⃣ Registrar log
            _ <- logRepo.logAcceso(Some(idUsuario), s"REGISTRO_OK cuenta=$numeroCuenta")

          } yield Either.right[DomainError, Int](idUsuario) // <-- especificando tipo correcto

        program
          .handleErrorWith { e =>
            logRepo.logError(s"Error registro usuario: ${e.getMessage}", None) *>
              Either.left[DomainError, Int](DomainError.DbError(e.getMessage)).pure[ConnectionIO]
          }
          .transact(xa)
    }
  }
}

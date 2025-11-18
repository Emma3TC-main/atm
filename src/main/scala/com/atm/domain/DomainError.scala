package com.atm.domain

sealed trait DomainError {
  def msg: String
}

object DomainError {
  final case object CuentaNoEncontrada extends DomainError { val msg = "Cuenta no encontrada" }
  final case object UsuarioNoEncontrado extends DomainError { val msg = "Usuario no encontrado" }
  final case object SaldoInsuficiente extends DomainError { val msg = "Saldo insuficiente" }
  final case object MontoInvalido extends DomainError { val msg = "Monto inválido" }
  final case object EmailInvalido extends DomainError { val msg = "Email inválido" }
  final case object NombreInvalido extends DomainError { val msg = "Nombre inválido" }
  final case object PasswordIncorrecta extends DomainError { val msg = "Contraseña incorrecta" }

  final case class DbError(cause: String) extends DomainError { val msg = s"Error BD: $cause" }
  final case class Other(override val msg: String) extends DomainError
  final case object UsuarioBloqueado extends DomainError { val msg = "Usuario bloqueado" }
}

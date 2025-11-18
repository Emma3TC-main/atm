package com.atm.domain

import com.atm.model.Usuario
import java.time.LocalDateTime

object Domain {

  import DomainError._

  def validarRegistro(nombre: String, email: String, password: String): Either[DomainError, Usuario] =
    if (nombre.trim.isEmpty) Left(NombreInvalido)
    else if (!email.contains("@")) Left(EmailInvalido)
    else if (password.length < 6) Left(MontoInvalido /* or new error for password */)
    else Right(Usuario(0, nombre.trim, email.trim, password, "ACTIVO", 0, "CLIENTE"))

  def validarDeposito(monto: BigDecimal): Either[DomainError, BigDecimal] =
    if (monto <= 0) Left(MontoInvalido) else Right(monto.setScale(2, BigDecimal.RoundingMode.HALF_UP))

  def validarRetiro(saldo: BigDecimal, monto: BigDecimal): Either[DomainError, BigDecimal] =
    if (monto <= 0) Left(MontoInvalido)
    else if (saldo < monto) Left(SaldoInsuficiente)
    else Right(monto.setScale(2, BigDecimal.RoundingMode.HALF_UP))

  def aplicarDeposito(saldo: BigDecimal, monto: BigDecimal): BigDecimal =
    (saldo + monto).setScale(2, BigDecimal.RoundingMode.HALF_UP)

  def aplicarRetiro(saldo: BigDecimal, monto: BigDecimal): BigDecimal =
    (saldo - monto).setScale(2, BigDecimal.RoundingMode.HALF_UP)
}

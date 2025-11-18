package com.atm.api

// DTOs para la API (requests)
case class DepositoRequest(idCuenta: Int, monto: BigDecimal)
case class RetiroRequest(idCuenta: Int, monto: BigDecimal)
case class RegistroRequest(nombre: String, email: String, password: String)

// Responses mínimo (puedes ampliar)
case class MessageResponse(mensaje: String)
case class IdResponse(idUsuario: Int)
case class UserResponse(id: Int, nombre: String)

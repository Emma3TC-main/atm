package com.atm.model

import java.time.LocalDateTime

case class Transaccion(id: Int,
                       tipo: String,
                       monto: BigDecimal,
                       fecha: LocalDateTime,
                       estado: String,
                       idCuenta: Int
                      )
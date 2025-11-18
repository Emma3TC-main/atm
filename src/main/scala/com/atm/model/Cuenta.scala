package com.atm.model


case class Cuenta(id: Int,
                  numeroCuenta: String,
                  saldo: BigDecimal,
                  tipo: String,
                  idUsuario: Int
                 )
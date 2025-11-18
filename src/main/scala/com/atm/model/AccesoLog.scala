package com.atm.model

import java.time.LocalDateTime

case class AccesoLog(
                      id: Int,
                      fecha: LocalDateTime,
                      resultado: String,
                      idUsuario: Option[Int]
                    )

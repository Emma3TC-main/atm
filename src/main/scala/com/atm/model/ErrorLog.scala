package com.atm.model

import java.time.LocalDateTime

case class ErrorLog(
                     id: Int,
                     fecha: LocalDateTime,
                     descripcion: String,
                     idUsuario: Option[Int]
                   )

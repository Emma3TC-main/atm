package com.atm.model

case class Usuario(id: Int,
                   nombre: String,
                   email: String,
                   passwordHash: String,
                   estado: String,
                   intentosFallidos:Int,
                   rol: String
                  )

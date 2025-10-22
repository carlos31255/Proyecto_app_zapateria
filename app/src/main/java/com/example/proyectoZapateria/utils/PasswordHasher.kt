package com.example.proyectoZapateria.utils

import org.mindrot.jbcrypt.BCrypt

object PasswordHasher {
    fun hashPassword(pass: String): String{
        return BCrypt.hashpw(pass, BCrypt.gensalt())
    }
    fun checkPassword(pass: String, hashedPass: String): Boolean{
        return BCrypt.checkpw(pass, hashedPass)
    }
}
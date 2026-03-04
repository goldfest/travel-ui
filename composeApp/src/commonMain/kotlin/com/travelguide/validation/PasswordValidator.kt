package com.travelguide.validation

object PasswordValidator {
    fun isStrong(password: String): Boolean {
        val p = password.trim()
        if (p.length < 8) return false

        var hasLetter = false
        var hasDigit = false
        for (ch in p) {
            if (ch.isLetter()) hasLetter = true
            else if (ch.isDigit()) hasDigit = true
            if (hasLetter && hasDigit) return true
        }
        return false
    }

    fun error(password: String): String? {
        return if (isStrong(password)) null
        else "Минимум 8 символов, должны быть буквы и цифры"
    }
}
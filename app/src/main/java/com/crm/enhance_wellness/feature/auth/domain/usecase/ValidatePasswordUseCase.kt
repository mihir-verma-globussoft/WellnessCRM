package com.crm.enhance_wellness.feature.auth.domain.usecase

import javax.inject.Inject

data class PasswordRule(val label: String, val met: Boolean)

class ValidatePasswordUseCase @Inject constructor() {
    // ponytail: rules assumed (backend rules unknown) — align with the server's policy
    operator fun invoke(password: String): List<PasswordRule> = listOf(
        PasswordRule("At least 8 characters", password.length >= 8),
        PasswordRule("An uppercase letter", password.any(Char::isUpperCase)),
        PasswordRule("A lowercase letter", password.any(Char::isLowerCase)),
        PasswordRule("A number", password.any(Char::isDigit)),
        PasswordRule("A special character", password.any { !it.isLetterOrDigit() }),
    )
}

package com.quimia.android.presentation.screens.auth

data class GoogleOnboarding(
    val uid: String, val email: String, val name: String, val birthDate: String,
    val photoUrl: String? = null, val addressStep: Boolean = false,
) {
    val valid: Boolean get() = uid.isNotBlank() && authEmailError(email) == null &&
        authNameError(name) == null && authBirthDateError(birthDate) == null
    val destination: AuthDestination get() = if (addressStep) AuthDestination.GoogleAddress else AuthDestination.GoogleDetails
    fun continueToAddress(): GoogleOnboarding? = takeIf { valid }?.copy(name = name.trim(), addressStep = true)
    fun backToDetails() = copy(addressStep = false)
}

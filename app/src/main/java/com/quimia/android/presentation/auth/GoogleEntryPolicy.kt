package com.quimia.android.presentation.auth

import com.quimia.android.data.auth.UserProfile
import com.quimia.android.presentation.components.BirthDate
import com.quimia.android.presentation.screens.auth.AuthDestination

/** Address is optional: not having one must never restart an already completed registration. */
internal fun googleEntryDestination(profile: UserProfile, completed: Boolean, pending: Boolean,
    checkpointed: Boolean = true): AuthDestination? {
    if (completed) return null
    val hasBirthDate = BirthDate.parse(displayBirthDate(profile.dataNasc)) != null
    // Old versions marked every Google login pending, including already completed accounts.
    if (pending && checkpointed) return if (hasBirthDate) AuthDestination.GoogleAddress else AuthDestination.GoogleDetails
    return if (hasBirthDate) null else AuthDestination.GoogleDetails
}

package com.quimia.android.presentation.auth

import com.quimia.android.data.auth.UserProfile
import com.quimia.android.presentation.components.BirthDate

/** Address can be skipped; a persisted name and birth date complete the initial profile. */
internal fun needsInitialProfile(profile: UserProfile): Boolean =
    profile.nome.isBlank() || BirthDate.parse(displayBirthDate(profile.dataNasc)) == null

internal fun shouldResumeInitialProfile(profile: UserProfile, locallyPending: Boolean): Boolean =
    locallyPending && needsInitialProfile(profile)

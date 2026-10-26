package com.quimia.android.presentation.screens.profile

import androidx.compose.ui.graphics.luminance
import org.junit.Assert.*
import org.junit.Test
import theme.HighContrastTokens
import theme.LightTokens

class ProfileModelsTest {
    @Test fun preservesOriginalDefaults() {
        assertEquals(ProfileTheme.Light, ProfilePreferences().theme)
        assertEquals(1f, ProfilePreferences().textScale)
        assertFalse(ProfilePreferences().dyslexicFont)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFF7F6F5), LightTokens.surfaceBackground)
    }

    @Test fun systemThemeFollowsDeviceOnlyWhenSelected() {
        assertFalse(ProfileTheme.System.usesDarkColors(false))
        assertTrue(ProfileTheme.System.usesDarkColors(true))
        assertFalse(ProfileTheme.Light.usesDarkColors(true))
        assertTrue(ProfileTheme.Dark.usesDarkColors(false))
        assertTrue(ProfileTheme.HighContrast.usesDarkColors(false))
    }

    @Test fun restoresKnownThemesAndFallsBackForUnknownValues() {
        ProfileTheme.entries.forEach { assertEquals(it, ProfileTheme.fromStored(it.name)) }
        assertEquals(ProfileTheme.Light, ProfileTheme.fromStored("unknown"))
        assertEquals(ProfileTheme.Light, ProfileTheme.fromStored(null))
    }

    @Test fun textScalingIsBoundedAndHasThreeSteps() {
        listOf(-1 to 1f, 0 to 1f, 1 to 1.2f, 2 to 1.4f, 99 to 1.4f).forEach { (step, expected) ->
            assertEquals(expected, ProfilePreferences(textSizeStep = step).textScale, .001f)
        }
    }

    @Test fun validatesAndNormalizesNamesWithoutLosingAccents() {
        assertEquals("João da Silva", normalizedProfileName("  João \n da  Silva  "))
        assertNotNull(profileNameError(" \n "))
        assertNotNull(profileNameError("a".repeat(81)))
        assertNull(profileNameError("a".repeat(80)))
    }

    @Test fun initialsSupportAccentsAndEmptyNames() {
        assertEquals("ÁC", profileInitials(" Ágata Costa "))
        assertEquals("Q", profileInitials("   "))
        assertEquals("FP", profileInitials("Fernando Pessoa Junior"))
    }

    @Test fun localProfileDataIsScopedToUserAndEmail() {
        assertEquals(profileStorageScope("uid", " A@B.com "), profileStorageScope("uid", "a@b.com"))
        assertNotEquals(profileStorageScope("uid", "a@b.com"), profileStorageScope("uid", "other@b.com"))
        assertNotEquals(profileStorageScope("uid", "a@b.com"), profileStorageScope("other", "a@b.com"))
    }

    @Test fun photoPersistenceOnlyAcceptsContentUris() {
        assertEquals("content://media/1", storedPhotoUri("content://media/1"))
        listOf(null, "", "content://", "file:///secret", "https://example.com").forEach { assertNull(storedPhotoUri(it)) }
    }

    @Test fun contrastThemeUsesReadablePrimaryAndSurfacePairs() {
        fun contrast(first: Float, second: Float) = (maxOf(first, second) + .05f) / (minOf(first, second) + .05f)
        assertTrue(contrast(HighContrastTokens.onPrimary.luminance(), HighContrastTokens.primary.luminance()) >= 4.5f)
        assertTrue(contrast(HighContrastTokens.textPrimary.luminance(), HighContrastTokens.surfaceBackground.luminance()) >= 7f)
        assertEquals(theme.DarkTokens.surfaceBackground, HighContrastTokens.surfaceBackground)
        assertEquals(theme.DarkTokens.primary, HighContrastTokens.primary)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF00DC9D), HighContrastTokens.borderFocus)
    }

    @Test fun birthDateUsesOnlyDayAndMonthWithoutInventingAYear() {
        listOf("", "20/04", "29/02", "31/12").forEach { assertNull(it, profileBirthDateError(it)) }
        listOf("20/04/2000", "31/04", "30/02", "00/12", "20/13", "2/04", "abcd").forEach {
            assertNotNull(it, profileBirthDateError(it))
        }
        assertEquals("20/04", formatProfileBirthDate("2004"))
        assertEquals("20/04", formatProfileBirthDate("20/04"))
        assertEquals("20", formatProfileBirthDate("20"))
    }

    @Test fun validatesEditableEmailWithoutChangingTheAuthenticationScope() {
        assertNull(profileEmailError("gabrielabenfica@gmail.com.br"))
        listOf("", "sem-arroba", "a@", "a b@gmail.com", "a@@b.com").forEach { assertNotNull(profileEmailError(it)) }
    }
}

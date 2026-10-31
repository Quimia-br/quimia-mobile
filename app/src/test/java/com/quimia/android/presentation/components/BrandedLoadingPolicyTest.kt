package com.quimia.android.presentation.components

import org.junit.Assert.*
import org.junit.Test

class BrandedLoadingPolicyTest {
    @Test fun menuTabsNeverGetTheFullScreenLoaderEvenAfterTheDelay() {
        for (account in listOf(false, true)) for (ai in listOf(false, true))
            for (pending in listOf(false, true)) for (delayed in listOf(false, true)) {
                assertFalse(brandedLoadingVisible(true, account, ai, pending, delayed))
            }
    }

    @Test fun anInternalPageOnlyShowsTheLoaderWhileItsInitialRequestIsStillPending() {
        assertFalse(brandedLoadingVisible(false, false, false, true, false))
        assertTrue(brandedLoadingVisible(false, false, false, true, true))
        assertFalse(brandedLoadingVisible(false, false, false, false, true))
    }

    @Test fun authenticationAndAssistantCanUseTheBrandedEntranceWithoutAMenu() {
        assertTrue(brandedLoadingVisible(false, true, false, false, false))
        assertTrue(brandedLoadingVisible(false, false, true, false, false))
    }
}

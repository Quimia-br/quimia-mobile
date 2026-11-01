package com.quimia.android.presentation.components

import org.junit.Assert.*
import org.junit.Test

class BrandedLoadingPolicyTest {
    @Test fun menuTabsNeverGetTheFullScreenLoaderEvenAfterTheDelay() {
        for (account in listOf(false, true)) for (ai in listOf(false, true))
            for (pending in listOf(false, true)) {
                assertFalse(brandedLoadingVisible(true, account, ai, pending))
            }
    }

    @Test fun anInternalPageOnlyShowsTheLoaderWhileItsInitialRequestIsStillPending() {
        assertTrue(brandedLoadingVisible(false, false, false, true))
        assertFalse(brandedLoadingVisible(false, false, false, false))
    }

    @Test fun authenticationAndAssistantCanUseTheBrandedEntranceWithoutAMenu() {
        assertTrue(brandedLoadingVisible(false, true, false, false))
        assertTrue(brandedLoadingVisible(false, false, true, false))
    }
}

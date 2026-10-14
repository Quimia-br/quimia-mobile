package com.quimia.android

import androidx.compose.ui.graphics.vector.VectorPath
import com.composables.icons.lucide.SoapDispenserDroplet
import org.junit.Assert.assertTrue
import org.junit.Test

class SoapDispenserDropletTest {
    @Test
    fun iconContainsDrawablePaths() {
        val paths = SoapDispenserDroplet.root.filterIsInstance<VectorPath>()

        // Parsing SVG commands without attaching them builds a valid but invisible icon.
        assertTrue("The icon must contain paths", paths.isNotEmpty())
        assertTrue("Every outline must have drawing commands", paths.all { it.pathData.isNotEmpty() })
        assertTrue("Every outline must have a visible stroke", paths.all { it.stroke != null && it.strokeLineWidth > 0f })
    }
}

package com.quimia.android.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import theme.quimiaColorTokens

internal enum class SkeletonLayout { Grid, Lines, Detail, Profile, History }

@Composable
internal fun SkeletonBlock(modifier: Modifier = Modifier, radius: Int = 12) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val opacity by transition.animateFloat(.12f, .22f, infiniteRepeatable(tween(950), RepeatMode.Reverse), label = "skeletonAlpha")
    Box(modifier.clip(RoundedCornerShape(radius.dp)).background(quimiaColorTokens().textPrimary.copy(alpha = opacity)))
}

@Composable
internal fun LoadingSkeleton(layout: SkeletonLayout = SkeletonLayout.Grid, label: String = "Carregando", modifier: Modifier = Modifier) {
    val tokens = quimiaColorTokens()
    Column(modifier.fillMaxWidth().padding(24.dp).semantics(mergeDescendants = true) {
        contentDescription = label
        stateDescription = "Carregando"
    }, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        when (layout) {
            SkeletonLayout.Profile -> {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SkeletonBlock(Modifier.fillMaxWidth(.8f).height(20.dp))
                        SkeletonBlock(Modifier.fillMaxWidth(.6f).height(14.dp))
                        SkeletonBlock(Modifier.width(100.dp).height(32.dp), 24)
                    }
                    SkeletonBlock(Modifier.size(88.dp), 88)
                }
                SkeletonBlock(Modifier.fillMaxWidth().height(140.dp), 24)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SkeletonBlock(Modifier.weight(1f).height(140.dp), 24)
                    SkeletonBlock(Modifier.weight(1f).height(140.dp), 24)
                }
                SkeletonBlock(Modifier.fillMaxWidth().height(44.dp), 24)
            }
            SkeletonLayout.Grid -> repeat(2) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    repeat(2) {
                        Column(Modifier.weight(1f).background(tokens.surfaceBase, RoundedCornerShape(24.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            SkeletonBlock(Modifier.fillMaxWidth(.85f).height(16.dp))
                            SkeletonBlock(Modifier.fillMaxWidth(.6f).height(12.dp))
                            SkeletonBlock(Modifier.fillMaxWidth().height(110.dp))
                            SkeletonBlock(Modifier.fillMaxWidth().height(28.dp), 24)
                        }
                    }
                }
            }
            SkeletonLayout.Detail -> {
                SkeletonBlock(Modifier.fillMaxWidth(.7f).height(24.dp))
                SkeletonBlock(Modifier.fillMaxWidth(.45f).height(14.dp))
                SkeletonBlock(Modifier.fillMaxWidth().height(160.dp), 24)
                SkeletonBlock(Modifier.fillMaxWidth().height(96.dp), 24)
                SkeletonBlock(Modifier.fillMaxWidth().height(56.dp), 24)
            }
            SkeletonLayout.History -> repeat(2) {
                Column(Modifier.fillMaxWidth().background(tokens.surfaceBase, RoundedCornerShape(24.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SkeletonBlock(Modifier.fillMaxWidth(.8f).height(18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        SkeletonBlock(Modifier.weight(1f).height(130.dp), 24)
                        SkeletonBlock(Modifier.weight(1f).height(130.dp), 24)
                    }
                    SkeletonBlock(Modifier.width(55.dp).height(12.dp))
                }
            }
            SkeletonLayout.Lines -> repeat(3) { SkeletonBlock(Modifier.fillMaxWidth(if (it == 2) .65f else 1f).height(16.dp)) }
        }
    }
}

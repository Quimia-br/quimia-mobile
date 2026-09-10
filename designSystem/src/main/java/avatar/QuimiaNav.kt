package avatar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import buttons.QuimiaIconButton
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import theme.PaddingLarge
import theme.PaddingMedium
import theme.quimiaColorTokens

@Composable
fun QuimiaNavController(
    startDestination: Any,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    verticalPadding: Dp = PaddingMedium.dp,
    horizontalPadding: Dp = PaddingLarge.dp,
    buttonColor: Color? = null,
    iconColor: Color? = null,
    iconSize: Dp = 20.dp,
    iconPadding: Dp = 8.dp,
    onCloseClick: () -> Unit = {},
    builder: NavGraphBuilder.(navController: NavController) -> Unit
) {
    val tokens = quimiaColorTokens()
    val actualButtonColor = buttonColor ?: tokens.secondary
    val actualIconColor = iconColor ?: tokens.foregroundSecondary

    val navController = rememberNavController()

    val currentOnCloseClick by rememberUpdatedState(onCloseClick)

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val canNavigateBack by remember(navBackStackEntry) {
        derivedStateOf { navController.previousBackStackEntry != null }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            QuimiaNav(
                backgroundColor = backgroundColor,
                horizontalPadding = horizontalPadding,
                verticalPadding = verticalPadding,
                buttonColor = actualButtonColor,
                iconColor = actualIconColor,
                iconSize = iconSize,
                iconPadding = iconPadding,
                showBackButton = canNavigateBack,
                onBackClick = { navController.popBackStack() },
                onCloseClick = currentOnCloseClick
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            builder(navController)
        }
    }
}

@Composable
fun QuimiaNav(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    horizontalPadding: Dp = PaddingLarge.dp,
    verticalPadding: Dp = PaddingMedium.dp,
    buttonColor: Color? = null,
    iconColor: Color? = null,
    iconSize: Dp = 20.dp,
    iconPadding: Dp = 11.dp,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    onCloseClick: () -> Unit = {}
) {
    val tokens = quimiaColorTokens()

    val actualButtonColor = buttonColor ?: tokens.secondary
    val actualIconColor = iconColor ?: tokens.foregroundSecondary

    Box(
        modifier = modifier
            .background(color = backgroundColor)
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
    ) {
        if (showBackButton) {
            QuimiaIconButton(
                modifier = Modifier.align(Alignment.CenterStart),
                icon = Lucide.ChevronLeft,
                onClick = onBackClick,
                containerColor = actualButtonColor,
                iconColor = actualIconColor,
                contentDescription = "Back",
                iconSize = iconSize,
                padding = iconPadding,
            )
        }
        QuimiaIconButton(
            modifier = Modifier.align(Alignment.CenterEnd),
            icon = Lucide.X,
            onClick = onCloseClick,
            containerColor = actualButtonColor,
            iconColor = actualIconColor,
            contentDescription = "Close",
            iconSize = iconSize,
            padding = iconPadding,
        )
    }
}

@Preview
@Composable
private fun QuimiaNavPreview() {
    QuimiaNav(
        modifier = Modifier.width(393.dp),
        showBackButton = true,
        backgroundColor = quimiaColorTokens().surfaceBackground,
        onBackClick = {},
        onCloseClick = {}
    )
}

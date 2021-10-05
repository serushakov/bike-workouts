package io.ushakov.bike_workouts.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun ThemedTopAppBar(
    modifier: Modifier = Modifier,
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
    contentPadding: PaddingValues = AppBarDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    TopAppBar(
        modifier = modifier,
        backgroundColor = MaterialTheme.colors.surface,
        elevation = elevation,
        contentPadding = contentPadding,
        content = content,
    )
}

@Composable
fun ThemedTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
) {
    TopAppBar(
        modifier = modifier,
        backgroundColor = MaterialTheme.colors.surface,
        elevation = elevation,
        contentColor = MaterialTheme.colors.primary,
        title = title,
        navigationIcon = navigationIcon,
        actions = {
            // This makes buttons have full opacity
            CompositionLocalProvider(LocalContentAlpha provides 1f) {
                actions()
            }
        },
    )
}
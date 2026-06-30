package org.ivanzaytsev.tariffanalyzer

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.ivanzaytsev.tariffanalyzer.presentation.tariff.TariffScreen
import org.ivanzaytsev.tariffanalyzer.theme.AppTheme

@Preview
@Composable
fun App(
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {}
) = AppTheme(onThemeChanged) {
    TariffScreen()
}

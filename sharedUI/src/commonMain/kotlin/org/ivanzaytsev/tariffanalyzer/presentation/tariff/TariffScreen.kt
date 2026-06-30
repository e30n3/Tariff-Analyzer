package org.ivanzaytsev.tariffanalyzer.presentation.tariff

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import org.ivanzaytsev.tariffanalyzer.domain.model.Tariff
import org.ivanzaytsev.tariffanalyzer.domain.usecase.GetTariffsUseCase
import org.ivanzaytsev.tariffanalyzer.theme.LocalThemeIsDark
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import tariff_analyzer.sharedui.generated.resources.Res
import tariff_analyzer.sharedui.generated.resources.data_and_minutes
import tariff_analyzer.sharedui.generated.resources.ic_dark_mode
import tariff_analyzer.sharedui.generated.resources.ic_light_mode
import tariff_analyzer.sharedui.generated.resources.ic_rotate_right
import tariff_analyzer.sharedui.generated.resources.price_per_month
import tariff_analyzer.sharedui.generated.resources.refresh
import tariff_analyzer.sharedui.generated.resources.retry
import tariff_analyzer.sharedui.generated.resources.tariffs_title
import androidx.compose.runtime.LaunchedEffect

@Composable
fun TariffScreen() {
    val getTariffs = koinInject<GetTariffsUseCase>()
    val viewModel: TariffViewModel = viewModel { TariffViewModel(getTariffs) }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is TariffContract.Effect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    TariffContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TariffContent(
    state: TariffContract.State,
    snackbarHostState: SnackbarHostState,
    onIntent: (TariffContract.Intent) -> Unit,
) {
    var isDark by LocalThemeIsDark.current
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.tariffs_title)) },
                actions = {
                    IconButton(onClick = { onIntent(TariffContract.Intent.Refresh) }) {
                        Icon(
                            vectorResource(Res.drawable.ic_rotate_right),
                            contentDescription = stringResource(Res.string.refresh),
                        )
                    }
                    IconButton(onClick = { isDark = !isDark }) {
                        val icon =
                            if (isDark) Res.drawable.ic_light_mode else Res.drawable.ic_dark_mode
                        Icon(vectorResource(icon), contentDescription = null)
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.isLoading && state.tariffs.isEmpty() ->
                    CircularProgressIndicator()

                state.error != null && state.tariffs.isEmpty() ->
                    ErrorState(
                        message = state.error,
                        onRetry = { onIntent(TariffContract.Intent.Load) },
                    )

                else -> TariffList(
                    tariffs = state.tariffs,
                    selectedId = state.selectedId,
                    onSelect = { onIntent(TariffContract.Intent.Select(it)) },
                )
            }
        }
    }
}

@Composable
private fun TariffList(
    tariffs: List<Tariff>,
    selectedId: String?,
    onSelect: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(tariffs, key = { it.id }) { tariff ->
            TariffCard(
                tariff = tariff,
                selected = tariff.id == selectedId,
                onClick = { onSelect(tariff.id) },
            )
        }
    }
}

@Composable
private fun TariffCard(
    tariff: Tariff,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = if (selected) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    } else {
        CardDefaults.cardColors()
    }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = colors,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tariff.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = tariff.provider,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(
                        Res.string.data_and_minutes,
                        tariff.dataGb,
                        tariff.callMinutes,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(
                text = stringResource(
                    Res.string.price_per_month,
                    "$${tariff.monthlyPrice}",
                ),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp),
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge,
        )
        ElevatedButton(onClick = onRetry) {
            Text(stringResource(Res.string.retry))
        }
    }
}

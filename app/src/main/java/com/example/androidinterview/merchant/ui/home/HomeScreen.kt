package com.example.androidinterview.merchant.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.androidinterview.R
import com.example.androidinterview.merchant.ui.home.components.ActivityRow
import com.example.androidinterview.merchant.ui.home.components.BalanceSection
import kotlinx.coroutines.flow.Flow

/**
 * Stateful entry point of the Home screen: collects the ViewModel state and events and delegates to
 * [HomeScreen].
 *
 * @param onShowActivity Called when the full activity list should be opened.
 * @param modifier Modifier applied to the screen.
 * @param viewModel ViewModel providing the screen state.
 */
@Composable
fun HomeRoute(
    onShowActivity: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeEvents(events = viewModel.events, onShowActivity = onShowActivity)
    HomeScreen(
        state = state,
        onRetry = viewModel::retry,
        onShowMoreClick = viewModel::onShowMoreClick,
        modifier = modifier,
    )
}

/**
 * Runs the ViewModel's one-shot events while the screen is on screen.
 *
 * @param events Events emitted by the ViewModel.
 * @param onShowActivity Called when the full activity list should be opened.
 */
@Composable
private fun HomeEvents(
    events: Flow<HomeViewModel.Event>,
    onShowActivity: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(events) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            events.collect { event ->
                when (event) {
                    HomeViewModel.Event.ShowActivity -> onShowActivity()
                }
            }
        }
    }
}

/**
 * Stateless Home screen showing balances and recent activity.
 *
 * @param state What to render.
 * @param onRetry Called when the user taps Retry on the error state.
 * @param onShowMoreClick Called when the user taps Show More.
 * @param modifier Modifier applied to the screen.
 */
@Composable
fun HomeScreen(
    state: HomeUiState,
    onRetry: () -> Unit,
    onShowMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        when (state) {
            HomeUiState.Loading -> LoadingContent(Modifier.padding(innerPadding))
            is HomeUiState.Error -> ErrorContent(state.reason, onRetry, Modifier.padding(innerPadding))
            is HomeUiState.Success -> SuccessContent(
                state = state,
                onShowMoreClick = onShowMoreClick,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

/**
 * Centred progress indicator.
 *
 * @param modifier Modifier applied to the container.
 */
@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.home_loading)
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.semantics { contentDescription = description })
    }
}

/**
 * Error message with a Retry button.
 *
 * @param reason Why loading failed.
 * @param onRetry Called when the user taps Retry.
 * @param modifier Modifier applied to the container.
 */
@Composable
private fun ErrorContent(
    reason: HomeUiState.Error.Reason,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = reason.message(),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onRetry) {
            Text(stringResource(R.string.home_retry))
        }
    }
}

/**
 * Title, balances, recent activity and the Show More button.
 *
 * @param state Loaded data to render.
 * @param onShowMoreClick Called when the user taps Show More.
 * @param modifier Modifier applied to the container.
 */
@Composable
private fun SuccessContent(
    state: HomeUiState.Success,
    onShowMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.home_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .semantics { heading() },
            )
        }
        item {
            Column(
                modifier = Modifier.padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SectionTitle(stringResource(R.string.balance_title))
                BalanceSection(state.availableBalance, state.pendingBalance)
            }
        }
        item { SectionTitle(stringResource(R.string.activity_title)) }
        if (state.recentActivity.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.activity_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }
        } else {
            items(state.recentActivity, key = { it.id }) { row ->
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ActivityRow(row)
            }
            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                FilledTonalButton(
                    onClick = onShowMoreClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                ) {
                    Text(stringResource(R.string.activity_show_more))
                }
            }
        }
    }
}

/**
 * Bold section heading, exposed as a heading to accessibility services.
 *
 * @param text Heading text.
 * @param modifier Modifier applied to the text.
 */
@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = modifier.semantics { heading() },
    )
}

/** Localised text for this error reason. */
@Composable
private fun HomeUiState.Error.Reason.message(): String = when (this) {
    HomeUiState.Error.Reason.Network -> stringResource(R.string.error_network)
    HomeUiState.Error.Reason.Unexpected -> stringResource(R.string.error_unexpected)
}

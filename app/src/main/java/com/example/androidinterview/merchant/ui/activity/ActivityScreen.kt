package com.example.androidinterview.merchant.ui.activity

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
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
import com.example.androidinterview.core.format.formatShort
import com.example.androidinterview.merchant.ui.activity.components.ActivityListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/** How many items before the end of the list the next page starts loading. */
private const val PREFETCH_DISTANCE = 5

/**
 * Stateful entry point of the activity list, shown as a full-screen dialog destination: collects the
 * ViewModel state and events and delegates to [ActivityScreen].
 *
 * The ViewModel belongs to the destination's back stack entry, so each time the list is opened it
 * starts fresh, and it is cleared when the destination is popped.
 *
 * @param onDismiss Called when the list should be closed.
 * @param viewModel ViewModel providing the list state.
 */
@Composable
fun ActivityRoute(
    onDismiss: () -> Unit,
    viewModel: ActivityViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ActivityEvents(events = viewModel.events, onDismiss = onDismiss)
    Surface(modifier = Modifier.fillMaxSize()) {
        ActivityScreen(
            state = state,
            onDone = viewModel::onDoneClick,
            onLoadMore = viewModel::loadMore,
            onRetry = viewModel::retry,
            modifier = Modifier.statusBarsPadding(),
        )
    }
}

/**
 * Runs the ViewModel's one-shot events while the screen is on screen.
 *
 * @param events Events emitted by the ViewModel.
 * @param onDismiss Called when the list should be closed.
 */
@Composable
private fun ActivityEvents(
    events: Flow<ActivityViewModel.Event>,
    onDismiss: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(events) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            events.collect { event ->
                when (event) {
                    ActivityViewModel.Event.Dismiss -> onDismiss()
                }
            }
        }
    }
}

/**
 * Stateless content of the activity list: header with Done, then the list in its current state.
 *
 * @param state What to render.
 * @param onDone Called when the user taps Done.
 * @param onLoadMore Called when the list is scrolled near its end.
 * @param onRetry Called when the user taps Retry after a failure.
 * @param modifier Modifier applied to the screen.
 */
@Composable
fun ActivityScreen(
    state: ActivityUiState,
    onDone: () -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.activity_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
            )
            TextButton(onClick = onDone) {
                Text(stringResource(R.string.activity_done), style = MaterialTheme.typography.titleMedium)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        val contentModifier = Modifier.weight(1f)
        when (state) {
            ActivityUiState.Loading -> LoadingContent(contentModifier)
            is ActivityUiState.Error -> ErrorContent(state.reason, onRetry, contentModifier)
            is ActivityUiState.Content -> ListContent(state, onLoadMore, onRetry, contentModifier)
        }
    }
}

/**
 * Centred progress indicator for the first page.
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
 * Error message with a Retry button, for the first page.
 *
 * @param reason Why loading failed.
 * @param onRetry Called when the user taps Retry.
 * @param modifier Modifier applied to the container.
 */
@Composable
private fun ErrorContent(
    reason: ActivityUiState.Reason,
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
 * Activity grouped under sticky date headers, with the next-page footer.
 *
 * @param state Loaded data to render.
 * @param onLoadMore Called when the list is scrolled near its end.
 * @param onRetry Called when the user taps Retry in the footer.
 * @param modifier Modifier applied to the list.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListContent(
    state: ActivityUiState.Content,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.sections.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.activity_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val listState = rememberLazyListState()
    LoadMoreEffect(listState, onLoadMore)

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        state.sections.forEach { section ->
            stickyHeader(key = section.date.toString()) {
                Text(
                    text = section.header.text(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .semantics { heading() },
                )
            }
            items(section.items, key = { it.id }) { item ->
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ActivityListItem(item)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
        item(key = "footer") {
            AppendFooter(state.append, onRetry)
        }
    }
}

/**
 * Calls [onLoadMore] whenever the last visible item comes within [PREFETCH_DISTANCE] of the end.
 *
 * @param listState State of the list to observe.
 * @param onLoadMore Called when more items are needed; may be called again before they arrive.
 */
@Composable
private fun LoadMoreEffect(listState: LazyListState, onLoadMore: () -> Unit) {
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)
    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= layoutInfo.totalItemsCount - PREFETCH_DISTANCE
        }
            .distinctUntilChanged()
            .filter { it }
            .collect { currentOnLoadMore() }
    }
}

/**
 * Bottom of the list: progress while the next page loads, or an error with Retry.
 *
 * @param append State of loading the next page.
 * @param onRetry Called when the user taps Retry.
 * @param modifier Modifier applied to the footer.
 */
@Composable
private fun AppendFooter(
    append: AppendState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val footerModifier = modifier
        .fillMaxWidth()
        .padding(16.dp)
    when (append) {
        AppendState.Loading -> Column(
            modifier = footerModifier.semantics(mergeDescendants = true) {},
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()
            Text(
                text = stringResource(R.string.activity_loading_more),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        is AppendState.Error -> Column(
            modifier = footerModifier,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = append.reason.message(),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onRetry) {
                Text(stringResource(R.string.home_retry))
            }
        }
        AppendState.Idle, AppendState.EndReached -> Unit
    }
}

/** Localised text of the section header. */
@Composable
private fun DateHeader.text(): String = when (this) {
    DateHeader.Today -> stringResource(R.string.activity_today)
    DateHeader.Yesterday -> stringResource(R.string.activity_yesterday)
    is DateHeader.Date -> remember(date) { date.formatShort() }
}

/** Localised text for this error reason. */
@Composable
private fun ActivityUiState.Reason.message(): String = when (this) {
    ActivityUiState.Reason.Network -> stringResource(R.string.error_network)
    ActivityUiState.Reason.Unexpected -> stringResource(R.string.error_unexpected)
}

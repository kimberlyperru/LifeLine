package com.perru.lifeline.presentation.auth

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.perru.lifeline.R
import com.perru.lifeline.ui.theme.LifeLineTheme
import com.perru.lifeline.ui.theme.Terracotta
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AuthScreen(
    initialPage: Int = 0,
    viewModel: AuthViewModel? = null,
    onAuthSuccess: () -> Unit
) {
    val tabs = listOf(stringResource(R.string.sign_in), stringResource(R.string.create_account))
    val pagerState = rememberPagerState(pageCount = { 2 }, initialPage = initialPage)
    val scope = rememberCoroutineScope()
    
    // Use hiltViewModel if not provided (for preview)
    val actualViewModel: AuthViewModel = viewModel ?: hiltViewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AuthHeader(
            title = stringResource(if (pagerState.currentPage == 0) R.string.welcome_back else R.string.join_lifeline),
            subtitle = stringResource(if (pagerState.currentPage == 0) R.string.login_subtitle else R.string.signup_subtitle)
        )

        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Terracotta,
            divider = {},
            indicator = { tabPositions ->
                if (pagerState.currentPage < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = Terracotta
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall
                        )
                    },
                    selectedContentColor = Terracotta,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> LoginContent(
                    viewModel = actualViewModel,
                    onLoginSuccess = onAuthSuccess,
                    onNavigateToSignUp = {
                        scope.launch { pagerState.animateScrollToPage(1) }
                    }
                )
                1 -> SignUpContent(
                    viewModel = actualViewModel,
                    onSignUpSuccess = onAuthSuccess,
                    onNavigateToLogin = {
                        scope.launch { pagerState.animateScrollToPage(0) }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    LifeLineTheme {
        AuthScreen(onAuthSuccess = {})
    }
}

package com.jerboa.ui.components.post

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.jerboa.api.API
import com.jerboa.datatypes.PostView
import com.jerboa.datatypes.api.GetPosts
import com.jerboa.datatypes.samplePostView
import com.jerboa.db.AccountViewModel
import com.jerboa.getCurrentAccount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun PostListings(
    posts: List<PostView>,
    onItemClicked: (postView: PostView) -> Unit = {},
    navController: NavController?,
) {
    // Remember our own LazyListState, can be
    // used to move to any position in the column.
    val listState = rememberLazyListState()

    @OptIn(ExperimentalFoundationApi::class)
    LazyColumn(state = listState) {
        // List of items
        items(posts) { postView ->
            PostListing(
                postView = postView,
                onItemClicked = onItemClicked,
                navController = navController
            )
        }
    }
}

@Composable
private fun PostListingsHeader(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState
) {
    TopAppBar(
        title = {
            Text(
                text = "Top Stories",
            )
        },
        navigationIcon = {
            IconButton(onClick = {
                scope.launch {
                    scaffoldState.drawerState.open()
                }
            }) {
                Icon(
                    Icons.Filled.Menu,
                    contentDescription = "Menu"
                )
            }
        },
    )
}

@Preview
@Composable
fun PreviewPostListingsHeader() {
    val scope = rememberCoroutineScope()
    val scaffoldState =
        rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    PostListingsHeader(scope, scaffoldState)
}

@Preview
@Composable
fun PreviewPostListings() {
    PostListings(
        posts = listOf(samplePostView, samplePostView),
        onItemClicked = {},
        navController = null,
    )
}

@Composable
fun DrawerHeader(
    accountViewModel: AccountViewModel,
    clickShowAccountAddMode: () -> Unit,
    showAccountAddMode: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.Blue)
            .padding(16.dp)
            .clickable(onClick = clickShowAccountAddMode),
        content = {
            val currentAccount = getCurrentAccount(accountViewModel = accountViewModel)
            currentAccount?.let { Text(text = it.name, color = Color.White) }
            Icon(
                imageVector = if (showAccountAddMode) {
                    Icons.Default.ExpandLess
                } else {
                    Icons.Default.ExpandMore
                },
                contentDescription = "TODO",
                modifier = Modifier.align(Alignment.End)
            )
        },
        verticalArrangement = Arrangement.Bottom,
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun IconAndTextDrawerItem(
    text: String,
    icon: ImageVector = Icons.Filled.Menu,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "TODO",
            modifier = Modifier
                .padding(12.dp)
                .size(24.dp)
        )
        Text(text = text, style = MaterialTheme.typography.subtitle1)
    }
}

@Preview
@Composable
fun IconAndTextDrawerItemPreview() {
    IconAndTextDrawerItem(text = "A test item", onClick = {})
}

@Composable
fun Drawer(
    navController: NavController = rememberNavController(),
    accountViewModel: AccountViewModel = viewModel(),
) {
    var showAccountAddMode by rememberSaveable { mutableStateOf(false) }

    DrawerHeader(
        accountViewModel = accountViewModel,
        showAccountAddMode = showAccountAddMode,
        clickShowAccountAddMode = { showAccountAddMode = !showAccountAddMode }
    )
    Divider()
    // Drawer items
    DrawerContent(
        accountViewModel = accountViewModel,
        showAccountAddMode = showAccountAddMode,
        navController = navController,
    )
}

@Composable
fun DrawerContent(
    accountViewModel: AccountViewModel,
    showAccountAddMode: Boolean,
    navController: NavController
) {
    AnimatedVisibility(
        visible = showAccountAddMode,
    ) {
        DrawerAddAccountMode(accountViewModel = accountViewModel, navController = navController)
    }

    AnimatedVisibility(
        visible = !showAccountAddMode,
    ) {
        DrawerItemsStandard()
    }
}

@Composable
fun DrawerItemsStandard() {
    Column {
        Text("Standard mode")
    }
}

@Composable
fun DrawerAddAccountMode(
    accountViewModel: AccountViewModel = viewModel(),
    navController: NavController = rememberNavController()
) {
    val ctx = LocalContext.current
    val accounts by accountViewModel.allAccounts.observeAsState()

    Column {
        IconAndTextDrawerItem(
            text = "Add Account", onClick = { navController.navigate(route = "login") }, icon = Icons.Default.Add,
        )
        accounts?.forEach {
            IconAndTextDrawerItem(
                text = "Switch to ${it.name}",
                onClick = {
                    accountViewModel.removeDefault()
                    accountViewModel.setDefault(it.id)
                    API.changeLemmyInstance(it.instance)
                },
                icon = Icons.Default.Login,
            )
        }
        accounts?.let {
            val currentAccount = getCurrentAccount(it)!!
            IconAndTextDrawerItem(
                text = "Sign Out",
                onClick = {
                    accountViewModel.delete(currentAccount)
                    var updatedList = it.toMutableList()
                    updatedList.remove(currentAccount)

                    if (updatedList.isNotEmpty()) {
                        accountViewModel.setDefault(updatedList[0].id)
                    }
                },
                icon = Icons.Default.Close,
            )
        }
    }
}

@Preview
@Composable
fun DrawerAddAccountModePreview() {
    DrawerAddAccountMode()
}

@Preview
@Composable
fun DrawerPreview() {
    Drawer()
}

@Composable
fun PostListingsScreen(
    navController: NavController,
    postListingsViewModel: PostListingsViewModel,
    accountViewModel: AccountViewModel,
) {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    val ctx = LocalContext.current

// Fetch initial posts for home screen
    postListingsViewModel.fetchPosts(
        GetPosts(
            auth = getCurrentAccount(accountViewModel = accountViewModel)?.jwt
        )
    )

    Surface(color = MaterialTheme.colors.background) {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                PostListingsHeader(scope, scaffoldState)
            },
            drawerContent = {
                Drawer(accountViewModel = accountViewModel, navController = navController)
            },
            content = {
                if (postListingsViewModel.loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                PostListings(
                    posts = postListingsViewModel.posts,
                    postListingsViewModel::onPostClicked,
                    navController,
                )
            }
        )
    }
}

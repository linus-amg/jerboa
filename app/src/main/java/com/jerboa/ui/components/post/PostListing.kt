package com.jerboa.ui.components.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.flowlayout.FlowRow
import com.jerboa.datatypes.Post
import com.jerboa.datatypes.PostView
import com.jerboa.datatypes.samplePost
import com.jerboa.datatypes.samplePostView
import com.jerboa.previewLines
import com.jerboa.ui.components.common.TimeAgo
import com.jerboa.ui.components.community.CommunityLink
import com.jerboa.ui.components.person.PersonLink
import com.jerboa.ui.theme.ACTION_BAR_ICON_SIZE

@Composable
fun PostHeaderLine(postView: PostView) {
    FlowRow {
        CommunityLink(community = postView.community)
        DotSpacer()
        PersonLink(person = postView.creator)
        DotSpacer()
        TimeAgo(dateStr = postView.post.published)
    }
}

@Composable
fun DotSpacer() {
    Text(
        text = "·",
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

@Preview
@Composable
fun PostHeaderLinePreview() {
    val postView = samplePostView
    PostHeaderLine(postView = postView)
}

@Composable
fun PostTitleAndDesc(
    post: Post,
    fullBody: Boolean = false
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Title of the post
        Text(
            text = post.name,
            style = MaterialTheme.typography.subtitle1
        )

        // The desc
        post.body?.let {
            val text = if (fullBody) it else previewLines(it)
            // TODO markdown
            Text(
                text = text,
                style = MaterialTheme.typography.body2,
            )
        }
    }
}

@Preview
@Composable
fun PreviewStoryTitleAndMetadata() {
    PostTitleAndDesc(
        post = samplePost
    )
}

@Composable
fun PostFooterLine(postView: PostView) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row {
            CommentCount(comments = postView.counts.comments)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Upvotes(upvotes = postView.counts.upvotes)
            Downvotes(downvotes = postView.counts.downvotes)
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "TODO",
                modifier = Modifier.size(ACTION_BAR_ICON_SIZE),
            )
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "TODO",
                modifier = Modifier.size(ACTION_BAR_ICON_SIZE)
            )
        }
    }
}

@Composable
fun Upvotes(upvotes: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.ArrowUpward,
            contentDescription = "TODO",
            modifier = Modifier
                .size(ACTION_BAR_ICON_SIZE)
                .padding(end = 2.dp)
        )
        Text(
            text = upvotes.toString(),
            style = MaterialTheme.typography.button
        )
    }
}

@Preview
@Composable
fun UpvotesPreview() {
    Upvotes(upvotes = 31)
}

@Composable
fun Downvotes(downvotes: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.ArrowDownward,
            contentDescription = "TODO",
            modifier = Modifier
                .size(ACTION_BAR_ICON_SIZE)
                .padding(end = 2.dp),
        )
        Text(
            text = downvotes.toString(),
            style = MaterialTheme.typography.button
        )
    }
}

@Preview
@Composable
fun DownvotesPreview() {
    Downvotes(downvotes = 6)
}

@Composable
fun CommentCount(comments: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.ChatBubble,
//      painter = painterResource(id = R.drawable.ic_message_square),
            contentDescription = "TODO",
            modifier = Modifier
                .size(ACTION_BAR_ICON_SIZE)
                .padding(end = 2.dp)
        )
        Text(
            text = "$comments comments",
        )
    }
}

@Preview
@Composable
fun CommentCountPreview() {
    CommentCount(42)
}

@Preview
@Composable
fun PostFooterLinePreview() {
    PostFooterLine(postView = samplePostView)
}

@Preview
@Composable
fun PreviewPostListing() {
    PostListing(
        postView = samplePostView,
        fullBody = true,
    )
}

@Composable
fun PostListing(
    postView: PostView,
    fullBody: Boolean = false,
    onItemClicked: (postView: PostView) -> Unit = {},
    navController: NavController? = null,
) {
    Card(
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .clickable {
                onItemClicked(postView)
                navController?.navigate("post")
            }
    ) {
        Box(modifier = Modifier.padding(8.dp)) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // Header
                PostHeaderLine(postView = postView)

                //  Title + metadata
                PostTitleAndDesc(post = postView.post, fullBody)

                // Footer bar
                PostFooterLine(postView = postView)
            }
        }
    }
}

@Composable
private fun PostListingHeader(
    navController: NavController,
) {
    TopAppBar(
        title = {
            Text(
                text = "Post",
            )
        },
        navigationIcon = {
            IconButton(onClick = {
                navController.popBackStack()
            }) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
    )
}

@Preview
@Composable
fun PostListingHeaderPreview() {
    val navController = rememberNavController()
    PostListingHeader(navController = navController)
}

@Composable
fun PostListingScreen(
    postView: PostView,
    navController: NavController,
) {
    Surface(color = MaterialTheme.colors.background) {
        Scaffold(
            topBar = {
                PostListingHeader(navController)
            },
        ) {
            PostListing(
                postView = postView,
                fullBody = true,
            )
        }
    }
}

@Preview
@Composable
fun PreviewPostListingScreen() {
    val navController = rememberNavController()
    PostListingScreen(
        postView = samplePostView,
        navController
    )
}

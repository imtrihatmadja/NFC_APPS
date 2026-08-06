package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.NfcViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: NfcViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppLayout(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout(viewModel: NfcViewModel) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedPost by viewModel.selectedPost.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var isSearchExpanded by remember { mutableStateOf(false) }

    // Toast feedback on background sync results
    LaunchedEffect(syncStatus) {
        syncStatus?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearSyncStatus()
        }
    }

    Scaffold(
        topBar = {
            // Top Brand bar
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = TablerLight,
                            border = BorderStroke(1.dp, TablerBorder),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_nfc_1),
                                contentDescription = "Logo NFC Indonesia",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Text(
                            text = "National Fishers Center",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TablerDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    if (isSearchExpanded) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = TablerLight,
                            border = BorderStroke(1.5.dp, TablerBlue),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .width(200.dp)
                                .height(38.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = TablerBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.setSearchQuery(it) },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TablerDark
                                    ),
                                    cursorBrush = SolidColor(TablerBlue),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("global_search_input"),
                                    decorationBox = { innerTextField ->
                                        Box(
                                            contentAlignment = Alignment.CenterStart,
                                            modifier = Modifier.fillMaxHeight()
                                        ) {
                                            if (searchQuery.isEmpty()) {
                                                Text(
                                                    text = "Cari kata kunci...",
                                                    fontSize = 12.sp,
                                                    color = TablerSecondary,
                                                    fontWeight = FontWeight.Normal
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                                IconButton(
                                    onClick = {
                                        isSearchExpanded = false
                                        viewModel.setSearchQuery("")
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Tutup pencarian",
                                        tint = TablerDanger,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = TablerBlue.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, TablerBlue.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clickable { isSearchExpanded = true }
                                .testTag("header_search_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Cari",
                                    tint = TablerBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Cari",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TablerBlue
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TablerDark
                )
            )
        },
        bottomBar = {
            // Tabler Image 3 Style Navigation Tabs Selector at the bottom
            TablerNavigationTabs(
                activeTab = currentTab,
                onTabSelected = { viewModel.setTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TablerLight)
                .padding(innerPadding)
        ) {
            val loggedInUser by viewModel.currentUser.collectAsState()
            val isCalculatorOpen by viewModel.isCalculatorOpen.collectAsState()

            // Render active screen content
            when (currentTab) {
                "Beranda" -> HomeScreen(viewModel = viewModel)
                "Pelatihan" -> TrainingScreen(viewModel = viewModel)
                "Tentang Kami" -> AboutScreen()
                "Lowongan" -> JobsScreen(viewModel = viewModel)
                "Kegiatan" -> ActivitiesScreen(viewModel = viewModel)
                "Profil" -> {
                    if (loggedInUser != null) {
                        ProfileScreen(viewModel = viewModel)
                    } else {
                        LoginScreen(viewModel = viewModel)
                    }
                }
            }

            // AKP Calculator full screen overlay
            AnimatedVisibility(
                visible = isCalculatorOpen,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                AkpCalculatorScreen(
                    viewModel = viewModel,
                    onBackClick = { viewModel.setCalculatorOpen(false) }
                )
            }

            // Post Detail full screen overlay
            AnimatedVisibility(
                visible = selectedPost != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedPost?.let { post ->
                    PostDetailScreen(
                        post = post,
                        onBack = { viewModel.setSelectedPost(null) }
                    )
                }
            }
        }
    }
}

// Custom Navigation tab layout inspired by Tabler UI Image 3
@Composable
fun TablerNavigationTabs(
    activeTab: String,
    onTabSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, TablerBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabsList = listOf(
                TabItem("Beranda", Icons.Default.Home),
                TabItem("Pelatihan", Icons.Default.School),
                TabItem("Profil", Icons.Default.AccountCircle),
                TabItem("Lowongan", Icons.Default.BusinessCenter),
                TabItem("Kegiatan", Icons.Default.Campaign)
            )

            tabsList.forEach { tab ->
                val isSelected = activeTab == tab.name
                
                if (tab.name == "Profil") {
                    // Center profile with enlarged floating-like action button design
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .clickable { onTabSelected(tab.name) }
                            .padding(vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(
                                    color = if (isSelected) TablerBlue else TablerBlue.copy(alpha = 0.1f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.name,
                                tint = if (isSelected) Color.White else TablerBlue,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tab.name,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) TablerBlue else TablerSecondary
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onTabSelected(tab.name) }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.name,
                            tint = if (isSelected) TablerBlue else TablerSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = tab.name,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) TablerBlue else TablerSecondary
                        )
                    }
                }
            }
        }
    }
}

data class TabItem(val name: String, val icon: ImageVector)

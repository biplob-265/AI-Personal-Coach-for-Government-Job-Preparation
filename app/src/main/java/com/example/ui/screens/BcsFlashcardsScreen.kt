package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.FlashcardEntity
import androidx.compose.ui.text.TextStyle
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BcsFlashcardsScreen(
    flashcards: List<FlashcardEntity>,
    onAddFlashcard: (String, String, String) -> Unit,
    onDeleteFlashcard: (Int) -> Unit,
    onReviewFlashcard: (FlashcardEntity, Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = পড়া (Review), 1 = ড্যাশবোর্ড / সব কার্ড
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf("all") }

    // Categories names mapping
    val categories = listOf(
        CategoryItem("all", "সব বিষয়", "📚"),
        CategoryItem("bangla", "বাংলা সাহিত্য/ব্যাকরণ", "✍️"),
        CategoryItem("english", "English Literature/Grammar", "🇬🇧"),
        CategoryItem("math", "গণিত ও যুক্তি", "📐"),
        CategoryItem("gk", "সাধারণ জ্ঞান", "🌏")
    )

    // Filtered flashcards
    val filteredCards = remember(flashcards, selectedCategoryFilter) {
        if (selectedCategoryFilter == "all") {
            flashcards
        } else {
            flashcards.filter { it.subjectId == selectedCategoryFilter }
        }
    }

    // Due flashcards (nextReviewTimeMills <= currentTime)
    val currentTime = System.currentTimeMillis()
    val dueCards = remember(filteredCards, currentTime) {
        filteredCards.filter { it.nextReviewTimeMills <= currentTime }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("bcs_flashcards_container"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "বিসিএস ডিজিটাল ফ্ল্যাশকার্ডস",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "SM-২ স্পেসড রিপিটিশন পদ্ধতি",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("flashcards_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                            .testTag("action_add_flashcard")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "নতুন কার্ড",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Stats indicator bar
            FlashcardStatsBar(
                totalCards = filteredCards.size,
                dueCards = dueCards.size
            )

            // Category horizontally scroller
            CategorySelectorTab(
                categories = categories,
                selectedCategory = selectedCategoryFilter,
                onCategorySelect = { selectedCategoryFilter = it }
            )

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("পাঠচক্র (Due Items: ${dueCards.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    modifier = Modifier.testTag("tab_review_mode")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("সব কার্ড (${filteredCards.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    modifier = Modifier.testTag("tab_all_cards_mode")
                )
            }

            if (selectedTab == 0) {
                // Read and review spaced repetition mode
                ReviewModeSection(
                    dueCards = dueCards,
                    onReviewRating = onReviewFlashcard,
                    onAddNewTrigger = { showAddDialog = true }
                )
            } else {
                // Edit and list management mode
                AllCardsListSection(
                    cardsList = filteredCards,
                    onDelete = onDeleteFlashcard
                )
            }
        }
    }

    if (showAddDialog) {
        AddFlashcardDialog(
            subjectCategories = categories.filter { it.id != "all" },
            onDismiss = { showAddDialog = false },
            onSave = { front, back, subj ->
                onAddFlashcard(front, back, subj)
                showAddDialog = false
            }
        )
    }
}

// Stats top strip
@Composable
fun FlashcardStatsBar(
    totalCards: Int,
    dueCards: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color(0xFF2E7D32), CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "মোট শব্দসম্ভার: $totalCards টি",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (dueCards > 0) Color(0xFFD32F2F) else Color(0xFF43A047), CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (dueCards > 0) "আজকে রিভিশন প্রয়োজন: $dueCards টি" else "সব রিভিশন সম্পন্ন! 🎉",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (dueCards > 0) Color(0xFFC62828) else Color(0xFF2E7D32)
            )
        }
    }
}

// Category lists selector strip
@Composable
fun CategorySelectorTab(
    categories: List<CategoryItem>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = categories.indexOfFirst { it.id == selectedCategory }.coerceAtLeast(0),
        edgePadding = 16.dp,
        containerColor = Color.Transparent,
        divider = {},
        indicator = {}
    ) {
        categories.forEach { item ->
            val isSelected = item.id == selectedCategory
            Card(
                modifier = Modifier
                    .padding(vertical = 4.dp, horizontal = 4.dp)
                    .clickable { onCategorySelect(item.id) }
                    .testTag("cat_filter_${item.id}"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = item.icon, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// Individual review interactive section
@Composable
fun ReviewModeSection(
    dueCards: List<FlashcardEntity>,
    onReviewRating: (FlashcardEntity, Int) -> Unit,
    onAddNewTrigger: () -> Unit
) {
    if (dueCards.isEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .testTag("empty_reveiw_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🏆 সব পড়ে ফেলেছেন!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "আপনার এই বিষয়ের সকল বিসিএস কার্ডগুলো রিভিশন বা পড়া সম্পন্ন হয়েছে। নতুন কোনো শব্দাবলী বা বিষয় জটিল মনে হলে নিচে ফ্ল্যাশকার্ড যুক্ত করুন।",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = onAddNewTrigger,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "যুক্ত")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("নতুন কার্ড তৈরি করুন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
        return
    }

    var currentIndex by remember(dueCards) { mutableIntStateOf(0) }
    // Bounds guard
    val safeIndex = currentIndex.coerceAtMost(dueCards.lastIndex).coerceAtLeast(0)
    val card = dueCards[safeIndex]

    var isFlipped by remember(card) { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 350, easing = LinearOutSlowInEasing),
        label = "card_flip_animation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Progress text
        Text(
            text = "কার্ড নম্বর: ${safeIndex + 1} / ${dueCards.size}",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Large physical card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 8 * density
                }
                .clickable { isFlipped = !isFlipped }
                .testTag("flashcard_flip_board"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isFlipped) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            border = BorderStroke(
                1.5.dp,
                if (isFlipped) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (rotation <= 90f) {
                    // Front side content
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = "  প্রশ্ন / বিষয়  ",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        Text(
                            text = card.front,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "💡 উত্তর দেখতে কার্ডে আলতো চাপ দিন (Tap to Flip)",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    // Back side content (mirrored horizontal translation to make text read normally)
                    Column(
                        modifier = Modifier.graphicsLayer { rotationY = 180f },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = "  উত্তর / ব্যাখ্যা  ",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        Text(
                            text = card.back,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Spaced Repetition action ratings
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isFlipped) "স্মরণ করার দক্ষতা মূল্যায়ন করুন:" else "উত্তর মনে করুন এবং কার্ডটি ঘুরিয়ে নিন",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            AnimatedVisibility(visible = isFlipped) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Rating 1: Forgot
                    Button(
                        onClick = {
                            onReviewRating(card, 1)
                            if (currentIndex < dueCards.lastIndex) {
                                currentIndex++
                            } else {
                                currentIndex = 0
                            }
                            isFlipped = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("rate_forgot"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ভুলে গেছি", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("১ দিন পর", fontSize = 8.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }

                    // Rating 2: Hard
                    Button(
                        onClick = {
                            onReviewRating(card, 2)
                            if (currentIndex < dueCards.lastIndex) {
                                currentIndex++
                            } else {
                                currentIndex = 0
                            }
                            isFlipped = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("rate_hard"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("কঠিন", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("২ দিন পর", fontSize = 8.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }

                    // Rating 3: Good
                    Button(
                        onClick = {
                            onReviewRating(card, 3)
                            if (currentIndex < dueCards.lastIndex) {
                                currentIndex++
                            } else {
                                currentIndex = 0
                            }
                            isFlipped = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("rate_good"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ভালো", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("৪ দিন পর", fontSize = 8.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }

                    // Rating 4: Easy
                    Button(
                        onClick = {
                            onReviewRating(card, 4)
                            if (currentIndex < dueCards.lastIndex) {
                                currentIndex++
                            } else {
                                currentIndex = 0
                            }
                            isFlipped = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("rate_easy"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("সহজ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("৭+ দিন পর", fontSize = 8.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            if (!isFlipped) {
                Button(
                    onClick = { isFlipped = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("show_answer_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("উত্তর দেখুন", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Manage tab: lists all cards in the category
@Composable
fun AllCardsListSection(
    cardsList: List<FlashcardEntity>,
    onDelete: (Int) -> Unit
) {
    if (cardsList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🗂️ কোনো ফ্ল্যাশকার্ড পাওয়া যায়নি",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "উপরের '+' আইকনে ট্যাপ করে জটিল বিষয় ও শব্দাবলী এখানে যুক্ত করুন যাতে SM-২ অ্যালগরিদমে বারবার পড়ে চিরস্মরণীয় করতে পারেন।",
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp, start = 12.dp, end = 12.dp)
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(cardsList) { card ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("flashcard_item_${card.id}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (card.subjectId) {
                                "bangla" -> Color(0xFFE8F5E9)
                                "english" -> Color(0xFFE3F2FD)
                                "math" -> Color(0xFFFFF3E0)
                                else -> Color(0xFFF3E5F5)
                            }
                        ) {
                            Text(
                                text = " " + when (card.subjectId) {
                                    "bangla" -> "বাংলা"
                                    "english" -> "ইংরেজি"
                                    "math" -> "গণিত"
                                    else -> "সা. জ্ঞান"
                                } + " ",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = when (card.subjectId) {
                                    "bangla" -> Color(0xFF2E7D32)
                                    "english" -> Color(0xFF1565C0)
                                    "math" -> Color(0xFFE65100)
                                    else -> Color(0xFF6A1B9A)
                                },
                                modifier = Modifier.padding(4.dp)
                            )
                        }

                        IconButton(
                            onClick = { onDelete(card.id) },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("delete_card_${card.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "ডিলেট করুন",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "প্রশ্ন: ${card.front}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "উত্তর: ${card.back}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "পরবর্তী রিভিশন: ${formatTimestampToDate(card.nextReviewTimeMills)}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (card.nextReviewTimeMills <= System.currentTimeMillis()) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                        )

                        Text(
                            text = "রিপিট: ${card.repetitions} বার | EF: ${String.format(Locale.US, "%.1f", card.easinessFactor)}",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

// Add Flashcard Dialog
@Composable
fun AddFlashcardDialog(
    subjectCategories: List<CategoryItem>,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var front by remember { mutableStateOf("") }
    var back by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf(subjectCategories.firstOrNull()?.id ?: "gk") }
    var errorMsg by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_flashcard_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "নতুন বিসিএস ফ্ল্যাশকার্ড",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = front,
                    onValueChange = { front = it },
                    label = { Text("প্রশ্ন বা বিষয় (Front)", fontSize = 12.sp) },
                    placeholder = { Text("উদা: গীতাঞ্জলি কাব্যের ইংরেজি অনুবাদ কে করেন?", fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("flashcard_input_front"),
                    textStyle = TextStyle(fontSize = 13.sp)
                )

                OutlinedTextField(
                    value = back,
                    onValueChange = { back = it },
                    label = { Text("উত্তর বা ব্যাখ্যা (Back)", fontSize = 12.sp) },
                    placeholder = { Text("উদা: ডব্লিউ বি ইয়েটস এর ভূমিকা সহ স্বয়ং রবীন্দ্র নাথ করেন।", fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("flashcard_input_back"),
                    textStyle = TextStyle(fontSize = 13.sp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "বিষয় সিলেক্ট করুন:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    subjectCategories.forEach { category ->
                        val isSelected = category.id == selectedSubjectId
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { selectedSubjectId = category.id }
                                .padding(vertical = 8.dp)
                                .testTag("select_subject_${category.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = category.icon, fontSize = 14.sp)
                                Text(
                                    text = when (category.id) {
                                        "bangla" -> "বাংলা"
                                        "english" -> "ইংরেজি"
                                        "math" -> "গণিত"
                                        else -> "সা. জ্ঞান"
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (errorMsg.isNotEmpty()) {
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("close_add_flashcard_dialog")
                    ) {
                        Text("বাতিল", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            if (front.trim().isEmpty() || back.trim().isEmpty()) {
                                errorMsg = "অনুগ্রহ করে প্রশ্ন এবং উত্তর দুটোই পূরণ করুন।"
                            } else {
                                onSave(front, back, selectedSubjectId)
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_add_flashcard_dialog"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("সংরক্ষণ করুন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// Format date helper
fun formatTimestampToDate(timestampMills: Long): String {
    val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
    return sdf.format(Date(timestampMills))
}

data class CategoryItem(val id: String, val name: String, val icon: String)

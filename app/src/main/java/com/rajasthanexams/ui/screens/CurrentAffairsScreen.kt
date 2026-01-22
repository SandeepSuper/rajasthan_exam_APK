package com.rajasthanexams.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rajasthanexams.ui.components.HeritagePatternBackground
import coil.compose.AsyncImage

data class NewsItem(
    val id: String,
    val title: String,
    val titleHi: String,
    val date: String,
    val description: String,
    val descriptionHi: String,
    val imageUrl: String? = null,
    val tag: String = "Daily" // "Daily" or "Weekly"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentAffairsScreen(
    isHindi: Boolean,
    onBackClick: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("Daily") }
    // Local language state, defaults to passed global setting
    var localIsHindi by remember(isHindi) { mutableStateOf(isHindi) }
    
    // Mock Data
    val newsList = remember {
        listOf(
            NewsItem(
                "1",
                "Rajasthan's New Solar Policy 2024 Announced",
                "राजस्थान की नई सौर नीति 2024 घोषित",
                "14 Jan 2024",
                "The state government has unveiled a new solar policy targeting 50GW capacity by 2030. Key focuses include decentralized solar generation and incentives for farmers.",
                "राज्य सरकार ने 2030 तक 50GW क्षमता का लक्ष्य रखते हुए एक नई सौर नीति का अनावरण किया है। मुख्य फोकस विकेंद्रीकृत सौर उत्पादन और किसानों के लिए प्रोत्साहन पर है।",
                "https://example.com/solar.jpg",
                "Daily"
            ),
            NewsItem(
                "2",
                "Weekly Recap: State Budget Highlights",
                "साप्ताहिक सारांश: राज्य बजट की मुख्य बातें",
                "10-14 Jan 2024",
                "A comprehensive review of the major announcements in the state budget, including education reforms and infrastructure projects in Jaipur and Jodhpur.",
                "राज्य बजट में प्रमुख घोषणाओं की व्यापक समीक्षा, जिसमें शिक्षा सुधार और जयपुर और जोधपुर में बुनियादी ढांचा परियोजनाएं शामिल हैं।",
                null,
                "Weekly"
            ),
            NewsItem(
                "3",
                "Padma Awards 2024: 5 from Rajasthan Honored",
                "पद्म पुरस्कार 2024: राजस्थान की 5 हस्तियां सम्मानित",
                "26 Jan 2024",
                "Five eminent personalities from Rajasthan have been conferred with Padma Shri awards for their contribution to Art and Social Work.",
                "राजस्थान की पांच प्रतिष्ठित हस्तियों को कला और सामाजिक कार्य में उनके योगदान के लिए पद्म श्री पुरस्कार से सम्मानित किया गया है।",
                null, 
                "Daily"
            ),
            NewsItem(
                "4",
                "Chiffon Sari Heritage of Kota: GI Tag Application",
                "कोटा की शिफॉन साड़ी विरासत: जीआई टैग आवेदन",
                "12 Jan 2024",
                "Artisians from Kota are pushing for a GI Tag for the unique Chiffon Sari weaving technique.",
                "कोटा के कारीगर अद्वितीय शिफॉन साड़ी बुनाई तकनीक के लिए जीआई टैग की मांग कर रहे हैं।",
                null,
                "Daily"
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if(localIsHindi) "करंट अफेयर्स" else "Current Affairs", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            if(localIsHindi) "हिन्दी" else "Eng",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = localIsHindi,
                            onCheckedChange = { localIsHindi = it },
                            modifier = Modifier.scale(0.8f) 
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        HeritagePatternBackground(modifier = Modifier.padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Filter Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "Daily",
                        onClick = { selectedFilter = "Daily" },
                        label = { Text(if(localIsHindi) "दैनिक अपडेट" else "Daily Updates") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "Weekly",
                        onClick = { selectedFilter = "Weekly" },
                        label = { Text(if(localIsHindi) "साप्ताहिक सारांश" else "Weekly Recap") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                LazyColumn(
                    contentPadding = PaddingValues(horizontal=16.dp, vertical=8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(newsList.filter { it.tag == selectedFilter }) { news ->
                        NewsCard(news, localIsHindi)
                    }
                }
            }
        }
    }
}

@Composable
fun NewsCard(news: NewsItem, isHindi: Boolean) {
    var isBookmarked by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            if (news.imageUrl != null) {
                // Placeholder for Image (Using Box + Color if loading fails or for mock)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(Color.Gray.copy(alpha=0.3f))
                ) {
                    // Logic for AsyncImage would go here
                     Text("Image Placeholder", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = if(isHindi) news.titleHi else news.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { isBookmarked = !isBookmarked }) {
                        Icon(
                            if(isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if(isBookmarked) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(news.date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    if(isHindi) news.descriptionHi else news.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.8f),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { /* Share Logic */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.Gray)
                    }
                }
            }
        }
    }
}

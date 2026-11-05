package com.babyygurll.store.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babyygurll.store.ui.theme.*
import com.babyygurll.store.ui.viewmodel.StoreViewModel

data class CategoryCardItem(
    val key: String,
    val title: String,
    val emoji: String,
    val description: String,
    val bgColor: Color
)

val CATEGORY_ITEMS = listOf(
    CategoryCardItem("plushies", "Amigurumi Plushies", "🧸", "Stuffed crochet animal cuties & mascots", Color(0xFFFFF0F5)),
    CategoryCardItem("scrunchies", "Hair Scrunchies", "🎀", "Fluffy milk cotton & ruffled hair bands", Color(0xFFF0FDF4)),
    CategoryCardItem("flowers", "Flowers & Bouquets", "💐", "Everlasting floral arrangements & single stems", Color(0xFFEFF6FF)),
    CategoryCardItem("bags", "Bags & Totes", "👜", "Bespoke shoulder bags & granny square totes", Color(0xFFFFFBEB)),
    CategoryCardItem("wearables", "Wearables & Beanies", "🧣", "Handcrafted bucket hats, coasters & accessories", Color(0xFFFAF5FF))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: StoreViewModel,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Crochet Collections",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BrandCream)
        ) {
            items(CATEGORY_ITEMS) { item ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = item.bgColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.selectCategory(item.key)
                            onCategorySelected(item.key)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.emoji, fontSize = 42.sp)

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        Text("→", fontSize = 22.sp, color = BrandPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


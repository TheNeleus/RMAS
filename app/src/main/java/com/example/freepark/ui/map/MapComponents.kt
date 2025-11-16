package com.example.freepark.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import com.example.freepark.data.model.Review
import com.example.freepark.data.model.Spot

private val PrimaryColor = Color.Black
private val SecondaryColor = Color(0xFF888888) // Tamnija siva
private val AccentColor = Color(0xFF444444)    // Veoma tamna siva
private val ItemBackgroundColor = Color.White

@Composable
fun SpotListItem(spot: Spot, onClick: () -> Unit) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = ItemBackgroundColor),

        headlineContent = { Text(spot.name, color = PrimaryColor, fontWeight = FontWeight.SemiBold) },

        supportingContent = { Text("${spot.type} • ${spot.authorName ?: "Unknown"}", color = SecondaryColor) },

        leadingContent = {
            spot.photoUrl?.let { url ->
                AsyncImage(model = url, contentDescription = "picture", modifier = Modifier.size(56.dp))
            } ?: Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f), shape = MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Text(spot.name.firstOrNull()?.uppercase() ?: "S", color = PrimaryColor, style = MaterialTheme.typography.titleLarge)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp, horizontal = 8.dp)
            .clickable { onClick() }
    )
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = Color.LightGray
    )
}

@Composable
fun ReviewItem(review: Review) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp, horizontal = 16.dp)
        .background(ItemBackgroundColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(review.username, style = MaterialTheme.typography.titleSmall, color = PrimaryColor)
            RatingBarDisplay(rating = review.rating)
        }
        if (review.comment.isNotBlank()) {
            Text(review.comment, style = MaterialTheme.typography.bodySmall, color = SecondaryColor)
        }

        review.photoUrl?.let { url ->
            Spacer(modifier = Modifier.height(8.dp))
            AsyncImage(
                model = url,
                contentDescription = "Picture of the review",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 250.dp)
                    .padding(vertical = 4.dp),
            )
        }

        Text(
            review.createdAt.toDate().toString(),
            style = MaterialTheme.typography.labelSmall,
            color = SecondaryColor
        )
    }
}

@Composable
fun RatingBarDisplay(rating: Int, max: Int = 5) {
    Row {
        for (i in 1..max) {
            Icon(
                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = null,
                tint = PrimaryColor
            )
        }
    }
}

@Composable
fun RatingBarInput(
    rating: Int,
    max: Int = 5,
    onRatingChanged: ((Int) -> Unit)? = null
) {
    Row {
        for (i in 1..max) {
            Icon(
                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "Grade $i",
                tint = PrimaryColor,
                modifier = Modifier
                    .size(32.dp)
                    .padding(2.dp)
                    .let {
                        if (onRatingChanged != null) {
                            it.clickable { onRatingChanged(i) }
                        } else it
                    }
            )
        }
    }
}
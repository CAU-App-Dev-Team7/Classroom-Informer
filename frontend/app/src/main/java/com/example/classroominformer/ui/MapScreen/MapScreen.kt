package com.example.classroominformer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.classroominformer.ui.components.TopBlueHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBack: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopBlueHeader(
            title = "Classroom Informer",
            showBackButton = true,
            onBackClick = onBack
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "MAP",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            Row(
                modifier = Modifier
                    .width(170.dp)
                    .height(40.dp)
                    .border(
                        width = 1.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(Icons.Default.Search, contentDescription = "Search")

                Spacer(modifier = Modifier.width(6.dp))

                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Search", color = Color.Gray) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(   // ⭐ CORRECT FUNCTION
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        cursorColor = Color.Black,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f)
                )

                if (searchText.isNotEmpty()) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear",
                        modifier = Modifier
                            .clickable { searchText = "" }
                            .padding(start = 4.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEFEFEF)),
            contentAlignment = Alignment.Center
        ) {
            Text("Map goes here")
        }
    }
}


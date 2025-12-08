package com.example.classroominformer.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classroominformer.data.*
import com.example.classroominformer.ui.components.TopBlueHeader
import kotlinx.coroutines.launch

// ---------------------------------------------------------------
// UI Model
// ---------------------------------------------------------------
data class FavouriteRoom(
    val id: Long,
    val roomName: String,     // "310-201"
    val building: String,     // "310"
    val roomNumber: String,   // "201"
    val createdAt: String
)

// ---------------------------------------------------------------
// MAIN SCREEN
// ---------------------------------------------------------------
@Composable
fun FavouritesScreen(
    userName: String,
    onBackClick: () -> Unit
) {
    val favoritesApi = RetrofitClient.favoritesApi
    val infoApi = RetrofitClient.infoApi
    val scope = rememberCoroutineScope()

    var favourites by remember { mutableStateOf<List<FavouriteRoom>>(emptyList()) }
    var searchResults by remember { mutableStateOf<List<FavouriteRoom>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // ---------------------------------------------------------
    // Load user's favourite rooms
    // ---------------------------------------------------------
    LaunchedEffect(Unit) {
        loading = true
        try {
            val res = favoritesApi.getMyFavorites()
            favourites = res.map { dto ->
                val r = dto.room
                FavouriteRoom(
                    id = dto.room_id,
                    roomName = if (r != null) "${r.building_code}-${r.room_number}" else "Unknown",
                    building = r?.building_code ?: "",
                    roomNumber = r?.room_number ?: "",
                    createdAt = dto.created_at
                )
            }
        } finally {
            loading = false
        }
    }

    // ---------------------------------------------------------
    // Search available rooms from backend
    // ---------------------------------------------------------
    fun searchRooms(query: String) {
        scope.launch {
            if (query.isBlank()) {
                searchResults = emptyList()
                return@launch
            }

            try {
                loading = true

                val building = query.substringBefore("-").trim()
                if (building.isEmpty()) {
                    searchResults = emptyList()
                    return@launch
                }

                // Dummy slot required by backend
                val dummySlots = listOf("월 09:00 - 10:00")

                val res = infoApi.getAvailableRooms(building, dummySlots)

                searchResults = res.map {
                    FavouriteRoom(
                        id = it.room_id.toLong(),
                        roomName = "${it.building_code}-${it.room_number}",
                        building = it.building_code,
                        roomNumber = it.room_number,
                        createdAt = ""
                    )
                }

            } catch (e: Exception) {
                searchResults = emptyList()
            } finally {
                loading = false
            }
        }
    }

    // ---------------------------------------------------------
    // Toggle favourite (ADD / REMOVE)
    // ---------------------------------------------------------
    fun toggleFavourite(roomId: Long) {
        scope.launch {
            try {
                favoritesApi.toggleFavorite(FavoriteToggleRequest(room_id = roomId))
            } catch (_: Exception) {}

            // Refresh list
            val res = favoritesApi.getMyFavorites()
            favourites = res.map { dto ->
                val r = dto.room
                FavouriteRoom(
                    id = dto.room_id,
                    roomName = if (r != null) "${r.building_code}-${r.room_number}" else "Unknown",
                    building = r?.building_code ?: "",
                    roomNumber = r?.room_number ?: "",
                    createdAt = dto.created_at
                )
            }
        }
    }

    // ---------------------------------------------------------
    // UI
    // ---------------------------------------------------------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopBlueHeader(
            title = "Favourites",
            showBackButton = true,
            onBackClick = onBackClick
        )

        // ---------------------- Search Input -----------------------
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            label = { Text("Search room (ex: 310-201)") },
            singleLine = true
        )

        Button(
            onClick = { searchRooms(searchQuery) },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            enabled = searchQuery.isNotBlank()
        ) {
            Text("Search")
        }

        // ---------------------- Loading -----------------------
        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return
        }

        // ---------------------- Search Results -----------------------
        if (searchResults.isNotEmpty()) {
            Text(
                "Search Results",
                modifier = Modifier.padding(16.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(searchResults) { room ->
                    val isFav = favourites.any { it.id == room.id }

                    SearchResultCard(
                        room = room,
                        isFavourite = isFav,
                        onToggle = { toggleFavourite(room.id) }
                    )
                }
            }
        }

        // ---------------------- Favourites List -----------------------
        Text(
            "My Favourites",
            modifier = Modifier.padding(16.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(favourites) { room ->
                FavouriteCard(
                    room = room,
                    onRemove = { toggleFavourite(room.id) }
                )
            }
        }
    }
}

// -------------------------------------------------------------------
// Search result card
// -------------------------------------------------------------------
@Composable
fun SearchResultCard(
    room: FavouriteRoom,
    isFavourite: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(room.roomName, fontWeight = FontWeight.Bold)
                Text("Building ${room.building}")
            }

            TextButton(onClick = onToggle) {
                Text(if (isFavourite) "Remove" else "Add")
            }
        }
    }
}

// -------------------------------------------------------------------
// Favourite card
// -------------------------------------------------------------------
@Composable
fun FavouriteCard(
    room: FavouriteRoom,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(room.roomName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Building ${room.building}")
            Text("Room ${room.roomNumber}")

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = onRemove) {
                Text("Remove from favourites")
            }
        }
    }
}

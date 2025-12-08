package com.example.classroominformer.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classroominformer.ui.components.TopBlueHeader
import kotlinx.coroutines.launch

// ──────────────────────────────────────────────
// Data model – maps directly to DB schema later
// favourite_rooms(user_id, room_id, room_name, building, floor, next_free_slot, is_free_now)
// ──────────────────────────────────────────────

data class FavouriteRoom(
    val id: Long,              // room_id in DB
    val roomName: String,
    val building: String,
    val floor: String,
    val nextFreeSlot: String,
    val isFreeNow: Boolean
)

// ──────────────────────────────────────────────
// Repository interface – BACKEND READY
// Later you implement this with Retrofit / Room / Supabase, etc.
// ──────────────────────────────────────────────

interface FavouriteRoomRepository {
    suspend fun getFavourites(userName: String): List<FavouriteRoom>
    suspend fun searchRooms(query: String): List<FavouriteRoom>
    suspend fun addFavourite(userName: String, roomId: Long)
    suspend fun removeFavourite(userName: String, roomId: Long)
}

// Temporary in-memory implementation for demo.
// Later: replace this with real backend implementation.
class InMemoryFavouriteRoomRepository : FavouriteRoomRepository {

    // All rooms that exist in the system (fake data for now)
    private val allRooms = listOf(
        FavouriteRoom(
            id = 1L,
            roomName = "310-210 (Lecture Room)",
            building = "Building 310",
            floor = "2nd floor",
            nextFreeSlot = "15:00 - 17:00",
            isFreeNow = true
        ),
        FavouriteRoom(
            id = 2L,
            roomName = "310-220 (Lecture Room)",
            building = "Building 310",
            floor = "2nd floor",
            nextFreeSlot = "17:00 - 19:00",
            isFreeNow = false
        ),
        FavouriteRoom(
            id = 3L,
            roomName = "303-108 (Seminar Room)",
            building = "Building 303",
            floor = "1st floor",
            nextFreeSlot = "13:00 - 15:00",
            isFreeNow = true
        )
    )

    // userName → set of roomIds
    private val favouritesPerUser: MutableMap<String, MutableSet<Long>> = mutableMapOf()

    override suspend fun getFavourites(userName: String): List<FavouriteRoom> {
        val favIds = favouritesPerUser[userName] ?: emptySet()
        return allRooms.filter { it.id in favIds }
    }

    override suspend fun searchRooms(query: String): List<FavouriteRoom> {
        if (query.isBlank()) return emptyList()
        val q = query.trim()
        return allRooms.filter {
            it.roomName.contains(q, ignoreCase = true) ||
                    it.building.contains(q, ignoreCase = true) ||
                    it.floor.contains(q, ignoreCase = true)
        }
    }

    override suspend fun addFavourite(userName: String, roomId: Long) {
        val set = favouritesPerUser.getOrPut(userName) { mutableSetOf() }
        set.add(roomId)
    }

    override suspend fun removeFavourite(userName: String, roomId: Long) {
        favouritesPerUser[userName]?.remove(roomId)
    }
}

// ──────────────────────────────────────────────
// FAVOURITES SCREEN – with search bar + backend-ready repo
// ──────────────────────────────────────────────

@Composable
fun FavouritesScreen(
    userName: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }
    val notificationRepo = remember { SharedPrefsNotificationRepository(context) }

    // Backend-ready repository – later you can inject a real implementation
    val favouritesRepository = remember { InMemoryFavouriteRoomRepository() }
    val scope = rememberCoroutineScope()

    var favouriteRooms by remember { mutableStateOf<List<FavouriteRoom>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<FavouriteRoom>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    // Load current favourites from "backend" when screen opens
    LaunchedEffect(userName) {
        favouriteRooms = favouritesRepository.getFavourites(userName)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopBlueHeader(
            title = "Classroom Informer",
            showBackButton = true,
            onBackClick = onBackClick
        )

        // ── Title row ───────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Favourites",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        // ── Search bar for adding favourite rooms ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                label = { Text("Search room (e.g. 310-210, 303)") },
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            isSearching = true
                            searchResults = favouritesRepository.searchRooms(searchQuery)
                            isSearching = false
                        }
                    },
                    enabled = searchQuery.isNotBlank()
                ) {
                    Text("Search")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Search results list (for adding favourites) ──
        if (isSearching) {
            Text(
                text = "Searching...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        } else if (searchResults.isNotEmpty()) {
            Text(
                text = "Search Results",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp) // keep it smaller than whole screen
                    .padding(horizontal = 12.dp)
            ) {
                items(searchResults) { room ->
                    val alreadyFavourite = favouriteRooms.any { it.id == room.id }

                    SearchResultRoomCard(
                        room = room,
                        isAlreadyFavourite = alreadyFavourite,
                        onAddClick = {
                            if (!alreadyFavourite) {
                                scope.launch {
                                    favouritesRepository.addFavourite(userName, room.id)
                                    favouriteRooms = favouritesRepository.getFavourites(userName)
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Favourite rooms list (existing section) ──
        Text(
            text = "Your Favourite Rooms",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            items(favouriteRooms) { room ->
                FavouriteRoomCard(
                    room = room,
                    onCheckClick = {
                        if (room.isFreeNow) {
                            val message =
                                "${room.roomName} is now available (${room.nextFreeSlot})."

                            // 1) Save into notification history
                            val item = NotificationItem(
                                id = System.currentTimeMillis(),
                                title = "Favorite room became free",
                                message = message,
                                timestamp = System.currentTimeMillis()
                            )
                            notificationRepo.addNotification(userName, item)

                            // 2) Show system notification
                            notificationHelper.notifyFavoriteRoomAvailable(
                                userName = userName,
                                roomName = room.roomName
                            )
                        }
                    },
                    onRemoveClick = {
                        scope.launch {
                            favouritesRepository.removeFavourite(userName, room.id)
                            favouriteRooms = favouritesRepository.getFavourites(userName)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

// ──────────────────────────────────────────────
// Card used in SEARCH RESULT list
// ──────────────────────────────────────────────

@Composable
private fun SearchResultRoomCard(
    room: FavouriteRoom,
    isAlreadyFavourite: Boolean,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = room.roomName,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            Text(
                text = "${room.building} • ${room.floor}",
                style = MaterialTheme.typography.bodySmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onAddClick,
                    enabled = !isAlreadyFavourite
                ) {
                    Text(
                        if (isAlreadyFavourite) "Already added"
                        else "Add to favourites"
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────
// Card used in FAVOURITES list (기존 카드)
// ──────────────────────────────────────────────

@Composable
private fun FavouriteRoomCard(
    room: FavouriteRoom,
    onCheckClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = room.roomName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = "${room.building} • ${room.floor}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Next free slot ${room.nextFreeSlot}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onRemoveClick) {
                    Text("Remove from favourites")
                }

                Card(
                    modifier = Modifier
                        .widthIn(min = 90.dp)
                        .clickable(onClick = onCheckClick),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (room.isFreeNow)
                            Color(0xFF3D8BFF) else Color(0xFFB0B5C0)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Check !",
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

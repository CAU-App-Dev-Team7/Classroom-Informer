package com.example.classroominformer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.classroominformer.ui.MapScreen
import com.example.classroominformer.ui.ReservationSearchScreen
import com.example.classroominformer.ui.RoomDetailScreen
import com.example.classroominformer.ui.RoomsListScreen
import com.example.classroominformer.ui.SearchScreen
import com.example.classroominformer.ui.SeminarRoomDetailScreen
import com.example.classroominformer.ui.SeminarRoomsListScreen
import com.example.classroominformer.ui.TimetableScreen
import com.example.classroominformer.ui.home.ProfessorMainScreen
import com.example.classroominformer.ui.home.StudentMainScreen
import com.example.classroominformer.ui.login.LoginScreen
import com.example.classroominformer.ui.theme.ClassroomInformerTheme

@Composable
fun ClassroomInformerApp() {
    val navController = rememberNavController()

    ClassroomInformerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(
                navController = navController,
                startDestination = "login"
            ) {
                // ---------------- LOGIN ----------------
                composable("login") {
                    LoginScreen(
                        onLoginClick = {
                            // TODO: replace this with real login / role logic.
                            // For now we go to PROFESSOR home (matches your screenshot).
                            navController.navigate("professorHome") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }

                // -------------- STUDENT HOME --------------
                composable("studentHome") {
                    StudentMainScreen(
                        onSearchClick = { navController.navigate("search") },
                        onTimetableClick = { navController.navigate("timetable") },
                        onFavouritesClick = {
                            // TODO: hook up Favourites screen when you create it
                        },
                        onMapClick = { navController.navigate("map") },
                        onNotificationsClick = {
                            // TODO: hook up Notifications screen when you create it
                        },
                        onLogoutClick = {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                // ------------- PROFESSOR HOME -------------
                composable("professorHome") {
                    ProfessorMainScreen(
                        onSearchClick = { navController.navigate("search") },
                        onTimetableClick = { navController.navigate("timetable") },
                        onFavouritesClick = {
                            // TODO: connect favourites later
                        },
                        onReservationsClick = {
                            navController.navigate("reservationSearch")
                        },
                        onMapClick = { navController.navigate("map") },
                        onNotificationsClick = {
                            // TODO: connect notifications later
                        },
                        onLogoutClick = {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                // ----------------- MAP -------------------
                composable("map") {
                    MapScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                // ---------------- SEARCH -----------------
                composable("search") {
                    SearchScreen(
                        onSearchComplete = { selectedSlots ->
                            // You can store selectedSlots somewhere if you want later.
                            navController.navigate("roomsList")
                        }
                    )
                }

                // ------------- ROOMS LIST (SHARED) -------------
                composable("roomsList") {
                    RoomsListScreen(
                        onBack = { navController.popBackStack() },
                        onRoomClick = { roomId ->
                            // Pass the selected room via SavedStateHandle
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("roomId", roomId)

                            navController.navigate("roomDetail")
                        }
                    )
                }

                // ----------- STUDENT ROOM DETAIL -----------
                composable("roomDetail") {
                    val roomId = navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.get<String>("roomId") ?: "Room"

                    RoomDetailScreen(
                        roomId = roomId,
                        onBack = { navController.popBackStack() }
                    )
                }

                // --------- PROFESSOR RESERVATION FLOW ---------
                composable("reservationSearch") {
                    ReservationSearchScreen(
                        onSearchDone = {
                            navController.navigate("seminarRooms")
                        }
                    )
                }

                composable("seminarRooms") {
                    SeminarRoomsListScreen(
                        onBack = { navController.popBackStack() },
                        onRoomClick = { roomId ->
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("roomId", roomId)

                            navController.navigate("seminarRoomDetail")
                        }
                    )
                }

                composable("seminarRoomDetail") {
                    val roomId = navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.get<String>("roomId") ?: "Seminar Room"

                    SeminarRoomDetailScreen(
                        roomId = roomId,
                        onBack = { navController.popBackStack() }
                    )
                }

                // ---------------- TIMETABLE ----------------
                composable("timetable") {
                    TimetableScreen()   // ok even if it's just a placeholder for now
                }
            }
        }
    }
}



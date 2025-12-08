package com.example.classroominformer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.classroominformer.ui.*
import com.example.classroominformer.ui.home.*
import com.example.classroominformer.ui.login.LoginScreen
import com.example.classroominformer.ui.login.UserRole
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

                // -------- LOGIN --------
                composable("login") {
                    LoginScreen { email: String, _password: String, role: UserRole ->

                        val userName = if (email.isNotBlank()) {
                            email.substringBefore("@")
                        } else "guest"

                        when (role) {
                            UserRole.Professor -> {
                                navController.navigate("professorMain/$userName") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }

                            UserRole.Student -> {
                                navController.navigate("studentMain/$userName") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        }
                    }
                }

                // -------- STUDENT MAIN --------
                composable("studentMain/{userName}") { backStackEntry ->
                    val userName = backStackEntry.arguments?.getString("userName") ?: "guest"

                    StudentMainScreen(
                        userName = userName,
                        onSearchClick = {
                            navController.navigate("search/student/$userName")
                        },
                        onTimetableClick = {
                            navController.navigate("timetable_student/$userName")
                        },
                        onFavouritesClick = {
                            navController.navigate("favourites/$userName")
                        },
                        onMapClick = { navController.navigate("map") },
                        onNotificationsClick = {
                            navController.navigate("notifications/$userName")
                        },
                        onLogoutClick = {
                            navController.navigate("login") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }

                // -------- PROFESSOR MAIN --------
                composable("professorMain/{userName}") { backStackEntry ->
                    val userName = backStackEntry.arguments?.getString("userName") ?: "guest"

                    ProfessorMainScreen(
                        userName = userName,
                        onSearchClick = {
                            // normal search (same as student)
                            navController.navigate("search/student/$userName")
                        },
                        onTimetableClick = {
                            navController.navigate("timetable_professor/$userName")
                        },
                        onFavouritesClick = {
                            navController.navigate("favourites/$userName")
                        },
                        onReservationsClick = {
                            // 🔹 RESERVATION ICON → same SearchScreen but reservation mode
                            navController.navigate("search/prof_reservation/$userName")
                        },
                        onMapClick = { navController.navigate("map") },
                        onNotificationsClick = {
                            navController.navigate("notifications/$userName")
                        },
                        onLogoutClick = {
                            navController.navigate("login") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }

                // -------- MAP --------
                composable("map") {
                    MapScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                // -------- SEARCH (STUDENT) --------
                composable("search/student/{userName}") { _ ->
                    SearchScreen(
                        onBack = { navController.popBackStack() },
                        isReservationMode = false
                    ) { selectedSlots: List<String> ->
                        // TODO: pass selectedSlots to RoomsList if needed
                        navController.navigate("roomsList")
                    }
                }

                // -------- SEARCH (PROFESSOR RESERVATION) --------
                composable("search/prof_reservation/{userName}") { _ ->
                    SearchScreen(
                        onBack = { navController.popBackStack() },
                        isReservationMode = true
                    ) { selectedSlots: List<String> ->
                        // TODO: here later call reservation API using selectedSlots
                        navController.navigate("roomsList")
                    }
                }

                // -------- ROOM LIST --------
                composable("roomsList") {
                    RoomsListScreen(
                        onBack = { navController.popBackStack() },
                        onRoomClick = { roomId: String ->
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("roomId", roomId)

                            navController.navigate("roomDetail")
                        }
                    )
                }

                // -------- ROOM DETAIL --------
                composable("roomDetail") {
                    val roomId = navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.get<String>("roomId") ?: "Room"

                    RoomDetailScreen(
                        roomId = roomId,
                        onBack = { navController.popBackStack() }
                    )
                }

                // -------- STUDENT TIMETABLE --------
                composable("timetable_student/{userName}") { backStackEntry ->
                    val userName = backStackEntry.arguments?.getString("userName") ?: "guest"

                    TimetableScreen(
                        userName = userName,
                        onBackClick = { navController.popBackStack() },
                        onEmptySlotClick = {
                            navController.navigate("search/student/$userName")
                        }
                    )
                }

                // -------- PROFESSOR TIMETABLE --------
                composable("timetable_professor/{userName}") { backStackEntry ->
                    val userName = backStackEntry.arguments?.getString("userName") ?: "guest"

                    TimetableScreen(
                        userName = userName,
                        onBackClick = { navController.popBackStack() },
                        onEmptySlotClick = {
                            navController.navigate("search/prof_reservation/$userName")
                        }
                    )
                }

                // -------- FAVOURITES --------
                composable("favourites/{userName}") { backStackEntry ->
                    val userName = backStackEntry.arguments?.getString("userName") ?: "guest"

                    FavouritesScreen(
                        userName = userName,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // -------- NOTIFICATIONS --------
                composable("notifications/{userName}") { backStackEntry ->
                    val userName = backStackEntry.arguments?.getString("userName") ?: "guest"

                    NotificationsScreen(
                        userName = userName,
                        onBackClick = { navController.popBackStack() },
                        onNotificationClick = { notification ->
                            navController.navigate(
                                "notificationDetail/$userName/${notification.id}"
                            )
                        }
                    )
                }

                // -------- NOTIFICATION DETAIL --------
                composable("notificationDetail/{userName}/{notificationId}") { backStackEntry ->
                    val userName = backStackEntry.arguments?.getString("userName") ?: "guest"
                    val notificationId = backStackEntry.arguments
                        ?.getString("notificationId")
                        ?.toLongOrNull() ?: -1L

                    NotificationDetailScreen(
                        userName = userName,
                        notificationId = notificationId,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

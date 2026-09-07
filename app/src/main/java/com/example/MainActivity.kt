package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.AppPreferences
import com.example.ui.CallScreen
import com.example.ui.HomeScreen
import com.example.ui.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppNavHost()
            }
        }
    }
}

@Composable
fun MainAppNavHost() {
    val context = LocalContext.current
    val navController = rememberNavController()

    // Request Audio & Camera permissions
    var permissionsGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        permissionsGranted = results.values.all { it }
    }

    LaunchedEffect(Unit) {
        if (!permissionsGranted) {
            permissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
                )
            )
        }
    }

    val startDestination = remember {
        if (AppPreferences.isConfigured(context)) "home" else "settings"
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("settings") {
                SettingsScreen(
                    onSaved = {
                        navController.navigate("home") {
                            popUpTo("settings") { inclusive = true }
                        }
                    }
                )
            }

            composable("home") {
                HomeScreen(
                    onLetterSelected = { char, name ->
                        val encodedChar = URLEncoder.encode(char, StandardCharsets.UTF_8.toString())
                        val encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString())
                        navController.navigate("call/$encodedChar/$encodedName")
                    },
                    onOpenSettings = {
                        navController.navigate("settings")
                    }
                )
            }

            composable(
                route = "call/{letterChar}/{letterName}",
                arguments = listOf(
                    navArgument("letterChar") { type = NavType.StringType },
                    navArgument("letterName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val encodedChar = backStackEntry.arguments?.getString("letterChar") ?: "أ"
                val encodedName = backStackEntry.arguments?.getString("letterName") ?: "ألف"
                val letterChar = URLDecoder.decode(encodedChar, StandardCharsets.UTF_8.toString())
                val letterName = URLDecoder.decode(encodedName, StandardCharsets.UTF_8.toString())

                CallScreen(
                    letterChar = letterChar,
                    letterName = letterName,
                    onEndCall = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

// Retained for backwards compatibility with baseline tests
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

package com.magicmirror.vton.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import com.magicmirror.vton.AppContainer
import com.magicmirror.vton.camera.UvcCameraController
import com.magicmirror.vton.ui.attract.AttractScreen
import com.magicmirror.vton.ui.capture.CaptureScreen
import com.magicmirror.vton.ui.catalog.CatalogScreen
import com.magicmirror.vton.ui.result.ResultScreen
import kotlinx.coroutines.delay

private const val IDLE_TIMEOUT_MS = 90_000L

@Composable
fun MagicMirrorNavHost(appContainer: AppContainer, cameraController: UvcCameraController) {
    val navController = rememberNavController()
    var lastInteractionAt by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Any touch anywhere resets the idle clock without stealing the event from
    // whichever composable would otherwise handle it: PointerEventPass.Initial
    // observes on the way down, before a child gets to consume it.
    val idleResetModifier = Modifier.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                awaitPointerEvent(PointerEventPass.Initial)
                lastInteractionAt = System.currentTimeMillis()
            }
        }
    }

    LaunchedEffect(navController) {
        while (true) {
            delay(5_000)
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            val idleFor = System.currentTimeMillis() - lastInteractionAt
            if (currentRoute != Destinations.ATTRACT && idleFor > IDLE_TIMEOUT_MS) {
                navController.popBackStack(Destinations.ATTRACT, inclusive = false)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Destinations.ATTRACT,
        modifier = Modifier.fillMaxSize().then(idleResetModifier),
    ) {
        composable(Destinations.ATTRACT) {
            AttractScreen(onStart = { navController.navigate(Destinations.CATALOG) })
        }
        composable(Destinations.CATALOG) {
            CatalogScreen(
                catalogRepository = appContainer.catalogRepository,
                onGarmentSelected = { garmentId -> navController.navigate(Destinations.capture(garmentId)) },
            )
        }
        composable(
            route = Destinations.CAPTURE,
            arguments = listOf(navArgument("garmentId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val garmentId = backStackEntry.arguments?.getString("garmentId").orEmpty()
            CaptureScreen(
                garmentId = garmentId,
                cameraController = cameraController,
                tryonRepository = appContainer.tryonRepository,
                onResult = { jobId -> navController.navigate(Destinations.result(jobId)) },
                onCancel = { navController.popBackStack() },
            )
        }
        composable(
            route = Destinations.RESULT,
            arguments = listOf(navArgument("jobId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId").orEmpty()
            ResultScreen(
                jobId = jobId,
                tryonRepository = appContainer.tryonRepository,
                onTryAnother = { navController.popBackStack(Destinations.CATALOG, inclusive = false) },
                onDone = { navController.popBackStack(Destinations.ATTRACT, inclusive = false) },
            )
        }
    }
}

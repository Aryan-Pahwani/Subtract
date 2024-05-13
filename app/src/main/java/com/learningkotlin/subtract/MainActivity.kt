package com.learningkotlin.subtract

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.learningkotlin.subtract.ui.theme.SubtractTheme

data class App(
    val name: String,
    val packageName: String,
    val icon: Drawable?
)

fun App.launch(context: Context) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return
    context.startActivity(intent)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SubtractTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    SetBarColors()
                    AppList()
                }
            }
        }
    }


}

@Composable
fun SetBarColors() {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()
    val statusBarLight = Color.White
    val statusBarDark = Color.Black
    val navigationBarLight = Color.White
    val navigationBarDark = Color.Black

    DisposableEffect(systemUiController, useDarkIcons) {
        systemUiController.setNavigationBarColor(
            color = if (useDarkIcons) statusBarLight else statusBarDark,
            darkIcons = useDarkIcons
        )
        systemUiController.setStatusBarColor(
            color = if (useDarkIcons) navigationBarLight else navigationBarDark,
            darkIcons = useDarkIcons
        )

        onDispose { /* Cleanup if needed */ }
    }
}


@Composable
fun AppList() {
    val context = LocalContext.current
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }

    val flags = PackageManager.ResolveInfoFlags.of(
        PackageManager.MATCH_ALL.toLong()
    )
    val activities: List<ResolveInfo> =
        context.packageManager.queryIntentActivities(intent, flags)

    val installedApps = activities.map { resolveInfo ->
        App(
            name = context.packageManager.getApplicationLabel(resolveInfo.activityInfo.applicationInfo).toString(),
            packageName = resolveInfo.activityInfo.packageName,
            icon = resolveInfo.loadIcon(context.packageManager)
        )
    }


    val sortedApps = installedApps.sortedBy { it.name }
    LazyColumn (modifier=Modifier
        .fillMaxWidth()
    ) {
        items(sortedApps) { app ->
            AppRow(app=app, context=context)
        }
    }
}



@Composable
fun AppRow(app: App, context: Context) {
    val interactionSource = remember { MutableInteractionSource() } // Use val instead of var
    val rippleColour = if (isSystemInDarkTheme()) Color.White else Color.Black
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = false, color = rippleColour)
            ) {
                app.launch(context) // This should be inside the clickable lambda
            }
            .padding(16.dp)
    ) {
        AppItem(app = app)
    }
}

@Composable
fun AppItem(app: App) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        app.icon?.toBitmap()?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "App Icon",
                modifier = Modifier.size(48.dp),
                colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
            )

        }
        Text(text=app.name, color = Color.White)
    }
}
package net.bonysoft.bonusstage.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.coroutines.flow.first
import net.bonysoft.bonusstage.data.widget.WidgetData
import net.bonysoft.bonusstage.data.widget.WidgetDataSource
import net.bonysoft.bonusstage.data.widget.WidgetUpdater
import net.bonysoft.bonusstage.shared.R
import net.bonysoft.bonusstage.ui.common.relativeTo
import net.bonysoft.bonusstage.ui.common.splitTitleTag
import net.bonysoft.bonusstage.ui.theme.BonusStageColors
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/** Images for the widget, decoded once per update (RemoteViews need bitmaps). */
private data class WidgetContent(val data: WidgetData, val images: Map<String, Bitmap>)

/**
 * Home screen widget: the game being played with its progress and the latest unlocks, drawn as a
 * game window. Reads the cache only; WidgetSync redraws it when the cache changes.
 */
class BonusStageWidget : GlanceAppWidget(), KoinComponent {

    override val sizeMode = SizeMode.Responsive(setOf(SMALL, LARGE))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val source = get<WidgetDataSource>()
        val loader = ImageLoader(context)
        val initial = source.observe().first().withImages(context, loader)
        provideContent {
            val content by produceState(initial) {
                source.observe().collect { value = it.withImages(context, loader) }
            }
            GlanceTheme { Widget(content) }
        }
    }

    private suspend fun WidgetData.withImages(context: Context, loader: ImageLoader): WidgetContent {
        val urls = listOfNotNull(currentGame?.iconUrl) + recent.map { it.badgeUrl }
        val images = urls.distinct().mapNotNull { url ->
            val request = ImageRequest.Builder(context).data(url).size(IMAGE_PX).allowHardware(false).build()
            loader.execute(request).image?.toBitmap()?.let { url to it }
        }.toMap()
        return WidgetContent(this, images)
    }

    companion object {
        private val SMALL = DpSize(160.dp, 110.dp)
        private val LARGE = DpSize(250.dp, 200.dp)
        private const val IMAGE_PX = 96
    }
}

class BonusStageWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = BonusStageWidget()
}

class GlanceWidgetUpdater(private val context: Context) : WidgetUpdater {
    override suspend fun update() = BonusStageWidget().updateAll(context)
}

private val colors = BonusStageColors
private fun Color.provider() = ColorProvider(this)
private val pixelFont = FontFamily.Monospace

@Composable
private fun Widget(content: WidgetContent) {
    val data = content.data
    val large = LocalSize.current.height >= 180.dp
    val context = LocalContext.current
    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_window))
            .padding(12.dp)
            .then(if (launchIntent != null) GlanceModifier.clickable(actionStartActivity(launchIntent)) else GlanceModifier),
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "BONUS STAGE",
                    style = TextStyle(color = colors.accent.provider(), fontFamily = pixelFont, fontWeight = FontWeight.Bold, fontSize = 12.sp),
                    modifier = GlanceModifier.defaultWeight(),
                )
                data.username?.let { Text(it, style = TextStyle(color = colors.onWindowMuted.provider(), fontFamily = pixelFont, fontSize = 11.sp)) }
            }
            Spacer(GlanceModifier.height(8.dp))
            when {
                !data.isSignedIn -> Message("Sign in to see your progress.")
                data.currentGame == null -> Message("Play something with RetroAchievements!")
                else -> {
                    CurrentGame(content)
                    if (large && data.recent.isNotEmpty()) {
                        Spacer(GlanceModifier.height(10.dp))
                        RecentList(content)
                    }
                }
            }
        }
    }
}

/** In its own Column: Glance allows at most 10 children per container. */
@Composable
private fun RecentList(content: WidgetContent) {
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        Text("RECENT", style = TextStyle(color = colors.onWindowMuted.provider(), fontFamily = pixelFont, fontWeight = FontWeight.Bold, fontSize = 11.sp))
        content.data.recent.forEach { unlock ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = GlanceModifier.fillMaxWidth().padding(top = 4.dp)) {
                Picture(content.images[unlock.badgeUrl], 28)
                Spacer(GlanceModifier.width(8.dp))
                Text(
                    unlock.title,
                    maxLines = 1,
                    style = TextStyle(color = colors.onWindow.provider(), fontFamily = pixelFont, fontSize = 12.sp),
                    modifier = GlanceModifier.defaultWeight(),
                )
                Text(
                    "${unlock.points} · ${unlock.unlockedAt.relativeTo()}",
                    style = TextStyle(color = colors.onWindowMuted.provider(), fontFamily = pixelFont, fontSize = 11.sp),
                )
            }
        }
    }
}

@Composable
private fun CurrentGame(content: WidgetContent) {
    val game = content.data.currentGame ?: return
    Row(verticalAlignment = Alignment.CenterVertically, modifier = GlanceModifier.fillMaxWidth()) {
        Picture(content.images[game.iconUrl], 44)
        Spacer(GlanceModifier.width(10.dp))
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                splitTitleTag(game.title).second,
                maxLines = 1,
                style = TextStyle(color = colors.onWindow.provider(), fontFamily = pixelFont, fontWeight = FontWeight.Bold, fontSize = 13.sp),
            )
            Spacer(GlanceModifier.height(4.dp))
            LinearProgressIndicator(
                progress = game.progress,
                color = colors.progress.provider(),
                backgroundColor = colors.track.provider(),
                modifier = GlanceModifier.fillMaxWidth().height(8.dp),
            )
            Spacer(GlanceModifier.height(2.dp))
            Text(
                "${game.unlocked} / ${game.achievementsTotal} · ${(game.progress * 100).toInt()}%",
                style = TextStyle(color = colors.onWindowMuted.provider(), fontFamily = pixelFont, fontSize = 11.sp),
            )
        }
    }
}

@Composable
private fun Picture(bitmap: Bitmap?, sizeDp: Int) {
    if (bitmap != null) {
        Image(ImageProvider(bitmap), contentDescription = null, modifier = GlanceModifier.size(sizeDp.dp))
    } else {
        Image(
            ImageProvider(R.drawable.ic_notification),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colors.track.provider()),
            modifier = GlanceModifier.size(sizeDp.dp),
        )
    }
}

@Composable
private fun Message(text: String) {
    Text(text, style = TextStyle(color = colors.onWindow.provider(), fontFamily = pixelFont, fontSize = 12.sp))
}

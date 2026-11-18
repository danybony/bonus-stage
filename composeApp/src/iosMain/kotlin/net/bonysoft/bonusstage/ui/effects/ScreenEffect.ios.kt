package net.bonysoft.bonusstage.ui.effects

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

private val runtimeEffect by lazy { RuntimeEffect.makeForShader(SCREEN_EFFECT_SHADER) }

/** Skia runtime shader (SkSL); Skia renders through Metal on iOS. */
actual fun Modifier.screenEffect(effect: ScreenEffect): Modifier {
    if (effect == ScreenEffect.Off) return this
    val builder = RuntimeShaderBuilder(runtimeEffect)
    return graphicsLayer {
        builder.uniform("resolution", size.width, size.height)
        builder.uniform("mode", effect.shaderMode())
        renderEffect = ImageFilter.makeRuntimeShader(builder, "content", null).asComposeRenderEffect()
    }
}

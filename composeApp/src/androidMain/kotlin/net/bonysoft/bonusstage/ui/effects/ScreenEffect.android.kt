package net.bonysoft.bonusstage.ui.effects

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer

actual fun Modifier.screenEffect(effect: ScreenEffect): Modifier = when {
    effect == ScreenEffect.Off -> this
    // AGSL runtime shaders arrived in Android 13.
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> shaderEffect(effect)
    else -> overlayEffect(effect)
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun Modifier.shaderEffect(effect: ScreenEffect): Modifier {
    val shader = RuntimeShader(SCREEN_EFFECT_SHADER)
    return graphicsLayer {
        shader.setFloatUniform("resolution", size.width, size.height)
        shader.setFloatUniform("mode", effect.shaderMode())
        renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "content").asComposeRenderEffect()
    }
}

package co.rikin.shaderbuttons.shaders

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.rikin.shaderbuttons.ui.theme.ShaderButtonsTheme

private const val chromaticShader = """
  uniform shader composable;
  uniform float2 size;
  uniform float offset;
  
  half4 main(float2 fragCoord) {
    float move = offset;
    
    half3 color = composable.eval(fragCoord).rgb;
    color.r = composable.eval(float2(fragCoord.x - move, fragCoord.y)).r;
    color.b = composable.eval(float2(fragCoord.x + move, fragCoord.y)).b;
    
    return half4(color, 1.0);
  }
"""

@Preview
@Composable
fun ChromaticButton() {
  ShaderButtonsTheme {
    val shifter = remember { Animatable(0f) }
    val shader = remember { RuntimeShader(chromaticShader) }

    Box(
      modifier = Modifier
        .graphicsLayer {
          shader.setFloatUniform(
            "offset",
            shifter.value
          )
          renderEffect =
            RenderEffect
              .createRuntimeShaderEffect(shader, "composable")
              .asComposeRenderEffect()
        }
        .fillMaxWidth()
        .aspectRatio(1f)
        .background(color = Color.White),
      contentAlignment = Alignment.Center
    ) {
      Box(
        modifier = Modifier
          .width(200.dp)
          .height(80.dp)
          .clip(CircleShape)
          .background(color = Color.Black)
          .pointerInput(Unit) {
            detectTapGestures(onPress = {
              shifter.animateTo(20f)
              awaitRelease()
              shifter.animateTo(0f)
            })
          },
        contentAlignment = Alignment.Center
      ) {
        Text("◻️", fontSize = 20.sp)
      }
    }
  }
}
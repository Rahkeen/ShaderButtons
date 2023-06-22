package co.rikin.shaderbuttons.shaders

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.rikin.shaderbuttons.ui.theme.Jedi
import co.rikin.shaderbuttons.ui.theme.ShaderButtonsTheme
import org.intellij.lang.annotations.Language

@Language("AGSL")
val glowingButtonShader = """
  uniform shader button;
  uniform float2 size;
  uniform float radius;
  uniform float glowRadius;
  uniform float glowIntensity;
  layout(color) uniform half4 glowColor;
  
  float roundRectSDF(vec2 position, vec2 box, float radius) {
      vec2 q = abs(position) - box + radius;
      return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius;   
  }
  
  float getGlow(float dist, float radius, float intensity){
      return pow(radius/dist, intensity);
  }
  
  half4 main(float2 coord) {
      float ratio = size.y / size.x;
      float2 pos = coord.xy / size;
      pos.y = pos.y * ratio;
      
      float2 normRect = float2(1.0, ratio);
      float2 normRectCenter = normRect / 2.0;
      pos = pos - normRectCenter;
      float normRadius = ratio / 2.0;
      float normDistance = roundRectSDF(pos, normRectCenter, normRadius);
      
      half4 color = button.eval(coord);
      if (normDistance < 0.0) {
        return color;
      } 
      
      // Add some glow
      float glow = getGlow(normDistance, glowRadius, glowIntensity);
      color = glow * glowColor;
      
      // tonemapping
      color = 1.0 - exp(-color);
      
      return color;
  }
""".trimIndent()

@Composable
fun GlowingButton(
  glowColor: Color,
  text: String,
  backgroundColor: Color = Color.White,
  textColor: Color = Color.Black,
  radius: Float = 0.3f,
  intensity: Float = 0.5f
) {
  ShaderButtonsTheme {
    val shader = remember { RuntimeShader(glowingButtonShader) }
    var width by remember { mutableStateOf(0f) }
    var height by remember { mutableStateOf(0f) }

    val interactionSource = remember { MutableInteractionSource() }

    shader.setColorUniform(
      "glowColor",
      glowColor.toArgb()
    )

    shader.setFloatUniform(
      "glowRadius",
      radius
    )

    shader.setFloatUniform(
      "glowIntensity",
      intensity
    )
    Box(
      modifier = Modifier
        .graphicsLayer {
          with(shader) {
            setFloatUniform(
              "size",
              width,
              height
            )
            setFloatUniform(
              "radius",
              30.dp.toPx()
            )
          }
          renderEffect = RenderEffect
            .createRuntimeShaderEffect(shader, "button")
            .asComposeRenderEffect()

        }
        .width(200.dp)
        .height(80.dp)
        .background(color = backgroundColor, shape = RoundedCornerShape(30.dp))
        .clip(RoundedCornerShape(30.dp))
        .clickable(interactionSource = interactionSource, indication = null) {}
        .onSizeChanged { size ->
          width = size.width.toFloat()
          height = size.height.toFloat()
        },
      contentAlignment = Alignment.Center
    ) {
      Text(text, style = MaterialTheme.typography.labelLarge, color = textColor)
    }
  }
}

@Preview
@Composable
fun JediGlowingButton() {
  ShaderButtonsTheme {
    Box(modifier = Modifier
      .fillMaxWidth()
      .aspectRatio(1f), contentAlignment = Alignment.Center) {
      GlowingButton(glowColor = Jedi, "Hello")
    }
  }
}

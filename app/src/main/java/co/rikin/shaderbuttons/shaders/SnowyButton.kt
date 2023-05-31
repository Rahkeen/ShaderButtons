package co.rikin.shaderbuttons.shaders

import android.graphics.RuntimeShader
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.rikin.shaderbuttons.SimpleSketchWithCache
import co.rikin.shaderbuttons.ui.theme.ShaderButtonsTheme
import org.intellij.lang.annotations.Language

@Language("AGSL")
private val swedenSnowShader = """
  uniform float2 resolution;
  uniform float time;
  
  half4 main(float2 coord) {
    float snow = 0.0;
    float ratio = resolution.y / resolution.x;
    float random = fract(sin(dot(coord.xy,vec2(12.9898,78.233)))* 43758.5453);
    for(int k=0;k<6;k++){
        for(int i=0;i<12;i++){
            float cellSize = 2.0 + (float(i)*3.0);
            float downSpeed = 0.3+(sin(time*0.4+float(k+i*20))+1.0)*0.00008;
            vec2 uv = vec2(coord.x/resolution.x, (1.0 - coord.y/resolution.y) * ratio)+vec2(0.01*sin((time+float(k*6185))*0.6+float(i))*(5.0/float(i)),downSpeed*(time+float(k*1352))*(1.0/float(i)));
            vec2 uvStep = (ceil((uv)*cellSize-vec2(0.5,0.5))/cellSize);
            float x = fract(sin(dot(uvStep.xy,vec2(12.9898+float(k)*12.0,78.233+float(k)*315.156)))* 43758.5453+float(k)*12.0)-0.5;
            float y = fract(sin(dot(uvStep.xy,vec2(62.2364+float(k)*23.0,94.674+float(k)*95.0)))* 62159.8432+float(k)*12.0)-0.5;

            float randomMagnitude1 = sin(time*2.5)*0.7/cellSize;
            float randomMagnitude2 = cos(time*2.5)*0.7/cellSize;

            float d = 5.0*distance((uvStep.xy + vec2(x*sin(y),y)*randomMagnitude1 + vec2(y,x)*randomMagnitude2),uv.xy);

            float omiVal = fract(sin(dot(uvStep.xy,vec2(32.4691,94.615)))* 31572.1684);
            if(omiVal<0.08?true:false){
                float newd = (x+1.0)*0.4*clamp(1.9-d*(15.0+(x*6.3))*(cellSize/1.4),0.0,1.0);
                snow += newd;
            }
        }
    }
    
    
    return half4(snow) + half4(0.0784,0.1294,0.2392,1.0) + random*0.01;
  }
""".trimIndent()

@Composable
fun SwedenSnowPlayground() {
  ShaderButtonsTheme {
    val shader = remember { RuntimeShader(swedenSnowShader) }

    SimpleSketchWithCache(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f)
    ) { time ->
      with(shader) {
        setFloatUniform(
          "resolution",
          size.width,
          size.height
        )

        setFloatUniform(
          "time",
          time.value
        )
      }

      onDrawBehind {
        drawRect(
          brush = ShaderBrush(shader)
        )
      }
    }
  }
}

@Preview
@Composable
fun SnowyButton() {
  ShaderButtonsTheme {
    val shader = remember { RuntimeShader(swedenSnowShader) }
    var time by remember { mutableStateOf(0f) }
    var width by remember { mutableStateOf(0f) }
    var height by remember { mutableStateOf(0f) }

    with(shader) {
      setFloatUniform("time", time)
      setFloatUniform("resolution", width, height)
    }

    LaunchedEffect(Unit) {
      do {
        withFrameMillis {
          time += 0.01f
        }
      } while (true)
    }

    Box(
      modifier = Modifier
        .width(250.dp)
        .height(100.dp)
        .clip(CircleShape)
        .background(brush = ShaderBrush(shader))
        .onSizeChanged { size ->
          width = size.width.toFloat()
          height = size.height.toFloat()
        },
      contentAlignment = Alignment.Center
    ) {
      Text(text = "❄️ it's cold", color = Color.White, fontSize = 18.sp)
    }
  }
}

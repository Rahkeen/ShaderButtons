package co.rikin.shaderbuttons.shaders

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.rikin.shaderbuttons.ui.theme.Clay
import co.rikin.shaderbuttons.ui.theme.Fairy
import co.rikin.shaderbuttons.ui.theme.Forest
import co.rikin.shaderbuttons.ui.theme.Greeny
import co.rikin.shaderbuttons.ui.theme.Jedi
import co.rikin.shaderbuttons.ui.theme.Moon
import co.rikin.shaderbuttons.ui.theme.Night
import co.rikin.shaderbuttons.ui.theme.Pink40
import co.rikin.shaderbuttons.ui.theme.Pink80
import co.rikin.shaderbuttons.ui.theme.Purple40
import co.rikin.shaderbuttons.ui.theme.ShaderButtonsTheme
import co.rikin.shaderbuttons.ui.theme.Terra
import co.rikin.shaderbuttons.ui.theme.VibrantPurp
import co.rikin.shaderbuttons.ui.theme.VibrantYellow
import org.intellij.lang.annotations.Language
import kotlin.math.cos
import kotlin.math.sin

@Language("AGSL")
val shader = """
uniform vec2 in_origin;
uniform vec2 in_touch;
uniform float in_progress;
uniform float in_maxRadius;
uniform vec2 in_resolutionScale;
uniform vec2 in_noiseScale;
uniform float in_hasMask;
uniform float in_noisePhase;
uniform float in_turbulencePhase;
uniform vec2 in_tCircle1;
uniform vec2 in_tCircle2;
uniform vec2 in_tCircle3;
uniform vec2 in_tRotation1;
uniform vec2 in_tRotation2;
uniform vec2 in_tRotation3;
layout(color) uniform vec4 in_color;
layout(color) uniform vec4 in_sparkleColor;
uniform shader in_shader;

float triangleNoise(vec2 n) {
  n = fract(n * vec2(5.3987, 5.4421));
  n += dot(n.yx, n.xy + vec2(21.5351, 14.3137));
  float xy = n.x * n.y;
  return fract(xy * 95.4307) + fract(xy * 75.04961) - 1.0;
}

const float PI = 3.1415926535897932384626;

float threshold(float v, float l, float h) {
    return step(l, v) * (1.0 - step(h, v));
}

float sparkles(vec2 uv, float t) {
  float n = triangleNoise(uv);
  float s = 0.0;
  for (float i = 0; i < 4; i += 1) {
    float l = i * 0.1;
    float h = l + 0.05;
    float o = sin(PI * (t + 0.35 * i));
    s += threshold(n + o, l, h);
  }
  return saturate(s) * in_sparkleColor.a;
}

float softCircle(vec2 uv, vec2 xy, float radius, float blur) {
  float blurHalf = blur * 0.5;
  float d = distance(uv, xy);
  return 1. - smoothstep(1. - blurHalf, 1. + blurHalf, d / radius);
}

float softRing(vec2 uv, vec2 xy, float radius, float progress, float blur) {
  float thickness = 0.05 * radius;
  float currentRadius = radius * progress;
  float circle_outer = softCircle(uv, xy, currentRadius + thickness, blur);
  float circle_inner = softCircle(uv, xy, max(currentRadius - thickness, 0.), blur);
  return saturate(circle_outer - circle_inner);
}

float subProgress(float start, float end, float progress) {
    float sub = clamp(progress, start, end);
    return (sub - start) / (end - start);
}

mat2 rotate2d(vec2 rad) {
  return mat2(rad.x, -rad.y, rad.y, rad.x);
}

float circle_grid(vec2 resolution, vec2 coord, float time, vec2 center,
    vec2 rotation, float cell_diameter) {
  coord = rotate2d(rotation) * (center - coord) + center;
  coord = mod(coord, cell_diameter) / resolution;
  float normal_radius = cell_diameter / resolution.y * 0.5;
  float radius = 0.65 * normal_radius;
  return softCircle(coord, vec2(normal_radius), radius, radius * 50.0);
}

float turbulence(vec2 uv, float t) {
  const vec2 scale = vec2(0.8);
  uv = uv * scale;
  float g1 = circle_grid(scale, uv, t, in_tCircle1, in_tRotation1, 0.17);
  float g2 = circle_grid(scale, uv, t, in_tCircle2, in_tRotation2, 0.2);
  float g3 = circle_grid(scale, uv, t, in_tCircle3, in_tRotation3, 0.275);
  float v = (g1 * g1 + g2 - g3) * 0.5;
  return saturate(0.45 + 0.8 * v);
}

vec4 main(vec2 p) {
    float fadeIn = subProgress(0., 0.13, in_progress);
    float scaleIn = subProgress(0., 1.0, in_progress);
    float fadeOutNoise = subProgress(0.4, 0.5, in_progress);
    float fadeOutRipple = subProgress(0.4, 1., in_progress);
    vec2 center = mix(in_touch, in_origin, saturate(in_progress * 2.0));
    float ring = softRing(p, center, in_maxRadius, scaleIn, 1.);
    float alpha = min(fadeIn, 1. - fadeOutNoise);
    vec2 uv = p * in_resolutionScale;
    vec2 densityUv = uv - mod(uv, in_noiseScale);
    float turbulence = turbulence(uv, in_turbulencePhase);
    float sparkleAlpha = sparkles(densityUv, in_noisePhase) * ring * alpha * turbulence;
    float fade = min(fadeIn, 1. - fadeOutRipple);
    float waveAlpha = softCircle(p, center, in_maxRadius * scaleIn, 1.) * fade * in_color.a;
    vec4 waveColor = vec4(in_color.rgb * waveAlpha, waveAlpha);
    vec4 sparkleColor = vec4(in_sparkleColor.rgb * in_sparkleColor.a, in_sparkleColor.a);
    float mask = in_hasMask == 1. ? in_shader.eval(p).a > 0. ? 1. : 0. : 1.;
    return mix(waveColor, sparkleColor, sparkleAlpha) * mask;
}
""".trimIndent()

@Language("AGSL")
private val sparkleShaderRewritten = """
  uniform vec2 in_origin;
  uniform vec2 in_touch;
  uniform float in_progress;
  uniform float in_maxRadius;
  uniform vec2 in_resolutionScale;
  uniform vec2 in_noiseScale;
  uniform float in_hasMask;
  uniform float in_noisePhase;
  uniform float in_turbulencePhase;
  uniform vec2 in_tCircle1;
  uniform vec2 in_tCircle2;
  uniform vec2 in_tCircle3;
  uniform vec2 in_tRotation1;
  uniform vec2 in_tRotation2;
  uniform vec2 in_tRotation3;
  layout(color) uniform vec4 in_color;
  layout(color) uniform vec4 in_sparkleColor;
  uniform shader in_shader;

  const float PI = 3.1415926535897932384626;

  float triangleNoise(vec2 n) {
    n = fract(n * vec2(5.3987, 5.4421));
    n += dot(n.yx, n.xy + vec2(21.5351, 14.3137));
    float xy = n.x * n.y;
    return fract(xy * 95.4307) + fract(xy * 75.04961) - 1.0;
  }

  float threshold(float v, float l, float h) {
      return step(l, v) * (1.0 - step(h, v));
  }

  float sparkles(vec2 uv, float t) {
    float n = triangleNoise(uv);
    float sparkleIntensity = 0.0;
    for (float i = 0.0; i < 4.0; i += 1.0) {
      float l = i * 0.1;
      float h = l + 0.05;
      float o = sin(PI * (t + 0.35 * i));
      sparkleIntensity += threshold(n + o, l, h);
    }
    return saturate(sparkleIntensity) * in_sparkleColor.a;
  }

  float softCircle(vec2 uv, vec2 xy, float radius, float blur) {
    float blurHalf = blur * 0.5;
    float d = distance(uv, xy);
    return 1.0 - smoothstep(1.0 - blurHalf, 1.0 + blurHalf, d / radius);
  }

  float softRing(vec2 uv, vec2 xy, float radius, float progress, float blur) {
    float thickness = 0.06 * radius;
    float currentRadius = radius * progress;
    float circleOuter = softCircle(uv, xy, currentRadius + thickness, blur);
    float circleInner = softCircle(uv, xy, max(currentRadius - thickness, 0.0), blur);
    return saturate(circleOuter - circleInner);
  }

  float subProgress(float start, float end, float progress) {
      float sub = clamp(progress, start, end);
      return (sub - start) / (end - start); 
  }

  mat2 rotate2d(vec2 rad) {
    return mat2(rad.x, -rad.y, rad.y, rad.x);
  }

  float circleGrid(vec2 resolution, vec2 coord, float time, vec2 center,
      vec2 rotation, float cellDiameter) {
    coord = rotate2d(rotation) * (center - coord) + center;
    coord = mod(coord, cellDiameter) / resolution;
    float normalRadius = cellDiameter / resolution.y * 0.5;
    float radius = 0.65 * normalRadius;
    return softCircle(coord, vec2(normalRadius), radius, radius * 50.0);
  }

  float turbulence(vec2 uv, float t) {
    const vec2 scale = vec2(0.8);
    uv *= scale;
    float g1 = circleGrid(scale, uv, t, in_tCircle1, in_tRotation1, 0.17);
    float g2 = circleGrid(scale, uv, t, in_tCircle2, in_tRotation2, 0.2);
    float g3 = circleGrid(scale, uv, t, in_tCircle3, in_tRotation3, 0.275);
    float v = (g1 * g1 + g2 - g3) * 0.5;
    return saturate(0.45 + 0.8 * v);
  }
  
  half3 palette(float t) {
      float3 a = float3(0.5, 0.5, 0.5);
      float3 b = float3(0.5, 0.5, 0.5);
      float3 c = float3(1.0, 1.0, 1.0);
      float3 d = float3(0.00, 0.33, 0.67);
      return a + b * cos(6.28318 * (c * t + d));
  }

  vec4 main(vec2 p) {
      float fadeIn = subProgress(0.0, 0.13, in_progress);
      float scaleIn = subProgress(0.0, 1.0, in_progress);
      float fadeOutNoise = subProgress(0.4, 0.5, in_progress);
      float fadeOutRipple = subProgress(0.4, 1.0, in_progress);
      vec2 center = mix(in_touch, in_origin, saturate(in_progress * 2.0));
      float ring = softRing(p, center, in_maxRadius, scaleIn, 1.0);
      float alpha = min(fadeIn, 1.0 - fadeOutNoise);
      vec2 uv = p * in_resolutionScale;
      vec2 densityUv = uv - mod(uv, in_noiseScale);
      float turbulenceIntensity = turbulence(uv, in_turbulencePhase);
      float sparkleAlpha = sparkles(densityUv, in_noisePhase) * ring * alpha * turbulenceIntensity;
      float fade = min(fadeIn, 1.0 - fadeOutRipple);
      float waveAlpha = softCircle(p, center, in_maxRadius * scaleIn, 1.0) * fade * in_color.a;
      vec4 waveColor = vec4(in_color.rgb * waveAlpha, waveAlpha);
      vec4 sparkleColor = vec4(in_sparkleColor.rgb * in_sparkleColor.a, in_sparkleColor.a);
      float mask = in_hasMask == 1.0 ? in_shader.eval(p).a > 0.0 ? 1.0 : 0.0 : 1.0;
      return mix(waveColor, sparkleColor, sparkleAlpha) * mask;
  }
""".trimIndent()

private const val PI_ROTATE_RIGHT = Math.PI * 0.0078125
private const val PI_ROTATE_LEFT = Math.PI * -0.0078125

private fun RuntimeShader.setNoisePhase(phase: Float) {
  val scale = 1.5f

  setFloatUniform("in_noisePhase", phase)
  setFloatUniform("in_turbulencePhase", phase)
  setFloatUniform(
    "in_tCircle1",
    (scale * 0.5 + phase * 0.01 * cos(scale * 0.55)).toFloat(),
    (scale * 0.5 + phase * 0.01 * sin(
      scale * 0.55
    )).toFloat()
  )
  setFloatUniform(
    "in_tCircle2",
    (scale * 0.2 + phase * -0.0066 * cos(scale * 0.45)).toFloat(),
    (scale * 0.2 + phase * -0.0066 * sin(
      scale * 0.45
    )).toFloat()
  )
  setFloatUniform(
    "in_tCircle3",
    (scale + phase * -0.0066 * cos(scale * 0.35)).toFloat(),
    (scale + phase * -0.0066 * sin(
      scale * 0.35
    )).toFloat()
  )
  val rotation1: Double = phase * PI_ROTATE_RIGHT + 1.7 * Math.PI
  setFloatUniform("in_tRotation1", cos(rotation1).toFloat(), sin(rotation1).toFloat())
  val rotation2: Double = phase * PI_ROTATE_LEFT + 2 * Math.PI
  setFloatUniform("in_tRotation2", cos(rotation2).toFloat(), sin(rotation2).toFloat())
  val rotation3: Double = phase * PI_ROTATE_RIGHT + 2.75 * Math.PI
  setFloatUniform("in_tRotation3", cos(rotation3).toFloat(), sin(rotation3).toFloat())
}

@Preview
@Composable
fun SparkleRipplePlayground() {
  val shader = remember { RuntimeShader(sparkleShaderRewritten) }
  var width by remember { mutableStateOf(0f) }
  var height by remember { mutableStateOf(0f) }
  var noisePhase by remember { mutableStateOf(0f) }
  val effectColor = remember { Greeny }
  val sparkleColor = remember { VibrantYellow }

  val progress = remember {
    Animatable(0f)
  }

  LaunchedEffect(Unit) {
    withFrameMillis {
      noisePhase = it * 0.001f
    }
  }

  ShaderButtonsTheme {
    Box(
      Modifier
        .fillMaxWidth()
        .aspectRatio(1f)
        .background(color = MaterialTheme.colorScheme.background),
      contentAlignment = Alignment.Center
    ) {
      Box(
        modifier = Modifier
          .width(200.dp)
          .height(80.dp)
          .clip(CircleShape)
          .background(Color.DarkGray)
          .onSizeChanged { size ->
            width = size.width.toFloat()
            height = size.height.toFloat()
          },
        contentAlignment = Alignment.Center
      ) {
        Text("💖", color = Night, fontSize = 20.sp)
      }
      Box(
        modifier = Modifier
          .graphicsLayer {
            shape = CircleShape
            clip = true

            with(shader) {
              setColorUniform(
                "in_color",
                effectColor
                  .copy(alpha = 0.5f)
                  .toArgb()
              )
              setColorUniform(
                "in_sparkleColor",
                sparkleColor.toArgb()
              )
              setFloatUniform(
                "in_origin",
                width / 2f,
                height / 2f
              )
              setFloatUniform(
                "in_resolutionScale",
                1f / width,
                1f / height
              )
              setFloatUniform(
                "in_noiseScale",
                2.1f / width,
                2.1f / height
              )
              setNoisePhase(noisePhase)
              setFloatUniform(
                "in_maxRadius",
                width
              )
              setFloatUniform(
                "in_progress",
                progress.value
              )
            }

            renderEffect = RenderEffect
              .createRuntimeShaderEffect(
                shader,
                "in_shader"
              )
              .asComposeRenderEffect()
          }
          .width(200.dp)
          .height(80.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primary)
          .pointerInput(Unit) {
            detectTapGestures(
              onPress = {
                shader.setFloatUniform(
                  "in_touch",
                  it.x,
                  it.y
                )
                progress.animateTo(1f, animationSpec = tween(1200, easing = EaseInOut))
                awaitRelease()
                progress.snapTo(0f)
              }
            )
          }
          .onSizeChanged { size ->
            width = size.width.toFloat()
            height = size.height.toFloat()
          },
      )
    }
  }
}
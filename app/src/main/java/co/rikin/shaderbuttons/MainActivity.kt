package co.rikin.shaderbuttons

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import co.rikin.shaderbuttons.shaders.GlowingButton
import co.rikin.shaderbuttons.shaders.SnowyButton
import co.rikin.shaderbuttons.ui.theme.Moon
import co.rikin.shaderbuttons.ui.theme.ShaderButtonsTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    WindowCompat.setDecorFitsSystemWindows(window, false);

    setContent {
      ShaderButtonsTheme {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          SnowyButton()
        }
      }
    }
  }
}

@Composable
fun GlowingButtons() {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .windowInsetsPadding(WindowInsets.statusBars),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    var radius by remember { mutableStateOf(0.3f) }
    var intensity by remember { mutableStateOf(1.0f) }
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(0.5f),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      GlowingButton(
        glowColor = Moon,
        text = "Light",
        textColor = Color.Gray,
        radius = radius,
        intensity = intensity
      )
    }

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .weight(0.5f), verticalArrangement = Arrangement.Center
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          "Radius",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.secondary
        )
        Slider(
          modifier = Modifier
            .weight(1f)
            .padding(20.dp),
          value = radius,
          onValueChange = { radius = it }
        )
      }
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          "Intensity",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.secondary
        )
        Slider(
          modifier = Modifier
            .weight(1f)
            .padding(20.dp),
          value = intensity,
          valueRange = 0f..2f,
          onValueChange = { intensity = it }
        )
      }
    }
  }
}
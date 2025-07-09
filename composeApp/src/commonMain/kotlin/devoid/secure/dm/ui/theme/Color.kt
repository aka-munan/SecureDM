package devoid.secure.dm.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val BlurColourLight = Color.White.copy(alpha = 0.2f)
val BlurColourDark = Color.Black.copy(alpha = 0.15f)

val BrushMiddleColorLight = Color(0x101212)
val BrushMiddleColorDark =  Color(0xFF343838)

@Composable
fun BackgroundBrushLight() :Brush{
    val background=MaterialTheme.colorScheme.background
    return Brush.horizontalGradient(listOf(background, BrushMiddleColorLight,background))
}

@Composable
fun BackgroundBrushDark():Brush{
    val edgeColor=Color(0xFF484D4D)
   return Brush.linearGradient(listOf(
       Color(0xFF2d3135), // Dark metallic gray (outer)
       Color(	0xFF2d3135), // Classic silver (center)
       Color(0xFF2d3135)  // Same dark metallic gray (outer)
   )
   )
}

@Composable
fun SecondaryBrushLight() :Brush{
    val background=MaterialTheme.colorScheme.background
    return Brush.linearGradient(listOf(background, Color.hsl(276f,1f,0.95f),background))
}

@Composable
fun SecondaryBrushDark():Brush{
    val background=MaterialTheme.colorScheme.background
   return Brush.linearGradient(listOf(background, Color.hsl(267f,1f,0.05f),background))
}
package devoid.secure.dm.ui.compose

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp

fun roundedShape(top: Dp,bottom:Dp):RoundedCornerShape{
    return RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom)
}
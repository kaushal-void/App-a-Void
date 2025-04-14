package com.example.app_a_void.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.app_a_void.R
import androidx.compose.ui.text.font.Font

val Rajdhani = FontFamily(
    Font(R.font.rajdhani_bold, FontWeight.Bold),
    Font(R.font.rajdhani_regular, FontWeight.Normal),
    Font(R.font.rajdhani_light, FontWeight.Light)
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = Rajdhani,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Rajdhani,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Rajdhani,
        fontWeight = FontWeight.Light,
        fontSize = 12.sp
    )
)

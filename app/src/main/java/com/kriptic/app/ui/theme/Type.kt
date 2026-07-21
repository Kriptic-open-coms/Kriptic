package com.kriptic.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val font = DesignTokens.AppFontFamily

val Display = TextStyle(fontFamily = font, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 38.sp)
val Title = TextStyle(fontFamily = font, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp)
val Heading = TextStyle(fontFamily = font, fontWeight = FontWeight.Medium, fontSize = 17.sp, lineHeight = 22.sp)
val Body = TextStyle(fontFamily = font, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 21.sp)
val Caption = TextStyle(fontFamily = font, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp)

val KripticTypography = Typography(
    displaySmall = Display,
    titleLarge = Title,
    titleMedium = Heading,
    bodyLarge = Body,
    bodyMedium = Body,
    labelSmall = Caption,
)

package com.back.ui

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import notepad.composeapp.generated.resources.Res
import notepad.composeapp.generated.resources.pretendard_bold
import notepad.composeapp.generated.resources.pretendard_regular
import notepad.composeapp.generated.resources.pretendard_semibold
import org.jetbrains.compose.resources.Font

@Composable
fun getPretendardFontFamily() = FontFamily(
    Font(Res.font.pretendard_regular, FontWeight.Normal),
    Font(Res.font.pretendard_semibold, FontWeight.SemiBold),
    Font(Res.font.pretendard_bold, FontWeight.Bold)
)

@Composable
fun getTypography(): Typography {
    val pretendard = getPretendardFontFamily()
    return Typography(
        displayLarge = TextStyle(fontFamily = pretendard, fontWeight = FontWeight.Bold, fontSize = 57.sp),
        displayMedium = TextStyle(fontFamily = pretendard, fontWeight = FontWeight.Bold, fontSize = 45.sp),
        displaySmall = TextStyle(fontFamily = pretendard, fontWeight = FontWeight.Bold, fontSize = 36.sp),
        headlineLarge = TextStyle(fontFamily = pretendard, fontWeight = FontWeight.SemiBold, fontSize = 32.sp),
        headlineMedium = TextStyle(fontFamily = pretendard, fontWeight = FontWeight.SemiBold, fontSize = 28.sp),
        headlineSmall = TextStyle(fontFamily = pretendard, fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
        titleLarge = TextStyle(fontFamily = pretendard, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
        titleMedium = TextStyle(fontFamily = pretendard, fontWeight = FontWeight.Medium, fontSize = 16.sp),
        titleSmall = TextStyle(fontFamily = pretendard, fontWeight = FontWeight.Medium, fontSize = 14.sp),
        bodyLarge = TextStyle(fontFamily = pretendard, fontWeight = FontWeight.Normal, fontSize = 16.sp),
        bodyMedium = TextStyle(fontFamily = pretendard, fontWeight = FontWeight.Normal, fontSize = 14.sp),
        bodySmall = TextStyle(fontFamily = pretendard, fontWeight = FontWeight.Normal, fontSize = 12.sp),
        labelLarge = TextStyle(fontFamily = pretendard, fontWeight = FontWeight.Medium, fontSize = 14.sp),
        labelMedium = TextStyle(fontFamily = pretendard, fontWeight = FontWeight.Medium, fontSize = 12.sp),
        labelSmall = TextStyle(fontFamily = pretendard, fontWeight = FontWeight.Medium, fontSize = 11.sp)
    )
}
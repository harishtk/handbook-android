package com.handbook.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.handbook.app.R

val AvenirNext = FontFamily(
    Font(
        resId = R.font.avenir_next_regular,
        weight = FontWeight.Normal,
    ),
    Font(
        resId = R.font.avenir_next_light,
        weight = FontWeight.Light,
    ),
    Font(
        resId = R.font.avenir_next_bold,
        weight = FontWeight.Bold,
    ),
    Font(
        resId = R.font.avenir_next_semi_bold,
        weight = FontWeight.SemiBold,
    ),
    Font(
        resId = R.font.avenir_next_medium,
        weight = FontWeight.Medium,
    ),
    Font(
        resId = R.font.avenir_next_bold,
        weight = FontWeight.W900,
    ),
)

val HandbookTypography = Typography(
    // Display Large - Montserrat 57/64 . -0.25px
    displayLarge = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.W400,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),

    // Display Medium - Montserrat 45/52 . 0px
    displayMedium = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.W400,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
    ),

    // Display Small - Montserrat 36/44 . 0px
    displaySmall = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.W400,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),

    // Headline Large - Montserrat 32/40 . 0px
    headlineLarge = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.W400,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),

    // Headline Medium - Montserrat 28/36 . 0px
    headlineMedium = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.W400,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),

    // Headline Small - Montserrat 24/32 . 0px
    headlineSmall = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.W400,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),

    // Title Large - Montserrat 22/28 . 0px
    titleLarge = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.W400,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),

    // Title Medium - Montserrat 16/24 . 0.15px
    titleMedium = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.W500,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),

    // Title Small - Montserrat 14/20 . 0.1px
    titleSmall = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),

    // Label Large - Montserrat 14/20 . 0.1px
    labelLarge = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),

    // Label Medium - Montserrat 12/16 . 0.5px
    labelMedium = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.W500,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),

    // Label Small - Montserrat 11/16 . 0.5px
    labelSmall = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.W500,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),

    // Body Large - Montserrat 16/24 . 0.5px
    bodyLarge = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.W400,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),

    // Body Medium - Montserrat 14/20 . 0.25px
    bodyMedium = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    // Body Small - Montserrat 12/16 . 0.4px
    bodySmall = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
)

val TypographyOld = Typography(
    bodySmall = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.Normal,
    ),
    bodyMedium = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.Normal,
    ),
    bodyLarge = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.Normal,
    ),
    displaySmall = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.Normal,
    ),
    displayMedium = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.Normal
    ),
    displayLarge = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.Normal
    ),
    headlineSmall = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.SemiBold,
    ),
    headlineMedium = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.Normal,
    ),
    titleSmall = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.SemiBold
    ),
    titleMedium = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.SemiBold
    ),
    titleLarge = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.SemiBold
    ),
    labelSmall = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.Medium
    ),
    labelMedium = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.SemiBold
    ),
    labelLarge = TextStyle(
        fontFamily = AvenirNext,
        fontWeight = FontWeight.SemiBold
    ),
)
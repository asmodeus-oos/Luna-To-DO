package com.luna.app.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Untitled UI Aesthetic Icon Set
 * Handcrafted minimalist 24x24 vectors with 2dp stroke and rounded caps/joins.
 */
object UntitledIcons {

    val Check: ImageVector by lazy {
        ImageVector.Builder(
            name = "UntitledCheck",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(20f, 6f)
            lineTo(9f, 17f)
            lineTo(4f, 12f)
        }.build()
    }

    val Play: ImageVector by lazy {
        ImageVector.Builder(
            name = "UntitledPlay",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(5f, 3f)
            lineTo(19f, 12f)
            lineTo(5f, 21f)
            close()
        }.build()
    }

    val Pause: ImageVector by lazy {
        ImageVector.Builder(
            name = "UntitledPause",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(6f, 4f)
            lineTo(6f, 20f)
            moveTo(18f, 4f)
            lineTo(18f, 20f)
        }.build()
    }

    val Clock: ImageVector by lazy {
        ImageVector.Builder(
            name = "UntitledClock",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            // Circle outer rim
            moveTo(12f, 22f)
            arcTo(10f, 10f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 2f)
            arcTo(10f, 10f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 22f)
            close()
            // Clock hands
            moveTo(12f, 6f)
            lineTo(12f, 12f)
            lineTo(16f, 14f)
        }.build()
    }

    val Flame: ImageVector by lazy {
        ImageVector.Builder(
            name = "UntitledFlame",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(8.5f, 14.5f)
            arcTo(3.5f, 3.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 12f, 18f)
            arcTo(3.5f, 3.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 15.5f, 14.5f)
            curveTo(15.5f, 11f, 12f, 9f, 12f, 9f)
            curveTo(12f, 9f, 8.5f, 11f, 8.5f, 14.5f)
            close()
            moveTo(12f, 2f)
            curveTo(6.5f, 7f, 4f, 12f, 4f, 15f)
            arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 20f, 15f)
            curveTo(20f, 10f, 16f, 5f, 12f, 2f)
            close()
        }.build()
    }

    val AlertCircle: ImageVector by lazy {
        ImageVector.Builder(
            name = "UntitledAlertCircle",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 22f)
            arcTo(10f, 10f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 2f)
            arcTo(10f, 10f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 22f)
            close()
            moveTo(12f, 8f)
            lineTo(12f, 12f)
            moveTo(12f, 16f)
            lineTo(12.01f, 16f)
        }.build()
    }

    val Calendar: ImageVector by lazy {
        ImageVector.Builder(
            name = "UntitledCalendar",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(19f, 4f)
            lineTo(5f, 4f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3f, 6f)
            lineTo(3f, 20f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5f, 22f)
            lineTo(19f, 22f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 21f, 20f)
            lineTo(21f, 6f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 19f, 4f)
            close()
            moveTo(16f, 2f)
            lineTo(16f, 6f)
            moveTo(8f, 2f)
            lineTo(8f, 6f)
            moveTo(3f, 10f)
            lineTo(21f, 10f)
        }.build()
    }

    val Target: ImageVector by lazy {
        ImageVector.Builder(
            name = "UntitledTarget",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 22f)
            arcTo(10f, 10f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 2f)
            arcTo(10f, 10f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 22f)
            close()
            moveTo(12f, 18f)
            arcTo(6f, 6f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 6f)
            arcTo(6f, 6f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 18f)
            close()
            moveTo(12f, 14f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 10f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 14f)
            close()
        }.build()
    }

    val Sparkles: ImageVector by lazy {
        ImageVector.Builder(
            name = "UntitledSparkles",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 3f)
            lineTo(14.5f, 9.5f)
            lineTo(21f, 12f)
            lineTo(14.5f, 14.5f)
            lineTo(12f, 21f)
            lineTo(9.5f, 14.5f)
            lineTo(3f, 12f)
            lineTo(9.5f, 9.5f)
            close()
        }.build()
    }

    val RotateCcw: ImageVector by lazy {
        ImageVector.Builder(
            name = "UntitledRotateCcw",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(3f, 12f)
            arcTo(9f, 9f, 0f, isMoreThanHalf = true, isPositiveArc = false, 5.64f, 5.64f)
            lineTo(1f, 10f)
            moveTo(1f, 4f)
            lineTo(1f, 10f)
            lineTo(7f, 10f)
        }.build()
    }

    val Plus: ImageVector by lazy {
        ImageVector.Builder(
            name = "UntitledPlus",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 5f)
            lineTo(12f, 19f)
            moveTo(5f, 12f)
            lineTo(19f, 12f)
        }.build()
    }

    val MoreVertical: ImageVector by lazy {
        ImageVector.Builder(
            name = "UntitledMoreVertical",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.4f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 5.5f)
            lineTo(12.01f, 5.5f)
            moveTo(12f, 12f)
            lineTo(12.01f, 12f)
            moveTo(12f, 18.5f)
            lineTo(12.01f, 18.5f)
        }.build()
    }

    val Edit: ImageVector by lazy {
        ImageVector.Builder(
            name = "UntitledEdit",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(17f, 3f)
            arcTo(2.83f, 2.83f, 0f, isMoreThanHalf = false, isPositiveArc = true, 21f, 7f)
            lineTo(7.5f, 20.5f)
            lineTo(2f, 22f)
            lineTo(3.5f, 16.5f)
            close()
            moveTo(15f, 5f)
            lineTo(19f, 9f)
        }.build()
    }

    val Duplicate: ImageVector by lazy {
        ImageVector.Builder(
            name = "UntitledDuplicate",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(18f, 8f)
            lineTo(10f, 8f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, 10f)
            lineTo(8f, 18f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 10f, 20f)
            lineTo(18f, 20f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 20f, 18f)
            lineTo(20f, 10f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 18f, 8f)
            close()
            moveTo(4f, 16f)
            lineTo(4f, 6f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 6f, 4f)
            lineTo(16f, 4f)
        }.build()
    }

    val Trash: ImageVector by lazy {
        ImageVector.Builder(
            name = "UntitledTrash",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(3f, 7f)
            lineTo(21f, 7f)
            moveTo(9f, 7f)
            arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 15f, 7f)
            moveTo(5.5f, 7.5f)
            lineTo(6.8f, 18.2f)
            arcTo(2.5f, 2.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 9.3f, 20.5f)
            lineTo(14.7f, 20.5f)
            arcTo(2.5f, 2.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 17.2f, 18.2f)
            lineTo(18.5f, 7.5f)
        }.build()
    }

    val Search: ImageVector by lazy {
        ImageVector.Builder(
            name = "LinearSearch",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(11f, 19f)
            arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 11f, 3f)
            arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 11f, 19f)
            close()
            moveTo(21f, 21f)
            lineTo(16.65f, 16.65f)
        }.build()
    }

    val Close: ImageVector by lazy {
        ImageVector.Builder(
            name = "LinearClose",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(18f, 6f)
            lineTo(6f, 18f)
            moveTo(6f, 6f)
            lineTo(18f, 18f)
        }.build()
    }

    val Paperclip: ImageVector by lazy {
        ImageVector.Builder(
            name = "LinearPaperclip",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(21.44f, 11.05f)
            lineTo(12.25f, 20.24f)
            arcTo(6f, 6f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3.76f, 11.76f)
            lineTo(12.95f, 2.57f)
            arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 18.61f, 8.23f)
            lineTo(9.41f, 17.42f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 6.59f, 14.59f)
            lineTo(15.07f, 6.1f)
        }.build()
    }

    val Layers: ImageVector by lazy {
        ImageVector.Builder(
            name = "LinearLayers",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 2f)
            lineTo(2f, 7f)
            lineTo(12f, 12f)
            lineTo(22f, 7f)
            close()
            moveTo(2f, 17f)
            lineTo(12f, 22f)
            lineTo(22f, 17f)
            moveTo(2f, 12f)
            lineTo(12f, 17f)
            lineTo(22f, 12f)
        }.build()
    }

    val Sun: ImageVector by lazy {
        ImageVector.Builder(
            name = "LinearSun",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 17f)
            arcTo(5f, 5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 7f)
            arcTo(5f, 5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 17f)
            close()
            moveTo(12f, 1f)
            lineTo(12f, 3f)
            moveTo(12f, 21f)
            lineTo(12f, 23f)
            moveTo(4.22f, 4.22f)
            lineTo(5.64f, 5.64f)
            moveTo(18.36f, 18.36f)
            lineTo(19.78f, 19.78f)
            moveTo(1f, 12f)
            lineTo(3f, 12f)
            moveTo(21f, 12f)
            lineTo(23f, 12f)
            moveTo(4.22f, 19.78f)
            lineTo(5.64f, 18.36f)
            moveTo(18.36f, 5.64f)
            lineTo(19.78f, 4.22f)
        }.build()
    }

    val Moon: ImageVector by lazy {
        ImageVector.Builder(
            name = "LinearMoon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(21f, 12.79f)
            arcTo(9f, 9f, 0f, isMoreThanHalf = true, isPositiveArc = true, 11.21f, 3f)
            arcTo(7f, 7f, 0f, isMoreThanHalf = false, isPositiveArc = false, 21f, 12.79f)
            close()
        }.build()
    }

    val Volume2: ImageVector by lazy {
        ImageVector.Builder(
            name = "LinearVolume2",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(11f, 5f)
            lineTo(6f, 9f)
            lineTo(2f, 9f)
            lineTo(2f, 15f)
            lineTo(6f, 15f)
            lineTo(11f, 19f)
            close()
            moveTo(19.07f, 4.93f)
            arcTo(10f, 10f, 0f, isMoreThanHalf = false, isPositiveArc = true, 19.07f, 19.07f)
            moveTo(15.54f, 8.46f)
            arcTo(5f, 5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 15.54f, 15.54f)
        }.build()
    }

    val VolumeX: ImageVector by lazy {
        ImageVector.Builder(
            name = "LinearVolumeX",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(11f, 5f)
            lineTo(6f, 9f)
            lineTo(2f, 9f)
            lineTo(2f, 15f)
            lineTo(6f, 15f)
            lineTo(11f, 19f)
            close()
            moveTo(23f, 9f)
            lineTo(17f, 15f)
            moveTo(17f, 9f)
            lineTo(23f, 15f)
        }.build()
    }

    val PriorityHigh: ImageVector by lazy {
        ImageVector.Builder(
            name = "LinearPriorityHigh",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(6f, 19f)
            lineTo(6f, 15f)
            moveTo(12f, 19f)
            lineTo(12f, 10f)
            moveTo(18f, 19f)
            lineTo(18f, 5f)
        }.build()
    }

    val PriorityMedium: ImageVector by lazy {
        ImageVector.Builder(
            name = "LinearPriorityMedium",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(6f, 19f)
            lineTo(6f, 15f)
            moveTo(12f, 19f)
            lineTo(12f, 10f)
        }.build()
    }

    val PriorityLow: ImageVector by lazy {
        ImageVector.Builder(
            name = "LinearPriorityLow",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(6f, 19f)
            lineTo(6f, 15f)
        }.build()
    }

    val PriorityUrgent: ImageVector by lazy {
        ImageVector.Builder(
            name = "LinearPriorityUrgent",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 2f)
            lineTo(22f, 12f)
            lineTo(12f, 22f)
            lineTo(2f, 12f)
            close()
            moveTo(12f, 8f)
            lineTo(12f, 12f)
            moveTo(12f, 16f)
            lineTo(12.01f, 16f)
        }.build()
    }

    val Settings: ImageVector by lazy {
        ImageVector.Builder(
            name = "LinearSettings",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(4f, 21f); lineTo(4f, 14f)
            moveTo(4f, 10f); lineTo(4f, 3f)
            moveTo(12f, 21f); lineTo(12f, 12f)
            moveTo(12f, 8f); lineTo(12f, 3f)
            moveTo(20f, 21f); lineTo(20f, 16f)
            moveTo(20f, 12f); lineTo(20f, 3f)
            moveTo(1f, 14f); lineTo(7f, 14f)
            moveTo(9f, 8f); lineTo(15f, 8f)
            moveTo(17f, 16f); lineTo(23f, 16f)
        }.build()
    }

    val Minus: ImageVector by lazy {
        ImageVector.Builder(
            name = "LinearMinus",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(5f, 12f)
            lineTo(19f, 12f)
        }.build()
    }

    val Vibrate: ImageVector by lazy {
        ImageVector.Builder(
            name = "LinearVibrate",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(9f, 5f)
            lineTo(15f, 5f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 17f, 7f)
            lineTo(17f, 17f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 15f, 19f)
            lineTo(9f, 19f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 7f, 17f)
            lineTo(7f, 7f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 9f, 5f)
            close()
            moveTo(3f, 8f); lineTo(3f, 16f)
            moveTo(21f, 8f); lineTo(21f, 16f)
        }.build()
    }

    val Home: ImageVector by lazy {
        ImageVector.Builder(
            name = "LinearHome",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(3f, 9.5f)
            lineTo(12f, 2.5f)
            lineTo(21f, 9.5f)
            lineTo(21f, 20f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 19f, 22f)
            lineTo(5f, 22f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3f, 20f)
            close()
            moveTo(9.5f, 22f)
            lineTo(9.5f, 13.5f)
            lineTo(14.5f, 13.5f)
            lineTo(14.5f, 22f)
        }.build()
    }

    val HomeFilled: ImageVector by lazy {
        ImageVector.Builder(
            name = "FilledHome",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = SolidColor(Color.Black)
        ) {
            moveTo(11.29f, 2.29f)
            arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12.71f, 2.29f)
            lineTo(20.71f, 9.29f)
            arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 21f, 10f)
            lineTo(21f, 20f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 19f, 22f)
            lineTo(15f, 22f)
            arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 14f, 21f)
            lineTo(14f, 15f)
            arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 13f, 14f)
            lineTo(11f, 14f)
            arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 10f, 15f)
            lineTo(10f, 21f)
            arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 9f, 22f)
            lineTo(5f, 22f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3f, 20f)
            lineTo(3f, 10f)
            arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3.29f, 9.29f)
            close()
        }.build()
    }

    val MapPin: ImageVector by lazy {
        ImageVector.Builder(
            name = "LinearMapPin",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(21f, 10f)
            arcTo(9f, 9f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3f, 10f)
            curveTo(3f, 17f, 12f, 23f, 12f, 23f)
            curveTo(12f, 23f, 21f, 17f, 21f, 10f)
            close()
            moveTo(12f, 13f)
            arcTo(3f, 3f, 0f, isMoreThanHalf = true, isPositiveArc = false, 12f, 7f)
            arcTo(3f, 3f, 0f, isMoreThanHalf = true, isPositiveArc = false, 12f, 13f)
            close()
        }.build()
    }

    val Bag: ImageVector by lazy {
        ImageVector.Builder(
            name = "LinearBag",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(6f, 2f)
            lineTo(3f, 6f)
            lineTo(3f, 20f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5f, 22f)
            lineTo(19f, 22f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 21f, 20f)
            lineTo(21f, 6f)
            lineTo(18f, 2f)
            close()
            moveTo(3f, 6f)
            lineTo(21f, 6f)
            moveTo(16f, 10f)
            arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8f, 10f)
        }.build()
    }

    val Gear: ImageVector by lazy {
        ImageVector.Builder(
            name = "LinearGear",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 15f)
            arcTo(3f, 3f, 0f, isMoreThanHalf = true, isPositiveArc = false, 12f, 9f)
            arcTo(3f, 3f, 0f, isMoreThanHalf = true, isPositiveArc = false, 12f, 15f)
            close()
            moveTo(19.4f, 15f)
            arcTo(1.65f, 1.65f, 0f, isMoreThanHalf = false, isPositiveArc = false, 19.73f, 16.82f)
            lineTo(19.79f, 16.88f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16.96f, 19.71f)
            lineTo(16.9f, 19.65f)
            arcTo(1.65f, 1.65f, 0f, isMoreThanHalf = false, isPositiveArc = false, 15.08f, 19.32f)
            lineTo(15f, 20.33f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 11f, 20.33f)
            lineTo(10.92f, 19.32f)
            arcTo(1.65f, 1.65f, 0f, isMoreThanHalf = false, isPositiveArc = false, 9.1f, 19.65f)
            lineTo(9.04f, 19.71f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 6.21f, 16.88f)
            lineTo(6.27f, 16.82f)
            arcTo(1.65f, 1.65f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6.6f, 15f)
            lineTo(5.59f, 14.92f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5.59f, 10.92f)
            lineTo(6.6f, 11f)
            arcTo(1.65f, 1.65f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6.27f, 9.18f)
            lineTo(6.21f, 9.12f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 9.04f, 6.29f)
            lineTo(9.1f, 6.35f)
            arcTo(1.65f, 1.65f, 0f, isMoreThanHalf = false, isPositiveArc = false, 10.92f, 6.68f)
            lineTo(11f, 5.67f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 15f, 5.67f)
            lineTo(15.08f, 6.68f)
            arcTo(1.65f, 1.65f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16.9f, 6.35f)
            lineTo(16.96f, 6.29f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 19.79f, 9.12f)
            lineTo(19.73f, 9.18f)
            arcTo(1.65f, 1.65f, 0f, isMoreThanHalf = false, isPositiveArc = false, 19.4f, 11f)
            lineTo(20.41f, 11f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 20.41f, 15f)
            close()
        }.build()
    }

    val EstateHome: ImageVector by lazy {
        ImageVector.Builder(
            name = "EstateHome",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).addPath(
            pathData = PathParser().parsePathString("M20,8h0L14,2.74a3,3,0,0,0-4,0L4,8a3,3,0,0,0-1,2.26V19a3,3,0,0,0,3,3H18a3,3,0,0,0,3-3V10.25A3,3,0,0,0,20,8ZM14,20H10V15a1,1,0,0,1,1-1h2a1,1,0,0,1,1,1Zm5-1a1,1,0,0,1-1,1H16V15a3,3,0,0,0-3-3H11a3,3,0,0,0-3,3v5H6a1,1,0,0,1-1-1V10.25a1,1,0,0,1,.34-.75l6-5.25a1,1,0,0,1,1.32,0l6,5.25a1,1,0,0,1,.34.75Z").toNodes(),
            fill = SolidColor(Color.Black)
        ).build()
    }

    val ClipboardNotes: ImageVector by lazy {
        ImageVector.Builder(
            name = "ClipboardNotes",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).addPath(
            pathData = PathParser().parsePathString("M13,14H9a1,1,0,0,0,0,2h4a1,1,0,0,0,0-2ZM17,4H15.82A3,3,0,0,0,13,2H11A3,3,0,0,0,8.18,4H7A3,3,0,0,0,4,7V19a3,3,0,0,0,3,3H17a3,3,0,0,0,3-3V7A3,3,0,0,0,17,4ZM10,5a1,1,0,0,1,1-1h2a1,1,0,0,1,1,1V6H10Zm8,14a1,1,0,0,1-1,1H7a1,1,0,0,1-1-1V7A1,1,0,0,1,7,6H8V7A1,1,0,0,0,9,8h6a1,1,0,0,0,1-1V6h1a1,1,0,0,1,1,1Zm-3-9H9a1,1,0,0,0,0,2h6a1,1,0,0,0,0-2Z").toNodes(),
            fill = SolidColor(Color.Black)
        ).build()
    }

    val StopwatchTab: ImageVector by lazy {
        ImageVector.Builder(
            name = "StopwatchTab",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).addPath(
            pathData = PathParser().parsePathString("M18.3,8.59l.91-.9a1,1,0,0,0-1.42-1.42l-.9.91a8,8,0,0,0-9.79,0l-.91-.92A1,1,0,0,0,4.77,7.69l.92.91A7.92,7.92,0,0,0,4,13.5,8,8,0,1,0,18.3,8.59ZM12,19.5a6,6,0,1,1,6-6A6,6,0,0,1,12,19.5Zm-2-15h4a1,1,0,0,0,0-2H10a1,1,0,0,0,0,2Zm3,6a1,1,0,0,0-2,0v1.89a1.5,1.5,0,1,0,2,0Z").toNodes(),
            fill = SolidColor(Color.Black)
        ).build()
    }

    val WalletTab: ImageVector by lazy {
        ImageVector.Builder(
            name = "WalletTab",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            // Main wallet body with rounded corners
            moveTo(20f, 10f)
            lineTo(20f, 7.5f)
            arcTo(2.5f, 2.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 17.5f, 5f)
            lineTo(5.5f, 5f)
            arcTo(2.5f, 2.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3f, 7.5f)
            lineTo(3f, 17.5f)
            arcTo(2.5f, 2.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.5f, 20f)
            lineTo(17.5f, 20f)
            arcTo(2.5f, 2.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 20f, 17.5f)
            lineTo(20f, 15f)

            // Top bill / card slot peek
            moveTo(7f, 5f)
            lineTo(7f, 3.8f)
            arcTo(1.8f, 1.8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8.8f, 2f)
            lineTo(15.2f, 2f)
            arcTo(1.8f, 1.8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 17f, 3.8f)
            lineTo(17f, 5f)

            // Rounded clasp pocket on right edge
            moveTo(15f, 10f)
            lineTo(19.5f, 10f)
            arcTo(2.5f, 2.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22f, 12.5f)
            arcTo(2.5f, 2.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 19.5f, 15f)
            lineTo(15f, 15f)
            arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 13.5f, 13.5f)
            lineTo(13.5f, 11.5f)
            arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 15f, 10f)
            close()

            // Clasp button / lock dot
            moveTo(18.5f, 12.5f)
            lineTo(18.51f, 12.5f)
        }.build()
    }

    val SlidersSettings: ImageVector by lazy {
        ImageVector.Builder(
            name = "SlidersSettings",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).addPath(
            pathData = PathParser().parsePathString("M5 21L5 15M5 15C6.10457 15 7 14.1046 7 13C7 11.8954 6.10457 11 5 11C3.89543 11 3 11.8954 3 13C3 14.1046 3.89543 15 5 15ZM5 7V3M12 21V15M12 7V3M12 7C10.8954 7 10 7.89543 10 9C10 10.1046 10.8954 11 12 11C13.1046 11 14 10.1046 14 9C14 7.89543 13.1046 7 12 7ZM19 21V17M19 17C20.1046 17 21 16.1046 21 15C21 13.8954 20.1046 13 19 13C17.8954 13 17 13.8954 17 15C17 16.1046 17.8954 17 19 17ZM19 9V3").toNodes(),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ).build()
    }

    val ChevronRight: ImageVector by lazy {
        ImageVector.Builder(
            name = "LinearChevronRight",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(9f, 18f)
            lineTo(15f, 12f)
            lineTo(9f, 6f)
        }.build()
    }

    val GridDots4: ImageVector by lazy {
        ImageVector.Builder(
            name = "GridDots4",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).addPath(
            pathData = PathParser().parsePathString("M7 6a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3zm10 0a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3zM7 16a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3zm10 0a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3z").toNodes(),
            fill = SolidColor(Color.Black)
        ).build()
    }

    val Download: ImageVector by lazy {
        ImageVector.Builder(
            name = "UntitledDownload",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(21f, 15f)
            lineTo(21f, 19f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 19f, 21f)
            lineTo(5f, 21f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3f, 19f)
            lineTo(3f, 15f)
            moveTo(7f, 10f)
            lineTo(12f, 15f)
            lineTo(17f, 10f)
            moveTo(12f, 15f)
            lineTo(12f, 3f)
        }.build()
    }

    val Upload: ImageVector by lazy {
        ImageVector.Builder(
            name = "UntitledUpload",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(21f, 15f)
            lineTo(21f, 19f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 19f, 21f)
            lineTo(5f, 21f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3f, 19f)
            lineTo(3f, 15f)
            moveTo(17f, 8f)
            lineTo(12f, 3f)
            lineTo(7f, 8f)
            moveTo(12f, 3f)
            lineTo(12f, 15f)
        }.build()
    }
}


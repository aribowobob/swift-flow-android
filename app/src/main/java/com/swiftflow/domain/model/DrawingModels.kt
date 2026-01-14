package com.swiftflow.domain.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

data class DrawPath(
    val path: Path,
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

data class PhotoEditorState(
    val paths: List<DrawPath> = emptyList(),
    val currentColor: Color = Color.Red,
    val strokeWidth: Float = 8f,
    val isSaving: Boolean = false,
    val error: String? = null
)

package com.nuvio.tv.ui.components

import android.content.Context
import coil.request.ImageRequest
import coil.request.repeatCount
import com.nuvio.tv.domain.model.CollectionFolder

const val ANIMATED_FOCUS_DEBOUNCE_MS = 200L

fun collectionFolderCardImageUrl(
    folder: CollectionFolder,
    isFocused: Boolean
): String? {
    if (!folder.focusGifEnabled) {
        return firstNonBlank(folder.coverImageUrl)
    }
    return if (isFocused) {
        firstNonBlank(folder.focusGifUrl, folder.coverImageUrl)
    } else {
        firstNonBlank(folder.coverImageUrl, folder.focusGifUrl)
    }
}

fun collectionFolderBackdropUrl(folder: CollectionFolder): String? {
    if (!folder.backdropAnimatedEnabled) {
        return firstNonBlank(folder.backdropImageUrl)
    }
    return firstNonBlank(folder.backdropAnimatedUrl, folder.backdropImageUrl)
}

fun buildAnimatedFocusRequest(
    context: Context,
    url: String,
    playOnce: Boolean,
    focusSessionKey: Int,
    width: Int,
    height: Int
): ImageRequest {
    return ImageRequest.Builder(context)
        .data(url)
        .crossfade(false)
        .apply {
            if (playOnce) {
                repeatCount(0)
                memoryCacheKey("${url}_${width}x${height}_session$focusSessionKey")
            } else {
                memoryCacheKey("${url}_${width}x${height}")
            }
        }
        .size(width = width, height = height)
        .build()
}

private fun firstNonBlank(vararg candidates: String?): String? {
    return candidates.firstOrNull { !it.isNullOrBlank() }?.trim()
}

package com.nuvio.tv.ui.components

import com.nuvio.tv.domain.model.CollectionFolder

fun collectionFolderCardImageUrl(
    folder: CollectionFolder,
    isFocused: Boolean
): String? {
    if (!folder.focusGifEnabled) {
        return firstNonBlankMediaUrl(folder.coverImageUrl)
    }
    return if (isFocused) {
        firstNonBlankMediaUrl(folder.focusGifUrl, folder.coverImageUrl)
    } else {
        firstNonBlankMediaUrl(folder.coverImageUrl, folder.focusGifUrl)
    }
}

internal fun firstNonBlankMediaUrl(vararg candidates: String?): String? {
    return candidates.firstOrNull { !it.isNullOrBlank() }?.trim()
}

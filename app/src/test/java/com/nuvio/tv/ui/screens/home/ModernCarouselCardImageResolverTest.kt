package com.nuvio.tv.ui.screens.home

import com.nuvio.tv.domain.model.Collection
import com.nuvio.tv.domain.model.CollectionFolder
import org.junit.Assert.assertEquals
import org.junit.Test

class ModernCarouselCardImageResolverTest {

    @Test
    fun `collection folder card art skips hero backdrop in expanded mode`() {
        val item = collectionFolderCarouselItem()

        val cardImage = resolveModernCarouselCardBaseImageUrl(
            item = item,
            focusedPosterBackdropExpandEnabled = true,
            isBackdropExpanded = true,
            useLandscapeOverlayTreatment = false,
            effectiveBackdropUrl = item.heroPreview.backdrop
        )

        assertEquals("https://images.example/folder-cover.jpg", cardImage)
        assertEquals("https://images.example/folder-backdrop.gif", item.heroPreview.backdrop)
    }

    @Test
    fun `collection folder card art skips hero backdrop in landscape mode`() {
        val item = collectionFolderCarouselItem()

        val cardImage = resolveModernCarouselCardBaseImageUrl(
            item = item,
            focusedPosterBackdropExpandEnabled = false,
            isBackdropExpanded = false,
            useLandscapeOverlayTreatment = true,
            effectiveBackdropUrl = item.heroPreview.backdrop
        )

        assertEquals("https://images.example/folder-cover.jpg", cardImage)
        assertEquals("https://images.example/folder-backdrop.gif", item.heroPreview.backdrop)
    }

    @Test
    fun `catalog card art keeps existing backdrop behavior`() {
        val item = ModernCarouselItem(
            key = "catalog_row_item",
            title = "Catalog Item",
            subtitle = null,
            imageUrl = "https://images.example/catalog-image.jpg",
            heroPreview = HeroPreview(
                title = "Catalog Item",
                logo = null,
                description = null,
                contentTypeText = null,
                yearText = null,
                imdbText = null,
                genres = emptyList(),
                poster = "https://images.example/catalog-poster.jpg",
                backdrop = "https://images.example/catalog-hero.jpg",
                imageUrl = "https://images.example/catalog-image.jpg"
            ),
            payload = ModernPayload.Catalog(
                focusKey = "catalog::item",
                itemId = "item",
                itemType = "movie",
                addonBaseUrl = "https://addon.example",
                trailerTitle = "Catalog Item",
                trailerReleaseInfo = null,
                trailerApiType = "movie"
            )
        )

        assertEquals(
            "https://images.example/catalog-hero.jpg",
            resolveModernCarouselCardBaseImageUrl(
                item = item,
                focusedPosterBackdropExpandEnabled = true,
                isBackdropExpanded = true,
                useLandscapeOverlayTreatment = false,
                effectiveBackdropUrl = "https://images.example/effective-backdrop.jpg"
            )
        )
        assertEquals(
            "https://images.example/effective-backdrop.jpg",
            resolveModernCarouselCardBaseImageUrl(
                item = item,
                focusedPosterBackdropExpandEnabled = false,
                isBackdropExpanded = false,
                useLandscapeOverlayTreatment = true,
                effectiveBackdropUrl = "https://images.example/effective-backdrop.jpg"
            )
        )
    }

    private fun collectionFolderCarouselItem(): ModernCarouselItem {
        return buildCollectionFolderItem(
            collection = Collection(
                id = "collection",
                title = "Collection",
                backdropImageUrl = "https://images.example/collection-backdrop.jpg"
            ),
            folder = CollectionFolder(
                id = "folder",
                title = "Folder",
                coverImageUrl = "https://images.example/folder-cover.jpg",
                backdropImageUrl = "https://images.example/folder-backdrop.gif"
            ),
            useLandscapePosters = true
        )
    }
}

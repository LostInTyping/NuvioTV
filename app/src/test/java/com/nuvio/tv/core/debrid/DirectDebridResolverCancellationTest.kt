package com.nuvio.tv.core.debrid

import com.nuvio.tv.data.local.DebridSettingsDataStore
import com.nuvio.tv.domain.model.DebridSettings
import com.nuvio.tv.domain.model.Stream
import com.nuvio.tv.domain.model.StreamClientResolve
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DirectDebridResolverCancellationTest {

    @Test
    fun cachedResultIsAvailable_whenOwnerCallerCancelsMidAwait() = runBlocking {
        val providerEntered = CompletableDeferred<Unit>()
        val releaseProvider = CompletableDeferred<Unit>()

        val torbox = mockk<TorboxDirectDebridResolver>()
        coEvery { torbox.resolve(any(), any(), any()) } coAnswers {
            providerEntered.complete(Unit)
            releaseProvider.await()
            DirectDebridResolveResult.Success(
                url = RESOLVED_URL,
                filename = "right.mkv",
                videoSize = 1234L
            )
        }

        val realDebrid = mockk<RealDebridDirectDebridResolver>()
        val dataStore = mockk<DebridSettingsDataStore>()
        every { dataStore.settings } returns flowOf(
            DebridSettings(enabled = true, torboxApiKey = "tb_token")
        )

        val resolver = DirectDebridResolver(dataStore, torbox, realDebrid)
        val stream = testStream()

        val owner = launch(Dispatchers.Default) {
            resolver.resolveToPlayableStream(stream, season = null, episode = null)
        }

        providerEntered.await()
        owner.cancel()
        releaseProvider.complete(Unit)

        val cached = withTimeout(2_000) {
            var result: Stream? = resolver.cachedPlayableStream(stream, null, null)
            while (result == null) {
                delay(20)
                result = resolver.cachedPlayableStream(stream, null, null)
            }
            result
        }

        assertNotNull(cached)
        assertEquals(RESOLVED_URL, cached.url)
    }

    private fun testStream(): Stream = Stream(
        name = "Direct Debrid",
        title = "Title",
        description = "Description",
        url = null,
        ytId = null,
        infoHash = null,
        fileIdx = null,
        externalUrl = null,
        behaviorHints = null,
        addonName = DebridProviders.instantName(DebridProviders.TORBOX_ID),
        addonLogo = null,
        clientResolve = StreamClientResolve(
            type = "debrid",
            infoHash = "abcdef",
            fileIdx = 7,
            magnetUri = "magnet:?xt=urn:btih:abcdef",
            sources = null,
            torrentName = "Torrent",
            filename = "right.mkv",
            mediaType = "movie",
            mediaId = "tt1",
            mediaOnlyId = "tt1",
            title = "Title",
            season = null,
            episode = null,
            service = "torbox",
            serviceIndex = 0,
            serviceExtension = null,
            isCached = true
        )
    )

    companion object {
        private const val RESOLVED_URL = "https://cdn.example/right.mkv"
    }
}

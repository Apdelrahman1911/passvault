package com.passvault.shared

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class BackgroundCleanupEpisodeTest {

    @Test
    fun `repeated background episodes complete independently`() {
        val episode = BackgroundCleanupEpisode<Any>()
        val firstCleanup = Any()
        val secondCleanup = Any()

        assertTrue(episode.requestCleanup())
        episode.attachCleanup(firstCleanup)
        assertTrue(episode.completeIfCurrent(firstCleanup))
        assertFalse(episode.cleanupRequested)
        assertNull(episode.currentCleanup)

        assertTrue(episode.requestCleanup())
        episode.attachCleanup(secondCleanup)
        assertTrue(episode.completeIfCurrent(secondCleanup))
        assertFalse(episode.cleanupRequested)
        assertNull(episode.currentCleanup)
    }

    @Test
    fun `stale completion cannot clear a replacement retry`() {
        val episode = BackgroundCleanupEpisode<Any>()
        val cancelledCleanup = Any()
        val retryCleanup = Any()

        assertTrue(episode.requestCleanup())
        episode.attachCleanup(cancelledCleanup)
        assertTrue(episode.markRetryableIfCurrent(cancelledCleanup))
        episode.attachCleanup(retryCleanup)

        assertFalse(episode.completeIfCurrent(cancelledCleanup))
        assertTrue(episode.cleanupRequested)
        assertSame(retryCleanup, episode.currentCleanup)

        assertTrue(episode.completeIfCurrent(retryCleanup))
        assertFalse(episode.cleanupRequested)
        assertNull(episode.currentCleanup)
    }

    @Test
    fun `stale failure cannot make a replacement cleanup retryable`() {
        val episode = BackgroundCleanupEpisode<Any>()
        val cancelledCleanup = Any()
        val retryCleanup = Any()

        assertTrue(episode.requestCleanup())
        episode.attachCleanup(cancelledCleanup)
        assertTrue(episode.markRetryableIfCurrent(cancelledCleanup))
        episode.attachCleanup(retryCleanup)

        assertFalse(episode.markRetryableIfCurrent(cancelledCleanup))
        assertTrue(episode.cleanupRequested)
        assertSame(retryCleanup, episode.currentCleanup)
    }

    @Test
    fun `only one active callback can complete a cleanup`() {
        val episode = BackgroundCleanupEpisode<Any>()
        val cleanup = Any()

        assertTrue(episode.requestCleanup())
        assertFalse(episode.requestCleanup())
        episode.attachCleanup(cleanup)

        assertTrue(episode.completeIfCurrent(cleanup))
        assertFalse(episode.completeIfCurrent(cleanup))
    }

    @Test
    fun `cancelled cleanup remains requested until a later retry succeeds`() {
        val episode = BackgroundCleanupEpisode<Any>()
        val cancelledCleanup = Any()
        val laterRetry = Any()

        assertTrue(episode.requestCleanup())
        episode.attachCleanup(cancelledCleanup)
        assertTrue(episode.markRetryableIfCurrent(cancelledCleanup))
        assertTrue(episode.cleanupRequested)
        assertNull(episode.currentCleanup)

        episode.attachCleanup(laterRetry)
        assertTrue(episode.completeIfCurrent(laterRetry))
        assertFalse(episode.cleanupRequested)
    }

    @Test
    fun `active cleanup cannot be replaced without proving exact identity`() {
        val episode = BackgroundCleanupEpisode<Any>()
        episode.requestCleanup()
        episode.attachCleanup(Any())

        assertFailsWith<IllegalStateException> {
            episode.attachCleanup(Any())
        }
    }

    @Test
    fun `runtime reset discards a pending cleanup token`() {
        val episode = BackgroundCleanupEpisode<Any>()
        episode.requestCleanup()
        episode.attachCleanup(Any())

        episode.reset()

        assertFalse(episode.cleanupRequested)
        assertNull(episode.currentCleanup)
        assertTrue(episode.requestCleanup())
    }
}

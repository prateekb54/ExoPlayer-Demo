package com.prateek.exoplayerdemo

import android.os.Bundle
import android.os.HandlerThread
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRendererCapabilitiesList
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.BandwidthMeter
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import kotlinx.android.synthetic.main.activity_main.nextButton
import kotlinx.android.synthetic.main.activity_main.player_exo

@UnstableApi
class MainActivity : AppCompatActivity(), Player.Listener {

    companion object {
        private const val LOAD_CONTROL_MIN_BUFFER_MS = 5_000
        private const val LOAD_CONTROL_MAX_BUFFER_MS = 20_000
        private const val LOAD_CONTROL_BUFFER_FOR_PLAYBACK_MS = 500
    }

    private var player: ExoPlayer? = null
    private var playbackPosition = 0L
    private var playWhenReady = true
    private lateinit var preloadManager: DefaultPreloadManager
    private var currentMediaIndex = 0
    private val playbackThread: HandlerThread =
        HandlerThread("playback-thread", android.os.Process.THREAD_PRIORITY_AUDIO)

    fun setupPreloadManager() {
        playbackThread.start()
        val loadControl =
            DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    LOAD_CONTROL_MIN_BUFFER_MS,
                    LOAD_CONTROL_MAX_BUFFER_MS,
                    LOAD_CONTROL_BUFFER_FOR_PLAYBACK_MS,
                    DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS,
                )
                .setPrioritizeTimeOverSizeThresholds(true)
                .build()
        val renderersFactory = DefaultRenderersFactory(this)
        val trackSelector = DefaultTrackSelector(this)
        val bandwidthMeter = DefaultBandwidthMeter.getSingletonInstance(this)
        trackSelector.init({}, DefaultBandwidthMeter.getSingletonInstance(this))
        preloadManager = DefaultPreloadManager(
            DefaultPreloadControl(),
            DefaultMediaSourceFactory(this),
            trackSelector,
            DefaultBandwidthMeter.getSingletonInstance(this),
            DefaultRendererCapabilitiesList.Factory(renderersFactory),
            loadControl.allocator,
            playbackThread.looper,
        )
        initPlayer(
            playbackThread.looper,
            loadControl,
            renderersFactory,
            bandwidthMeter,
        )
        preloadManager.invalidate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupPreloadManager()
        nextButton.setOnClickListener {
            player?.let {
                currentMediaIndex = (currentMediaIndex + 1) % MediaItemDatabase.mediaUris.size
                setupMediaItem()
            }
        }
    }

    private fun initPlayer(
        playbackLooper: Looper,
        loadControl: LoadControl,
        renderersFactory: RenderersFactory,
        bandwidthMeter: BandwidthMeter,
    ) {
        player = ExoPlayer.Builder(this)
            .setPlaybackLooper(playbackLooper)
            .setLoadControl(loadControl)
            .setRenderersFactory(renderersFactory)
            .setBandwidthMeter(bandwidthMeter)
            .build()
        player?.playWhenReady = true
        player_exo.player = player
        currentMediaIndex = 0
        setupMediaItem()
        preloadManager.invalidate()
    }

    private fun setupMediaItem() {
        if (currentMediaIndex == 0) {
            MediaItemDatabase.mediaUris.forEachIndexed { index, uri ->
                val mediaItem = MediaItemDatabase.get(index)
                preloadManager.add(mediaItem, index)
            }
        } else {
            preloadManager.remove(MediaItemDatabase.get(currentMediaIndex - 1))
        }
        preloadManager.setCurrentPlayingIndex(currentMediaIndex)
        preloadManager.invalidate()
        val mediaItem = MediaItemDatabase.get(currentMediaIndex)
        val mediaSource = preloadManager.getMediaSource(mediaItem)
        mediaSource?.let {
            player?.setMediaSource(it)
            player?.seekTo(playbackPosition)
            player?.prepare()
        }
    }

    private fun releasePlayer() {
        preloadManager.release()
        playbackThread.quit()
        player?.let {
            playbackPosition = it.currentPosition
            playWhenReady = it.playWhenReady
            it.release()
            player = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }
}
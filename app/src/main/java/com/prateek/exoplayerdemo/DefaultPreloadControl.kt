package com.prateek.exoplayerdemo

import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager.Status.STAGE_LOADED_TO_POSITION_MS
import androidx.media3.exoplayer.source.preload.TargetPreloadStatusControl

@UnstableApi
class DefaultPreloadControl : TargetPreloadStatusControl<Int> {
    override fun getTargetPreloadStatus(rankingData: Int): DefaultPreloadManager.Status {
        return DefaultPreloadManager.Status(STAGE_LOADED_TO_POSITION_MS, 500)

    }
}
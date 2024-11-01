package com.prateek.exoplayerdemo

import androidx.media3.common.MediaItem


object MediaItemDatabase {

    val mediaUris =
        mutableListOf(
            "https://storage.googleapis.com/exoplayer-test-media-0/shortform_3.mp4",
            "https://storage.googleapis.com/exoplayer-test-media-0/shortform_6.mp4",
            "https://storage.googleapis.com/exoplayer-test-media-0/shortform_4.mp4",
            "https://storage.googleapis.com/exoplayer-test-media-0/shortform_1.mp4",
            "https://storage.googleapis.com/exoplayer-test-media-0/shortform_2.mp4",
        )

    fun get(index: Int): MediaItem {
        val uri = mediaUris.get(index.mod(mediaUris.size))
        return MediaItem.Builder().setUri(uri).setMediaId(index.toString()).build()
    }
}
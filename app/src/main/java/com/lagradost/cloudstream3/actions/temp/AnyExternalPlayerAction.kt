package com.lagradost.cloudstream3.actions.temp

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.lagradost.cloudstream3.CloudStreamApp.Companion.setKey
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.actions.VideoClickAction
import com.lagradost.cloudstream3.actions.makeTempM3U8Intent
import com.lagradost.cloudstream3.ui.result.LinkLoadingResult
import com.lagradost.cloudstream3.ui.result.ResultEpisode
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.txt

/**
 * Opens the video link in any external player installed on the device
 * via Android's native app chooser (Intent.ACTION_VIEW without a specific package).
 * Particularly useful on Android TV where users may have their own preferred player
 * that isn't in the hardcoded list (e.g. Nova Player, Kodi, etc.).
 */
class AnyExternalPlayerAction : VideoClickAction() {
    override val name = txt(R.string.episode_action_play_in_external)

    override val oneSource = true

    override val isPlayer = true

    override val sourceTypes: Set<ExtractorLinkType> = setOf(
        ExtractorLinkType.VIDEO,
        ExtractorLinkType.DASH,
        ExtractorLinkType.M3U8
    )

    override fun shouldShow(context: Context?, video: ResultEpisode?) = true

    override suspend fun runAction(
        context: Context?,
        video: ResultEpisode,
        result: LinkLoadingResult,
        index: Int?
    ) {
        if (context == null) return

        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        if (index != null) {
            val link = result.links.getOrNull(index) ?: return
            intent.setDataAndType(link.url.toUri(), "video/*")
        } else {
            makeTempM3U8Intent(context, intent, result)
        }

        // Pass title as extra — many players support this
        intent.putExtra("title", video.name)

        setKey("last_opened", video)

        // Launch directly — Android shows its native resolver with "Just once" / "Always"
        launch(intent)
    }
}

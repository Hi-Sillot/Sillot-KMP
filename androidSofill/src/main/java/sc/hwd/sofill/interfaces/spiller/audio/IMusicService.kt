package sc.hwd.sofill.interfaces.spiller.audio

import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.media.MediaPlayer

interface IMusicService {

    // region 播放控制
    fun play(uri: Uri, fileName: String? = null)
    fun pause()
    fun resume()
    fun stop()
    fun seekTo(position: Int)
    fun skipToNext()
    fun skipToPrevious()
    // endregion

    // region 播放状态查询
    fun isPlaying(): Boolean
    fun getCurrentPosition(): Int
    fun getDuration(): Int
    // endregion

    // region 播放器配置
    fun setLooping(looping: Boolean)
    fun isLooping(): Boolean
    // endregion

    // region 服务生命周期
    fun onCreate()
    fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    fun onBind(intent: Intent): IBinder
    fun onDestroy()
    // endregion

    // region 媒体会话控制
    fun setMediaSessionActive(active: Boolean)
    fun updateMediaMetadata(
        title: String?,
        artist: String?,
        album: String?,
        duration: Long
    )
    // endregion

    // region 事件监听器
    fun setOnPreparedListener(listener: MediaPlayer.OnPreparedListener)
    fun setOnCompletionListener(listener: MediaPlayer.OnCompletionListener)
    fun setOnSeekCompleteListener(listener: MediaPlayer.OnSeekCompleteListener)
    // endregion

    // region 状态广播
    fun sendMediaStatusUpdate()
    // endregion

    companion object {
        const val BASE_PACKAGE = "sc.hwd.sillot.kmp"
        const val ACTION_PLAY = "$BASE_PACKAGE.action.PLAY"
        const val ACTION_START = "$BASE_PACKAGE.action.START"
        const val ACTION_PAUSE = "$BASE_PACKAGE.action.PAUSE"
        const val ACTION_RESUME = "$BASE_PACKAGE.action.RESUME"
        const val ACTION_NEXT = "$BASE_PACKAGE.action.NEXT"
        const val ACTION_PREVIOUS = "$BASE_PACKAGE.action.PREVIOUS"
        const val ACTION_SEEKTO = "$BASE_PACKAGE.action.SEEKTO"
        const val ACTION_SkipPrevious = "$BASE_PACKAGE.action.SkipPrevious"
        const val ACTION_SkipNext = "$BASE_PACKAGE.action.SkipNext"
        const val ACTION_MEDIA_STATUS_CHANGED = "$BASE_PACKAGE.action.MEDIA_STATUS_CHANGED"
        const val EXTRA_MEDIA_PLAYING = "$BASE_PACKAGE.extra.MEDIA_PLAYING"
        const val EXTRA_MEDIA_CURRENT_POSITION = "$BASE_PACKAGE.extra.MEDIA_CURRENT_POSITION"
        const val EXTRA_MEDIA_DURATION = "$BASE_PACKAGE.extra.MEDIA_DURATION"
    }
}
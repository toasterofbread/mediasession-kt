package dev.toastbits.mediasession

data class MediaSessionMetadata(
    val track_id: String? = null,
    val length_ms: Long? = null,
    val art_url: String? = null,
    val album: String? = null,
    val album_artists: List<String>? = null,
    val artist: String? = null,
    val lyrics: String? = null,
    val audio_bpm: Int? = null,
    val auto_rating: Float? = null,
    val comment: List<String>? = null,
    val composer: List<String>? = null,
    val content_created: String? = null,
    val disc_number: Int? = null,
    val first_used: String? = null,
    val genre: List<String>? = null,
    val last_used: String? = null,
    val lyricist: List<String>? = null,
    val title: String? = null,
    val track_number: Int? = null,
    val url: String? = null,
    val use_count: Int? = null,
    val user_rating: Float? = null,

    val custom_metadata: Map<String, String> = emptyMap()
)

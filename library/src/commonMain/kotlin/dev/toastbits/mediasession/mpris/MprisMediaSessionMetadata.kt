package dev.toastbits.mediasession.mpris

import dev.toastbits.mediasession.MediaSessionMetadata
import kotlin.text.Regex

@Suppress("UNCHECKED_CAST")
fun MediaSessionMetadata.toMprisPlayerMetadata(identity: String): Map<String, Any> {
    val track_id_error: String? = track_id?.let { getTrackIdError(it) }
    check(track_id_error == null) {
        "Track ID '$track_id' is not a valid DBus object path. $track_id_error. (See https://dbus.freedesktop.org/doc/dbus-specification.html#message-protocol-marshaling-object-path)"
    }

    return mutableMapOf(
        "mpris:trackid" to track_id,
        "mpris:length" to length_ms?.times(1000L),
        "mpris:artUrl" to art_url,
        "xesam:album" to album,
        "xesam:albumArtist" to album_artists,
        "xesam:artist" to artist,
        "xesam:asText" to lyrics,
        "xesam:audioBPM" to audio_bpm,
        "xesam:autoRating" to auto_rating,
        "xesam:comment" to comment,
        "xesam:composer" to composer,
        "xesam:contentCreated" to content_created,
        "xesam:discNumber" to disc_number,
        "xesam:firstUsed" to first_used,
        "xesam:genre" to genres,
        "xesam:lastUsed" to last_used,
        "xesam:lyricist" to lyricist,
        "xesam:title" to title,
        "xesam:trackNumber" to track_number,
        "xesam:url" to url,
        "xesam:useCount" to use_count,
        "xesam:userRating" to user_rating
    )
    .also { map ->
        for ((key, value) in custom_metadata) {
            map["$identity:$key"] = value
        }
    }
    .filter { it.value != null } as Map<String, Any>
}

@Suppress("UNCHECKED_CAST")
fun Map<String, DBusVariant<*>>.fromMprisPlayerMetadata(identity: String): MediaSessionMetadata {
    val default: MediaSessionMetadata = MediaSessionMetadata()
    val custom_prefix: String = identity + ":"

    return MediaSessionMetadata(
        track_id = (get("mpris:trackid")?.value ?: default.track_id) as String?,
        length_ms = (get("mpris:length")?.value as Long?)?.div(1000L) ?: default.length_ms,
        art_url = (get("mpris:artUrl")?.value ?: default.art_url) as String?,
        album = (get("xesam:album")?.value ?: default.album) as String?,
        album_artists = (get("xesam:albumArtist")?.value as Array<DBusVariant<*>>?)?.toStringList() ?: default.album_artists,
        artist = (get("xesam:artist")?.value ?: default.artist) as String?,
        lyrics = (get("xesam:asText")?.value ?: default.lyrics) as String?,
        audio_bpm = (get("xesam:audioBPM")?.value ?: default.audio_bpm) as Int?,
        auto_rating = (get("xesam:autoRating")?.value ?: default.auto_rating) as Float?,
        comment = (get("xesam:comment")?.value as Array<DBusVariant<*>>?)?.toStringList() ?: default.comment,
        composer = (get("xesam:composer")?.value as Array<DBusVariant<*>>?)?.toStringList() ?: default.composer,
        content_created = (get("xesam:contentCreated")?.value ?: default.content_created) as String?,
        disc_number = (get("xesam:discNumber")?.value ?: default.disc_number) as Int?,
        first_used = (get("xesam:firstUsed")?.value ?: default.first_used) as String?,
        genres = (get("xesam:genre")?.value as Array<DBusVariant<*>>?)?.toStringList() ?: default.genres,
        last_used = (get("xesam:lastUsed")?.value ?: default.last_used) as String?,
        lyricist = (get("xesam:lyricist")?.value as Array<DBusVariant<*>>?)?.toStringList() ?: default.lyricist,
        title = (get("xesam:title")?.value ?: default.title) as String?,
        track_number = (get("xesam:trackNumber")?.value ?: default.track_number) as Int?,
        url = (get("xesam:url")?.value ?: default.url) as String?,
        use_count = (get("xesam:useCount")?.value ?: default.use_count) as Int?,
        user_rating = (get("xesam:userRating")?.value ?: default.user_rating) as Float?,
        custom_metadata = mapNotNull { entry ->
            if (!entry.key.startsWith(custom_prefix)) {
                return@mapNotNull null
            }

            val value: String = (entry.value.value as String?) ?: return@mapNotNull null
            return@mapNotNull entry.key.drop(custom_prefix.length) to value
        }.toMap()
    )
}

fun Array<DBusVariant<*>>.toStringList(): List<String> =
    map { it.value as String }

fun getTrackIdError(id: String): String? {
    if (!id.startsWith("/")) {
        return "ID doesn't begin with '/'"
    }

    val parts: List<String> = id.split('/').filter { it.isNotEmpty() }
    val part_regex: Regex = Regex("^[A-Za-z0-9_]+\$")

    for ((index, part) in parts.withIndex()) {
        if (!part_regex.matches(part)) {
            return "Part ${index + 1} '$part' contains illegal character(s)"
        }
    }

    return null
}

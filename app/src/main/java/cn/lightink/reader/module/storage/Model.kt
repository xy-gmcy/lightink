package cn.lightink.reader.module.storage

import android.graphics.BitmapFactory
import cn.lightink.reader.model.BookSource
import cn.lightink.reader.module.Room

data class Chapter(
    val index: Int,
    val title: String,
    val content: String,
    val level: Int = 0
)

data class ChapterPreview(
    val chapter: Chapter,
    var checked: Boolean = true
)

data class BookSourcePreview(
    val source: BookSource,
    val local: BookSource? = Room.bookSource().getLocalInstalled(source.url),
    var checked: Boolean = (local == null || source.version > local.version)
)

data class EPUBChapter(
    val index: Int,
    val title: String,
    val level: Int,
    val spineIndex: Int = 0,
    val htmlId: String?
)

data class Img(
    val name: String,
    val bytes: ByteArray
) {
    val size: String

    init {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        val width = options.outWidth
        val height = options.outHeight
        size = "${width}x${height}"
    }
}
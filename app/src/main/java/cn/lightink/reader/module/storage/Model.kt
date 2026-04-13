package cn.lightink.reader.module.storage

import cn.lightink.reader.model.BookSource
import cn.lightink.reader.module.Room

data class Chapter(
    val index: Int,
    val title: String,
    val content: String
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
package cn.lightink.reader.module.storage

import cn.lightink.reader.ktx.toJson
import cn.lightink.reader.model.BookRank
import cn.lightink.reader.model.BookSource
import cn.lightink.reader.module.Room
import cn.lightink.reader.module.booksource.BookSourceJson
import cn.lightink.reader.transcode.JavaScriptTranscoder
import com.google.gson.Gson
import java.io.File

class SourceParser {
    fun isRepository(file: File) : Boolean {
        return try {
            Gson().fromJson(file.readText(), List::class.java).all { it is String }
        } catch (e: Exception) { false }
    }

    fun getRepository(file: File) : List<BookSource>? {
        val list = Gson().fromJson(file.readText(), List::class.java).map {
                it as String
                when {
                    it.endsWith(".json") -> it.removeSuffix(".json")
                    it.endsWith(".js") -> it.removeSuffix(".js")
                    else -> it
                }
            }
        return File(file.parent, "sources").listFiles()
            ?.filter { it.isFile && it.nameWithoutExtension in list }?.mapNotNull {
                when (it.extension.toLowerCase()) {
                    "json" -> jsonToSource(it)
                    "js" -> jsToSource(it)
                    else -> null
                }
            }
    }

    fun jsonToSource(file: File) : BookSource? {
        try {
            val json = Gson().fromJson(file.readText(), BookSourceJson::class.java)
            return BookSource(
                id = 0,
                name = json.name,
                url = json.url,
                version = json.version,
                rank = json.rank != null && json.rank.isNotEmpty(),
                account = json.auth != null && json.auth.login.isNotEmpty(),
                owner = file.path,
                type = "json",
                content = json.toJson(true)
            )
        } catch (e: Exception) { return null }
    }

    fun jsToSource(file: File) : BookSource? {
        try {
            val js = file.readText()
            val info = JavaScriptTranscoder(file.nameWithoutExtension, js).bookSource()
                ?: return null
            return BookSource(
                id = 0,
                name = info.name,
                url = info.url,
                version = info.version,
                rank = info.ranks != null && info.ranks.isNotEmpty(),
                account = info.authorization.isNotBlank(),
                owner = file.path,
                type = "js",
                content = js
            )
        } catch (e: Exception) { return null }
    }

    fun sourceImport(list: List<BookSourcePreview>) {
        list.forEach { (source, local) ->
            if (local == null) {
                Room.bookSource().install(source)
                if (source.rank) {
                    Room.bookRank().insert(BookRank(source.url, source.name))
                }
            } else {
                Room.bookSource().update(source)
                if (source.rank) {
                    if (local.rank)
                        Room.bookRank().update(BookRank(source.url, source.name))
                    else
                        Room.bookRank().insert(BookRank(source.url, source.name))
                }
            }
        }
    }
}
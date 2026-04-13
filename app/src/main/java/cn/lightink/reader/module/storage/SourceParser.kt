package cn.lightink.reader.module.storage

import cn.lightink.reader.ktx.toJson
import cn.lightink.reader.model.BookSource
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
        val list = Gson().fromJson(file.readText(), List::class.java) as List<String>
        return File(file.parent, "sources").listFiles()
            ?.filter { it.nameWithoutExtension in list }?.mapNotNull {
                when (it.extension.toLowerCase()) {
                    "json" -> jsonToSource(it)
                    "js" -> jsToSource(it)
                    else -> null
                }
            }
    }

    fun jsonToSource(file: File) : BookSource? {
        val json = Gson().fromJson(file.readText(), BookSourceJson::class.java)
            ?: return null
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
    }

    fun jsToSource(file: File) : BookSource? {
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
    }
}
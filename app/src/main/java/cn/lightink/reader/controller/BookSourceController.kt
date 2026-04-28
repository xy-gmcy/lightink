package cn.lightink.reader.controller

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.toLiveData
import cn.lightink.reader.transcode.JavaScriptTranscoder
import cn.lightink.reader.ktx.toJson
import cn.lightink.reader.model.BookSource
import cn.lightink.reader.model.Result
import cn.lightink.reader.module.EMPTY
import cn.lightink.reader.module.LIMIT
import cn.lightink.reader.module.Room
import cn.lightink.reader.module.booksource.BookSourceJson
import cn.lightink.reader.module.booksource.BookSourceParser
import cn.lightink.reader.module.storage.BookSourcePreview
import cn.lightink.reader.module.storage.SourceParser
import cn.lightink.reader.net.Http
import cn.lightink.reader.ui.discover.storage.BookSourceImportFragment.RepositoryDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookSourceController : ViewModel() {

    /**************************************************************************************************************************************
     * 书源列表
     *************************************************************************************************************************************/
    //读取已安装书源列表
    val bookSources = Room.bookSource().getAll().toLiveData(LIMIT)

    /**
     * 验证书源登录
     */
    fun verify(bookSource: BookSource): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch(Dispatchers.IO) {
            if (bookSource.type == "js") {
                result.postValue(JavaScriptTranscoder(bookSource.url, bookSource.content).loginVerify())
            } else {
                result.postValue(BookSourceParser(bookSource).verify())
            }
        }
        return result
    }

    /**************************************************************************************************************************************
     * 验证网络书源
     *************************************************************************************************************************************/
    fun verifyRepository(url: String, fragmentManager: FragmentManager): LiveData<String> {
        val liveData = MutableLiveData<String>()
        viewModelScope.launch(Dispatchers.IO) {
            val response: Result<List<String>>
            try {
                response = Http.get<List<String>>(url)
            } catch (e: Exception) {
                liveData.postValue(e.message)
                return@launch
            }
            when {
                response.isSuccessful && response.data.isNullOrEmpty() -> liveData.postValue("该网址不存在书源索引")
                response.isSuccessful -> {
                    withContext(Dispatchers.IO) {
                        /**
                        response.data?.forEach { name ->
                        launch { verifyBookSource(name, url) }
                        }
                         **/
                        var type = "json"
                        response.data?.mapNotNull {
                            when {
                                it == "---json---" -> { type = "json" ; null }
                                it == "---js---" -> { type = "js"; null }
                                it.endsWith(".json") -> verifyBookSource(it.removeSuffix(".json"), url, "json")
                                it.endsWith(".js") -> verifyBookSource(it.removeSuffix(".js"), url, "js")
                                else -> verifyBookSource(it,url, type)
                            }
                        }
                    }?.apply {
                        if (this.isEmpty()) { liveData.postValue("该仓库不存在书源"); return@launch }
                    }?.map { BookSourcePreview(it) }
                        .apply {
                            RepositoryDialog(url, this!!).callback { list ->
                                list.filter { it.checked }.apply {
                                    if (this.isNotEmpty()) SourceParser().sourceImport(this)
                                }
                            }.show(fragmentManager)
                        }
                    liveData.postValue(EMPTY)
                }
                else -> liveData.postValue(response.message)
            }
        }
        return liveData
    }


    private suspend fun verifyBookSource(name: String, baseUrl: String, type: String): BookSource? {
        return when (type) {
            "json" -> {
                val url = "${baseUrl.substringBeforeLast("/")}/sources/$name.json"
                val response = Http.get<BookSourceJson>(url)
                if (response.isSuccessful && response.data != null) {
                    BookSource(
                        0,
                        response.data.name,
                        response.data.url,
                        response.data.version,
                        !response.data.rank.isNullOrEmpty(),
                        response.data.auth != null && response.data.auth.login.isNotEmpty(),
                        baseUrl,
                        "json",
                        response.data.toJson(true)
                    )
                } else null
            }
            "js" -> {
                val url = "${baseUrl.substringBeforeLast("/")}/sources/$name.js"
                val response = Http.get<String>(url)
                if (response.isSuccessful && response.data != null) {
                    val javaScript = response.data
                    val info = JavaScriptTranscoder(name, javaScript).bookSource() ?: return null
                    BookSource(
                        0, info.name,
                        info.url,
                        info.version,
                        info.ranks.isNotEmpty(),
                        info.authorization.isNotEmpty(),
                        baseUrl,
                        "js",
                        javaScript
                    )
                } else null
            }
            else -> null
        }
    }
    /**
    private suspend fun verifyBookSource(name: String, baseUrl: String) {
        if (name.endsWith(".js")) {
            val url = "${baseUrl.substringBeforeLast("/")}/sources/$name"
            val response = Http.get<String>(url)
            if (response.isSuccessful && response.data != null) {
                val javaScript = response.data
                val info = JavaScriptTranscoder(name, javaScript).bookSource() ?: return
                val bookSource = Room.bookSource().getLocalInstalled(info.url)
                if (bookSource != null) {
                    //已安装需要更新版本
                    if (bookSource.version < info.version) {
                        if (!bookSource.rank && info.ranks.isNotEmpty() && !Room.bookRank().isExist(info.url)) {
                            Room.bookRank().insert(BookRank(info.url, info.name))
                        }
                        bookSource.name = info.name
                        bookSource.version = info.version
                        bookSource.rank = bookSource.rank || info.ranks.isNotEmpty()
                        bookSource.account = info.authorization.isNotEmpty()
                        bookSource.content = javaScript
                        Room.bookSource().update(bookSource)
                    }
                } else {
                    //未安装
                    Room.bookSource().install(
                        BookSource(
                            0, info.name,
                            info.url,
                            info.version,
                            info.ranks.isNotEmpty(),
                            info.authorization.isNotEmpty(),
                            baseUrl,
                            "js",
                            javaScript
                        )
                    )
                    if (info.ranks.isNotEmpty() && !Room.bookRank().isExist(info.url)) {
                        Room.bookRank().insert(BookRank(info.url, info.name))
                    }
                }
            }
        } else {
            val url = "${baseUrl.substringBeforeLast("/")}/sources/$name.json"
            val response = Http.get<BookSourceJson>(url)
            if (response.isSuccessful && response.data != null && !Room.bookSource().isInstalled(response.data.url)) {
                val bookSource = Room.bookSource().getLocalInstalled(response.data.url)
                if (bookSource != null) {
                    //已安装需要更新版本
                    if (bookSource.version < response.data.version) {
                        if (!bookSource.rank && !response.data.rank.isNullOrEmpty() && !Room.bookRank().isExist(response.data.url)) {
                            Room.bookRank().insert(BookRank(response.data.url, response.data.name))
                        }
                        bookSource.name = response.data.name
                        bookSource.version = response.data.version
                        bookSource.rank = response.data.rank.isNullOrEmpty()
                        bookSource.account = response.data.auth != null
                        bookSource.content = response.data.toJson()
                        Room.bookSource().update(bookSource)
                    }
                } else {
                    //未安装
                    Room.bookSource().install(
                        BookSource(
                            0,
                            response.data.name,
                            response.data.url,
                            response.data.version,
                            !response.data.rank.isNullOrEmpty(),
                            response.data.auth != null,
                            baseUrl,
                            "json",
                            response.data.toJson()
                        )
                    )
                    if (!response.data.rank.isNullOrEmpty() && !Room.bookRank().isExist(response.data.url)) {
                        Room.bookRank().insert(BookRank(response.data.url, response.data.name))
                    }
                }
            }
        }
    }
    **/

    /**
     * 卸载书源
     */
    fun uninstall(bookSource: BookSource) {
        viewModelScope.launch(Dispatchers.IO) {
            Room.bookSource().uninstall(bookSource)
        }
    }

}
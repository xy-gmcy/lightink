package cn.lightink.reader.controller

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cn.lightink.reader.BOOK_PATH
import cn.lightink.reader.ktx.md5
import cn.lightink.reader.ktx.only
import cn.lightink.reader.ktx.toJson
import cn.lightink.reader.model.Book
import cn.lightink.reader.model.Bookshelf
import cn.lightink.reader.model.MPMetadata
import cn.lightink.reader.module.MP_ENTER
import cn.lightink.reader.module.MP_FILENAME_CATALOG
import cn.lightink.reader.module.MP_FILENAME_METADATA
import cn.lightink.reader.module.MP_FOLDER_IMAGES
import cn.lightink.reader.module.MP_FOLDER_TEXTS
import cn.lightink.reader.module.Room
import cn.lightink.reader.module.storage.Chapter
import cn.lightink.reader.module.storage.Img
import cn.lightink.reader.ui.bookshelf.SelectPreferredBookshelfDialog
import java.io.File
import kotlin.collections.forEach

class ImportController {

    fun bookCheck(name: String, author: String): Book? {
        val books = Room.book().getAll()
        books.forEach { book ->
            if (book.name == name && book.author == author ) return book
        }
        return null
    }

    /**
     * 获取首选书架，无首选书架则弹出书架选择列表
     */
    fun getPreferredBookshelf(
        fragmentManager: FragmentManager,
        callback: (Bookshelf) -> Unit
    ) {
        val preferred = Room.getPreferredBookshelf()
        if (preferred != null) {
            callback(preferred)
            return
        }

        SelectPreferredBookshelfDialog()
            .callback {
                    bookshelf -> callback(bookshelf)
            }.show(fragmentManager)
        return
    }

    fun publish(metadata: MPMetadata, chapters: List<Chapter>, img: List<Img> = emptyList(), bookshelf: Bookshelf): LiveData<Book?> {
        val liveData = MutableLiveData<Book?>()
        //生成输出目录
        val output = File(BOOK_PATH, metadata.objectId).only()
        //生成图片文件夹
        val imgDir = File(output, MP_FOLDER_IMAGES).apply { mkdirs() }
        //生成章节文件夹
        val chapterDir = File(output, MP_FOLDER_TEXTS).apply{ mkdirs() }
        //生成目录
        val catalog = File(output, MP_FILENAME_CATALOG).apply { createNewFile() }
        val catalogBuilder = StringBuilder()
        //写入目录和章节
        chapters.forEach { chapter ->
            val chapterFile = File(
                chapterDir,
                "${
                    chapter.index.toString() +
                            chapter.title.md5() +
                            System.currentTimeMillis()
                                .toString()
                }.md"
            ).apply { createNewFile() }
            val chapterContent = chapter.content
                .replace("\r\n", "\n")
                .lines()
                .filter { it.isNotBlank() }
                .joinToString("\n") { it.trim() }
            chapterFile.writeText(chapterContent.trim())
            if (chapter.level > 0) catalogBuilder.append("\t".repeat(chapter.level))
            catalogBuilder.append("* [${chapter.title}](${chapterFile.nameWithoutExtension})$MP_ENTER")
        }
        //写入图片
        img.forEach {
            val imgFile = File(imgDir, it.name).apply { createNewFile() }
            imgFile.writeBytes(it.bytes)
        }
        catalog.writeText(catalogBuilder.toString())
        //存储元数据
        File(output, MP_FILENAME_METADATA).writeText(metadata.toJson())
        //构造图书对象
        val book = Book(metadata, bookshelf.id ?: -1L)
        book.catalog = chapters.size
        book.lastChapter = chapters.last().title

        //加入书架
        Room.book().insert(book)
        liveData.postValue(book)
        return liveData
    }
}
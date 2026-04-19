package cn.lightink.reader.ui.discover.storage

import android.content.Intent
import androidx.lifecycle.lifecycleScope
import cn.lightink.reader.R
import cn.lightink.reader.controller.ImportController
import cn.lightink.reader.ktx.toast
import cn.lightink.reader.module.Preferences
import cn.lightink.reader.module.Room
import cn.lightink.reader.module.storage.BookParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class BookImportFragment : StorageFragment(){

    val controller = ImportController()

    override fun getDefaultDir(): File {
        val systemDir = android.os.Environment.getExternalStorageDirectory()
        val defaultDir = File(
            Preferences.get(
                Preferences.Key.BOOK_STORAGE_PATH,
                systemDir.absolutePath
            )
        )
        return if (defaultDir.exists()) defaultDir else systemDir
    }

    override fun onOpenFile(file: File) {
        when (file.extension.toLowerCase()) {
            "txt" -> runWithLoadingFile(file) { finish ->
                view?.post {
                    startActivity(
                        Intent(requireContext(), TXTConvertActivity::class.java).apply {
                            putExtra(TXTConvertActivity.BOOK_FILE, file.absolutePath)
                        }
                    )
                    finish()
                }
            }
            "epub", "mobi", "azw3" -> runWithLoadingFile(file) { finish ->
                view?.post {
                    try {
                        val parser = BookParser(file, requireContext())
                        val book = controller.bookCheck(parser.metadata.name, parser.metadata.author)
                        if (book != null) {
                            requireContext().toast("${Room.bookshelf().get(book.bookshelf).name}已存在《${book.name}》")
                            parser.close()
                            finish()
                            return@post
                        }
                        controller.getPreferredBookshelf(requireFragmentManager()) { bookshelf ->
                            lifecycleScope.launch {
                                try {
                                    val list = withContext(Dispatchers.IO) { parser.getList() }
                                    controller.publish(parser.metadata, list.first, list.second, bookshelf)
                                    requireContext().toast("导入成功")
                                } catch (e: Exception) {
                                    requireContext().toast("导入失败")
                                } finally {
                                    parser.close()
                                    finish()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        requireContext().toast("导入失败")
                        finish()
                    }
                }
            }
            else -> requireContext().toast("暂不支持该格式文件")
        }
    }

    override fun onSaveCurrentPath(dir: File) {
        Preferences.put(
            Preferences.Key.BOOK_STORAGE_PATH,
            dir.absolutePath
        )
    }

    override fun getTopbarText(): Int {
        return R.string.discover_storage
    }
}
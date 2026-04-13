package cn.lightink.reader.ui.discover.storage

import android.content.Intent
import android.view.View
import cn.lightink.reader.R
import cn.lightink.reader.ktx.toast
import cn.lightink.reader.module.Preferences
import java.io.File

class BookImportFragment : StorageFragment(){

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
                view?.findViewById<View>(R.id.mStorageRecycler)?.post {
                    startActivity(
                        Intent(requireContext(), TXTConvertActivity::class.java).apply {
                            putExtra(TXTConvertActivity.BOOK_FILE, file.absolutePath)
                        }
                    )
                    finish()
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
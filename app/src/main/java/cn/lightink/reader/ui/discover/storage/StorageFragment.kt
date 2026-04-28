package cn.lightink.reader.ui.discover.storage

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import cn.lightink.reader.R
import cn.lightink.reader.ktx.change
import cn.lightink.reader.ktx.size
import cn.lightink.reader.ktx.toast
import cn.lightink.reader.module.ListAdapter
import cn.lightink.reader.ui.base.LifecycleFragment
import cn.lightink.reader.ui.base.PopupMenu
import kotlinx.android.synthetic.main.fragment_storage.*
import kotlinx.android.synthetic.main.activity_storage.*
import kotlinx.android.synthetic.main.item_file.view.*
import java.io.File
import java.text.SimpleDateFormat
import kotlin.io.extension
import kotlin.io.nameWithoutExtension

abstract class StorageFragment : LifecycleFragment() {

    private val adapter by lazy { buildAdapter() }
    private var loadingPath: String? = null
    protected var currentDir: File? = null
    protected val dirStack = mutableListOf<File>()

    abstract fun getDefaultDir(): File
    abstract fun onOpenFile(file: File)

    abstract fun getTopbarText(): Int
    open fun onSaveCurrentPath(dir: File) {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_storage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mTopbar.text = getString(getTopbarText())

        mTopbar.setNavigationOnClickListener {
            activity?.finish()
        }

        mTopbar.setOnMenuClickListener {
            showPopup()
        }

        mSearchBox.change { value ->
            fileFilter(value)
            mSearchClear.isVisible = value.isNotBlank()
        }

        mSearchClear.setOnClickListener {
            mSearchBox.setText("")
        }

        mPathBar.setOnClickListener {
            val parent = currentDir?.parentFile
            if (parent != null && parent.exists()) {
                openDir(parent)
            } else {
                activity?.finish()
            }
        }

        mStorageRecycler.layoutManager = LinearLayoutManager(requireContext())
        mStorageRecycler.adapter = adapter

        openDir(getDefaultDir())
    }

    open fun onBackPressed(): Boolean {
        return if (dirStack.isNotEmpty()) {
            val last = dirStack.removeAt(dirStack.lastIndex)
            openDir(last, false)
            true
        } else false
    }

    override fun onResume() {
        super.onResume()
        setLoadingFile(null)
    }

    protected fun runWithLoadingFile(
        file: File,
        action: (finish: () -> Unit) -> Unit
    ) {
        setLoadingFile(file.absolutePath)
        action {
            setLoadingFile(null)
        }
    }

    protected fun openDir(dir: File, pushStack: Boolean = true) {
        if (pushStack && currentDir != null) {
            dirStack.add(currentDir!!)
        }

        currentDir = dir
        mCurrentPath.text = dir.absolutePath
        fileFilter(mSearchBox.text.toString())
    }

    private fun onFileClick(file: File) {
        if (file.isDirectory) {
            openDir(file, true)
            return
        }
        onOpenFile(file)
    }

    private fun setLoadingFile(path: String?) {
        loadingPath = path
        adapter.notifyDataSetChanged()
    }

    private fun showPopup() {
        PopupMenu(requireActivity())
            .gravity(Gravity.END)
            .items(R.string.default_storage_path)
            .callback { item ->
                when (item) {
                    R.string.default_storage_path -> {
                        currentDir?.let { onSaveCurrentPath(it) }
                    }
                }
            }
            .show(mTopbar)
    }

    protected fun fileFilter(keyword: String) {
        val files = currentDir?.listFiles()
            ?.filter {
                if (keyword.startsWith("reg/"))
                    try {
                        val regex = keyword.removePrefix("reg/").toRegex()
                        it.name.contains(regex)
                    } catch (e: Exception) {
                        requireContext().toast("Invalid regex pattern")
                        return@filter true
                    }
                else it.name.contains(keyword, false) }
            ?.sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name })
            ?: emptyList()

        adapter.submitList(files)
    }

    private fun buildAdapter() = ListAdapter<File>(R.layout.item_file) { item, file ->
        item.view.mFileLoading.visibility =
            if (file.absolutePath == loadingPath) View.VISIBLE else View.GONE
        if (file.isDirectory) {
            item.view.mFileType.setImageResource(R.drawable.ic_filetype_directory)
            item.view.mFileName.text = "/${file.name}"
        } else {
            when (file.extension.toLowerCase()) {
                "txt" ->  item.view.mFileType.setImageResource(R.drawable.ic_filetype_txt)
                "epub" ->  item.view.mFileType.setImageResource(R.drawable.ic_filetype_epub)
                "mobi" ->  item.view.mFileType.setImageResource(R.drawable.ic_filetype_mobi)
                "azw3" ->  item.view.mFileType.setImageResource(R.drawable.ic_filetype_azw3)
                "json" ->  item.view.mFileType.setImageResource(R.drawable.ic_filetype_json)
                "js" ->  item.view.mFileType.setImageResource(R.drawable.ic_filetype_javascript)
                else ->  item.view.mFileType.setImageResource(R.drawable.ic_filetype_other)
            }
            item.view.mFileName.text = file.nameWithoutExtension
        }

        item.view.mFileInfo.text = buildString {
            append(file.size())
            append("  |  ")
            append(SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(file.lastModified()))
        }

        item.view.setOnClickListener {
            onFileClick(file)
        }
    }
}
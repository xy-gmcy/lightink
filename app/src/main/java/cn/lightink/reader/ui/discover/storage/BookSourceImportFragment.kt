package cn.lightink.reader.ui.discover.storage

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import cn.lightink.reader.R
import cn.lightink.reader.ktx.toast
import cn.lightink.reader.module.Preferences
import cn.lightink.reader.module.ListAdapter
import cn.lightink.reader.module.Room
import cn.lightink.reader.module.RVLinearLayoutManager
import cn.lightink.reader.module.storage.BookSourcePreview
import cn.lightink.reader.module.storage.SourceParser
import cn.lightink.reader.ui.base.WarningMessageDialog
import kotlinx.android.synthetic.main.dialog_diff.*
import kotlinx.android.synthetic.main.dialog_list.*
import kotlinx.android.synthetic.main.item_booksource_checkable.view.*
import java.io.File

class BookSourceImportFragment : StorageFragment(){

    override fun getDefaultDir(): File {
        val systemDir = android.os.Environment.getExternalStorageDirectory()
        val defaultDir = File(
            Preferences.get(
                Preferences.Key.SOURCE_STORAGE_PATH,
                systemDir.absolutePath
            )
        )
        return if (defaultDir.exists()) defaultDir else systemDir
    }

    override fun onOpenFile(file: File) {
        val parser = SourceParser()
        val list = when (file.extension.toLowerCase()) {
            "json" -> {
                if (parser.isRepository(file)) parser.getRepository(file)
                    .apply {
                        if (this.isNullOrEmpty())
                            return requireContext().toast("书源仓库为空")
                    }
                else listOfNotNull(parser.jsonToSource(file))
            }
            "js" -> listOfNotNull(parser.jsToSource(file))
            else -> return requireContext().toast("暂不支持该格式文件")
        }.apply {
            if (this.isNullOrEmpty())
                return requireContext().toast("书源不合法")
        }?.map { BookSourcePreview(it) }
        if (list?.size == 1) {
            if (list.first().local != null) {
                BookSourceDialog(requireActivity(), list.first()) { isOK ->
                    if (isOK) {
                        Room.bookSource().update(list.first().source)
                        requireContext().toast("更新成功")
                    }
                }.show()
            }
            else {
                Room.bookSource().install(list.first().source)
                requireContext().toast("导入成功")
            }
        }
        else {
            RepositoryDialog(file.nameWithoutExtension, list!!).callback { list ->
                list.filter { it.checked }.forEach {
                    if (it.local == null) {
                        Room.bookSource().install(it.source)
                    } else {
                        Room.bookSource().update(it.source)
                    }
                }
            }.show(this.childFragmentManager)
        }
    }

    override fun onSaveCurrentPath(dir: File) {
        Preferences.put(
            Preferences.Key.SOURCE_STORAGE_PATH,
            dir.absolutePath
        )
    }

    override fun getTopbarText(): Int {
        return R.string.discover_source
    }

    class BookSourceDialog(val activity: FragmentActivity, val preview: BookSourcePreview, val callback: (Boolean) -> Unit) : Dialog(activity) {

        init {
            setContentView(R.layout.dialog_diff)
            mTitle.text = preview.source.url
            if (preview.local == null) {
                (mLocal.layoutParams as LinearLayout.LayoutParams).weight = 0f
                mLocal.visibility = View.GONE
            } else {
                mLocalName.text = preview.local.name
                mLocalVersion.text = preview.local.version.toString()
                mLocalType.text = preview.local.type
                mLocal.setOnClickListener {
                    WarningMessageDialog(activity, preview.local.content).show()
                }
            }
            mImportName.text = preview.source.name
            mImportVersion.text = preview.source.version.toString()
            mImportType.text = preview.source.type
            setOnCancelListener { callback.invoke(false) }
            mImport.setOnClickListener {
                WarningMessageDialog(activity, preview.source.content).show()
            }
            mSubmit.setOnClickListener { callback.invoke(true).run { dismiss() } }
            mCancel.setOnClickListener { callback.invoke(false).run { dismiss() } }
        }

        override fun onStart() {
            super.onStart()
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setLayout(-1, -2)
            window?.setDimAmount(0.6F)
        }
    }

    class RepositoryDialog(val repository: String, var list: List<BookSourcePreview>) : DialogFragment() {

        private val adapter: ListAdapter<BookSourcePreview> by lazy {
            buildAdapter()
        }
        private var callback: ((List<BookSourcePreview>) -> Unit)? = null

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.dialog_list, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            mTopbarTitle.setText(repository)
            mTopbar.setNavigationOnClickListener { dismiss() }
            mTopbarSubmit.setOnClickListener {
                callback?.invoke(list).run { dismissAllowingStateLoss() }
            }

            mBooKSourceRecycler.layoutManager = RVLinearLayoutManager(activity)
            mBooKSourceRecycler.adapter = adapter
            adapter.submitList(list)
        }

        fun show(manager: FragmentManager) {
            show(manager, null)
        }

        fun callback(callback: (List<BookSourcePreview>) -> Unit): RepositoryDialog {
            this.callback = callback
            return this
        }

        override fun onStart() {
            super.onStart()
            dialog?.window?.setDimAmount(0.2F)
            dialog?.window?.setLayout(resources.displayMetrics.widthPixels - resources.getDimensionPixelSize(R.dimen.dimen2v) * 2, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        private fun buildAdapter() = ListAdapter<BookSourcePreview>(R.layout.item_booksource_checkable) { item, preview ->
            item.view.mCheckableBookSource.text = preview.source.name
            item.view.mUrl.text = preview.source.url
            item.view.mType.text = preview.source.type +
                    if (preview.local!= null &&preview.source.type != preview.local.type)
                        " -> " + preview.local.type
                    else ""
            item.view.mVersion.text = preview.source.version.toString() +
                    if (preview.local!= null && preview.source.version > preview.local.version)
                        " -> " + preview.local.version.toString()
                    else ""
            item.view.mCheckableBookSource.isChecked = preview.checked
            item.view.setOnClickListener {
                preview.checked = preview.checked.not()
                item.view.mCheckableBookSource.isChecked = preview.checked
            }
            item.view.setOnLongClickListener {
                BookSourceDialog(requireActivity(), preview) {}.show()
                true
            }
        }
    }
}
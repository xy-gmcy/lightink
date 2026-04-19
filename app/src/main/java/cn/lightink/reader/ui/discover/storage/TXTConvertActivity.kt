package cn.lightink.reader.ui.discover.storage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import cn.lightink.reader.R
import cn.lightink.reader.controller.ImportController
import cn.lightink.reader.ktx.toast
import cn.lightink.reader.model.MPMetadata
import cn.lightink.reader.module.Room
import cn.lightink.reader.module.storage.ChapterPreview
import cn.lightink.reader.module.storage.TXTParser
import java.io.File
import kotlinx.android.synthetic.main.activity_txt_convert.*
import kotlinx.android.synthetic.main.dialog_input.view.*
import kotlinx.android.synthetic.main.item_chapter_checkable.view.*
import androidx.lifecycle.lifecycleScope
import cn.lightink.reader.module.ListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TXTConvertActivity : AppCompatActivity() {

    companion object {
        const val BOOK_FILE = "book_file"
        private var DEFAULT_CHAPTER_REGEX = """^\s*第\s*[\d零一二三四五六七八九十百千万]*\s*[章节回部集卷].*$"""
    }

    private lateinit var file: File
    private lateinit var rawText: String
    private lateinit var controller: ImportController
    private val adapter by lazy { buildAdapter() }
    private val parser = TXTParser()
    private var chapters = mutableListOf<ChapterPreview>()
    private var metadata: MPMetadata? = null
    private var currentRegex = Regex(DEFAULT_CHAPTER_REGEX, RegexOption.MULTILINE)
    private var isRegexMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_txt_convert)

        val path = intent.getStringExtra(BOOK_FILE)
        file = File(path!!)

        initTopbar()
        initRecycler()
        loadTxt()
    }

    // UI 初始化
    private fun initTopbar() {
        mTopbarTitle.text = file.nameWithoutExtension
        mBookChapterRegex.setText(DEFAULT_CHAPTER_REGEX)
        mSplitBySizeTextField.setText("10")

        mTopbarSubmit.setOnClickListener {
            doImport()
        }

        mBookChapterRegexSubmit.setOnClickListener {
            lifecycleScope.launch {
                applyRegex()
            }
        }

        mSplitBySizeSubmit.setOnClickListener {
            val sizeKb = mSplitBySizeTextField.text.toString()
            if (sizeKb.isEmpty()) {
                toast("请输入划分大小")
                return@setOnClickListener
            }
            performSizeSplit(sizeKb.toInt())
        }
    }

    // 章节列表初始化
    private fun initRecycler() {
        mChapterRecycler.layoutManager = LinearLayoutManager(this)
        mChapterRecycler.adapter = adapter
    }

    private fun loadTxt() {
        lifecycleScope.launch {
            chapters = mutableListOf()
            mTXTConvertLoading.visibility = View.VISIBLE
            rawText = withContext(Dispatchers.IO) {
                parser.readTextAutoCharset(file)
            }
            applyRegex()
            fillBookInfo(file.nameWithoutExtension)
            mTXTConvertLoading.visibility = View.GONE
        }
    }

    private suspend fun applyRegex() {
        val regexText = mBookChapterRegex.text?.toString()?.trim()
        if (!regexText.isNullOrEmpty()) {
            try {
                currentRegex = Regex(regexText, RegexOption.MULTILINE)
            } catch (e: Exception) {
                toast("正则表达式格式有误")
                return
            }
        }

        chapters = withContext(Dispatchers.Default) {
            parser.splitChapters(rawText, currentRegex).toMutableList()
        }

        isRegexMode = true
        updateChapterViews(chapters)
    }

    private fun fillBookInfo(filename: String) {
        val chapter =
            if (chapters.isEmpty()) null
            else chapters[0].chapter
        metadata = parser.getInfo(chapter, filename)

        mBookName.setText(metadata!!.name)
        mBookAuthor.setText(metadata!!.author)
    }

    private fun performSizeSplit(sizeKb: Int = 10) {
        val lines = rawText.lines()
        if (rawText.length < sizeKb * 1024) {
            toast("大于全文本大小")
            return
        }
        if (lines.sortedBy { it.length }.last().length > sizeKb * 1024) {
            toast("不足单段段落大小")
            return
        }

        chapters = parser.splitBySize(lines, sizeKb).toMutableList()
        updateChapterViews(chapters)
    }

    fun chapterRenameDialog(preview: ChapterPreview) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_input, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialogView.mDialogTitle.setText("重命名章节")
        dialogView.mDialogTextField.setHint("章节标题")
        dialogView.mDialogTextField.setInputType(android.text.InputType.TYPE_CLASS_TEXT)
        dialogView.mDialogTextField.setText(preview.chapter.title)

        dialog.setCanceledOnTouchOutside(true)
        dialogView.mDialogCancel.setOnClickListener { dialog.dismiss() }
        dialogView.mDialogSubmit.setOnClickListener {
            val input = dialogView.mDialogTextField.text.toString().trim()
            if (input.isNotEmpty()) {
                val index = chapters.indexOf(preview)
                if (index != -1) {
                    chapters[index] = preview.copy(
                        chapter = preview.chapter.copy(title = input)
                    )
                }
                adapter.submitList(chapters.toList())
                dialog.dismiss()
            } else {
                toast("标题不能为空")
            }
        }

        dialog.show()
    }

    private fun doImport() {
        val name = mBookName.text?.toString()?.trim()
        val author = mBookAuthor.text?.toString()?.trim()
        if (name.isNullOrEmpty() || author.isNullOrEmpty()) {
            toast("请填写书名和作者")
            return
        }
        metadata = MPMetadata(name = name, author = author)
        controller = ImportController()
        val book = controller.bookCheck(metadata!!.name, metadata!!.author)
        if (book != null) {
            toast("${Room.bookshelf().get(book.bookshelf).name}已存在《${book.name}》")
            return
        }

        controller.getPreferredBookshelf(supportFragmentManager) { bookshelf ->
            lifecycleScope.launch {
                if (mTXTConvertHint.visibility == View.VISIBLE && chapters.isNullOrEmpty()) {
                    toast("未完成章节划分")
                    return@launch
                }
                mTXTConvertLoading.visibility = View.VISIBLE
                if (chapters.isNullOrEmpty()) performSizeSplit()
                val finalChapters = withContext(Dispatchers.IO) {
                    parser.buildFinalChapters(chapters)
                }
                controller.publish(metadata!!, finalChapters, emptyList(), bookshelf)
                mTXTConvertLoading.visibility = View.GONE
                finish()
            }
        }
    }

    private fun updateChapterViews(chapters: List<ChapterPreview>) {
        adapter.submitList(chapters.toList())
        if (!isRegexMode) return

        val showSplit = chapters.size < 2

        mTXTConvertHint.visibility =
            if (showSplit) View.VISIBLE else View.GONE

        mSplitBySizeLayout.visibility =
            if (showSplit) View.VISIBLE else View.GONE

        mChapterRecycler.visibility =
            if (showSplit) View.GONE else View.VISIBLE
    }

    private fun buildAdapter() = ListAdapter<ChapterPreview>(R.layout.item_chapter_checkable) { item, preview ->
        item.view.mCheckableChapter.text = preview.chapter.title
        item.view.mCheckableChapter.setOnCheckedChangeListener(null)
        item.view.mCheckableChapter.isChecked = preview.checked
        item.view.mCheckableChapter.setOnCheckedChangeListener { _, isChecked ->
            preview.checked = isChecked
        }
        item.view.mCheckableChapter.isLongClickable = true
        item.view.mCheckableChapter.setOnLongClickListener {
            chapterRenameDialog(preview)
            true
        }
    }
}
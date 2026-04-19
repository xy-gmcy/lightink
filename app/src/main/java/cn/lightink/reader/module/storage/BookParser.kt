package cn.lightink.reader.module.storage

import android.content.Context
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import cn.lightink.reader.model.MPMetadata
import cn.lightink.reader.transcode.ContentParser
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.io.readText

class BookParser(file: File, val context: Context) {
    val dir = if (file.extension == "epub") null else unpackKindle(file)
    val tempZip = if (dir != null) dirToZip(dir) else null
    val zip = if (tempZip != null) ZipFile(tempZip) else ZipFile(file)
    val opfPath =
        if (dir == null) {
            getDocument("META-INF/container.xml")
                .selectFirst("rootfile")?.attr("full-path")
        }
        else {
            dir.walkTopDown().lastOrNull { it.extension == "opf" }
                ?.relativeTo(dir)?.path
        }
    val opf = getDocument(opfPath!!)
    val metadata: MPMetadata by lazy {
        val meta = opf.selectFirst("metadata") ?: return@lazy MPMetadata()
        MPMetadata(
            meta.getElementsByTag("dc:title").text() ?: file.nameWithoutExtension,
            meta.getElementsByTag("dc:creator").text() ?: "未知作者",
            meta.getElementsByTag("dc:identifier").text() ?: "",
            meta.getElementsByTag("dc:publisher").text() ?: "",
            -1
        )
    }

    fun getList(): Pair<List<Chapter>, List<Img>> {

        val manifest = opf.select("manifest > item").associateBy(
            { it.attr("id") },
            { it.attr("href") }
        )
        val spine = opf.select("spine > itemref")
            .map { getPath(opfPath!!,manifest[it.attr("idref")]!!) }

        val tocId = opf.selectFirst("spine")?.attr("toc")
        val tocPath = getPath(opfPath!!, manifest[tocId]!!)
        val navMap = getDocument(tocPath).selectFirst("navMap")
        val toc = getToc(navMap!!).mapIndexed { index, pair ->
            val node = pair.first
            val src = node.selectFirst("> content")
                ?.attr("src")?.split("#")
            EPUBChapter(
                index,
                node.selectFirst("> navLabel > text")!!.text().trim(),
                pair.second,
                spine.indexOf(getPath(tocPath, src?.first()!!)),
                if (src.size == 2)src.last() else null
            )
        }.sortedBy { it.spineIndex }

        val img: MutableList<Img> = mutableListOf()
        val cover = getCover(manifest, spine)
        if (cover!= null) img.add(cover)

        if (toc.isEmpty()) {
            val list = spine.mapIndexed { index, path ->
                val content = getContent(path, emptyList())
                val pair = imgReg(content)
                img.addAll(pair.second)
                Chapter(
                    index,
                    path.split("/").last().substringBeforeLast("."),
                    pair.first
                )
            }
            return Pair(list, img)
        }

        val list = spine.mapIndexed { index, path ->
            getContent(path, toc.filter { it.spineIndex == index })
        }.joinToString("\n")
            .split("---Chapter Split---").drop(1)
            .mapIndexed { index, string ->
                val chapter = toc[index]
                val pair = imgReg(string)
                val lines = pair.first.lines().filter { it.isNotBlank() }
                img.addAll(pair.second)
                val content = lines.drop(
                    if (lines.isNotEmpty() && lines.first().trim() == chapter.title.trim()) 1 else 0
                ).joinToString("\n")
                Chapter(index, chapter.title, content, chapter.level)
            }
        return Pair(list, img)
    }

    fun getToc(element: Element, level: Int = 0): List<Pair<Element, Int>> {
        val list = mutableListOf<Pair<Element, Int>>()
        element.select("> navPoint")
            .forEach {
                list.add(Pair(it, level))
                list.addAll(getToc(it, level + 1))
            }
        return list
    }

    fun getCover(manifest: Map<String, String>, spine: List<String>) : Img? {
        if (!manifest["cover"].isNullOrEmpty()) {
            val path = getPath(opfPath!!,manifest["cover"]!!)
            return Img("cover", readBytes(path))
        } else {
            spine.forEach { path ->
                val img = getDocument(path).selectFirst("img")
                val image = getDocument(path).selectFirst("image")
                when {
                    image != null -> {
                        val path = getPath(path, image.attr("xlink:href"))
                        return Img("cover", readBytes(path))
                    }
                    img != null -> {
                        val path = getPath(path, img.attr("src"))
                        return Img("cover", readBytes(path))
                    }
                    else -> return null
                }
            }
            return null
        }
    }

    fun getContent(path: String, chapters: List<EPUBChapter>): String {
        val html = getDocument(path)
        html.head().remove()
        chapters.forEach {
            if (it.htmlId.isNullOrEmpty())
                html.body().before("<p>---Chapter Split---</p>")
            else
                html.getElementById(it.htmlId)?.before("<p>---Chapter Split---</p>")
        }
        html.getElementsByTag("img").forEach {
            val src = it.attr("src")
            if (src.isNullOrEmpty()) it.remove()
            else it.attr("src", getPath(path, src))
        }
        return ContentParser.read("", html.toString(), null)
    }

    fun imgReg(string: String): Pair<String, List<Img>> {
        var content = string
        val img = Regex("""^!\[]\(.+?\)$""", RegexOption.MULTILINE)
            .findAll(content)
            .map { match ->
                val src = match.value
                    .substringAfter("![](").substringBefore(")")
                Img(
                    src.split("/").last(),
                    readBytes(src)
                ).apply {
                    content = content
                        .replace(match.value, "![${this.size}](${this.name})")
                }
            }.toList()
        return Pair(content, img)
    }

    fun readText(path: String): String {
        val entry = zip.getEntry(path)
        return zip.getInputStream(entry).bufferedReader().use { it.readText() }
    }

    fun readBytes(path: String): ByteArray {
        val entry = zip.getEntry(path) ?: return ByteArray(0)
        return zip.getInputStream(entry).use { it.readBytes() }
    }

    fun getPath( path: String, src: String ): String {
        val p = path.split("/").dropLast(1)
        val s = src.split("/")
        val n = s.count { it == ".." }
        return (p.dropLast(n) + s.drop(n)).joinToString("/")
    }

    fun getDocument(path: String): Document = Jsoup.parse(
        readText(path),
        "",
        Parser.xmlParser()
    )

    fun unpackKindle(input: File): File? {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }

        val py = Python.getInstance()
        val module = py.getModule("kindle_unpack_entry")

        val outputDir = File(context.cacheDir, input.nameWithoutExtension)
        if (!outputDir.exists()) outputDir.mkdirs()

        return try {
            module.callAttr("unpack", input.absolutePath, outputDir.absolutePath)
            outputDir
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun dirToZip(dir: File): File {
        val zipFile = File(dir.parentFile, "${dir.name}.zip")
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            dir.walkTopDown().forEach { f ->
                if (f.isFile) {
                    val entry = ZipEntry(f.relativeTo(dir).path)
                    zos.putNextEntry(entry)
                    f.inputStream().copyTo(zos)
                    zos.closeEntry()
                }
            }
        }
        return zipFile
    }

    fun close() {
        try {
            zip.close()
            tempZip?.deleteRecursively()
            dir?.deleteRecursively()
        } catch (_: Exception) {}
    }
}
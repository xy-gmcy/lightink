package cn.lightink.reader.module.storage

import cn.lightink.reader.model.MPMetadata
import java.io.File
import java.nio.charset.Charset

class TXTParser {

    // 读取文本，自动识别编码
    fun readTextAutoCharset(file: File): String {
        // 读取文件字节
        val bytes = file.readBytes()
        // 候选编码列表
        val charsets = listOf(
            Charsets.UTF_8,
            Charset.forName("GB2312"),
            Charset.forName("GBK")
        )

        var bestText: String? = null
        var bestBadRatio = 1.0

        for (cs in charsets) {
            val text = String(bytes, cs)
            if (text.isBlank()) continue
            // 统计乱码字符
            val badCount = text.count { it == '\uFFFD' || it.code in 0..8 && it != '\n' && it != '\t' }
            val ratio = badCount.toDouble() / text.length

            // 乱码比例低于阈值，直接返回
            if (ratio < 0.01) {
                return text
            }
            // 记录最佳结果
            if (ratio < bestBadRatio) {
                bestBadRatio = ratio
                bestText = text
            }
        }

        return bestText ?: String(bytes, Charsets.UTF_8)
    }

    // 生成元数据
    fun getInfo(chapter: Chapter?, filename: String): MPMetadata {
        var name = filename
        var author = ""
        // 尝试从正文中提取标题和作者
        if (chapter != null &&chapter.index == 0) {
            val regName =
                Regex("""(?<=《).*?(?=》)""", RegexOption.MULTILINE).find(chapter.content)
            val regAuthor =
                Regex("""(?<=作者：).*""", RegexOption.MULTILINE).find(chapter.content)
            if (regName != null) name = regName.value
            if (regAuthor != null) author = regAuthor.value
        }
        // 尝试从文件名中提取标题和作者
        if (name == filename && author == "") {
            val match = Regex("-").find(filename)
            if (match != null) {
                name = filename.substringBefore("-").trim()
                author = filename.substringAfter("-").trim()
            }
        }
        return MPMetadata(
            name = name,
            author = author,
            state = -1
        )
    }

    // 划分章节
    fun splitChapters(content: String, regex: Regex): List<ChapterPreview> {
        // 匹配标题
        val matches = regex.findAll(content).toList()
        if (matches.isEmpty()) return emptyList()

        val result = mutableListOf<ChapterPreview>()
        // 前置内容
        if (matches[0].range.first > 0) {
            result.add(
                ChapterPreview(
                    Chapter(
                        0,
                        "无匹配章节",
                        content.take(matches[0].range.first)
                    )
                )
            )
        }
        // 章节内容
        result.addAll(matches.mapIndexed { index, match ->
            ChapterPreview(
                Chapter(
                    index + 1,
                    match.value.trim(),
                    content.substring(
                        match.range.last + 1,
                        if (index == matches.lastIndex)
                            content.length
                        else
                            matches[index + 1].range.first
                    )
                )
            )
        })

        return result
    }

    // 按大小划分章节
    fun splitBySize(lines: List<String>, chunkSizeKB: Int): List<ChapterPreview> {
        val maxChars = chunkSizeKB * 1024
        val lines = lines.filter { it.isNotBlank() }
        val chapters = mutableListOf<ChapterPreview>()
        var currentContent = StringBuilder()
        var currentLength = 0
        var partIndex = 1

        for (line in lines) {
            val lineLength = line.length

            // 如果当前部分加上这一行会超出限制，且当前部分不为空，则保存当前部分
            if (currentLength + lineLength > maxChars && currentContent.isNotEmpty()) {
                chapters.add(
                    ChapterPreview(
                        Chapter(
                            index = chapters.size,
                            title = "第${partIndex}部分",
                            content = currentContent.toString().trimEnd()
                        )
                    )
                )
                partIndex++
                currentContent = StringBuilder()
                currentLength = 0
            }

            currentContent.append("\n").append(line)
            currentLength += lineLength
        }

        // 添加最后一部分
        if (currentContent.isNotEmpty()) {
            chapters.add(
                ChapterPreview(
                    Chapter(
                        index = chapters.size,
                        title = "第${partIndex}部分",
                        content = currentContent.toString().trimEnd()
                    )
                )
            )
        }

        return chapters
    }

    // 合并章节
    fun buildFinalChapters(previews: List<ChapterPreview>): List<Chapter> {
        if (previews.all { it.checked }) return previews.map { it.chapter }
        val result = mutableListOf<Chapter>()
        var lastAccepted: Chapter? = null
        var chapter: Chapter

        for (p in previews) {
            if (p.checked) {
                chapter = p.chapter.copy(index = result.size)
                lastAccepted = chapter
            } else {
                // 合并到前一个已选章节
                if (lastAccepted != null) {
                    lastAccepted = lastAccepted
                        .copy(content = lastAccepted.content + "\n" + p.chapter.content)
                    chapter = lastAccepted
                    result.removeAt(result.lastIndex)
                }
                // 未分配章节
                else {
                    chapter = Chapter(0, "未分配章节", p.chapter.content)
                    lastAccepted = chapter
                }
            }
            result.add(chapter)
        }
        return result
    }

}
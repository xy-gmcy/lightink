package cn.lightink.reader.transcode

import com.hippo.quickjs.android.*
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.DataNode
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.parser.Parser
import java.net.URL
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach

object JSBridge {

    private fun JSContext.jsoupOperations() {
        globalObject.setProperty("JSOUP_OPERATIONS", createJSFunction { context, args ->
            val obj = args[0].cast(JSObject::class.java)
            val operation = args[1].cast(JSString::class.java).string
            if (operation == "toString") return@createJSFunction createJSString(obj.javaObject.toString())
            val input = args[2].cast(JSArray::class.java)
            val type = obj.getProperty("type").cast(JSString::class.java).string
            return@createJSFunction when (type) {
                "document" -> context.documentOperations((obj.javaObject as Document), operation, input)
                "element" -> context.elementOperations((obj.javaObject as Element), operation, input)
                "connection" -> context.connectionOperations((obj.javaObject as Connection), operation, input)
                "response" -> context.responseOperations((obj.javaObject as Connection.Response), operation, input)
                "textNode" -> context.textNodeOperations((obj.javaObject as TextNode), operation, input)
                "dataNode" -> context.dataNodeOperations((obj.javaObject as DataNode), operation, input)
                "node" -> context.nodeOperations((obj.javaObject as Node), operation, input)
                else -> return@createJSFunction createJSNull()
            }
        })
    }

    private fun JSContext.documentOperations(document: Document, operation: String, args: JSArray): JSValue {
        return when (operation) {
            "title" -> createJSString(document.title())
            "head" -> createJsoupObject(document.head())
            "body" -> createJsoupObject(document.body())
            "charset" -> createJSString(document.charset().name())
            "location" -> createJSString(document.location().toString())
            else -> elementOperations(document, operation, args)
        }
    }

    private fun JSContext.elementOperations(element: Element, operation: String, args: JSArray): JSValue {
        return when (operation) {
            "selectFirst" -> {
                val query = args.getProperty(0).cast(JSString::class.java).string
                createJsoupObject(element.selectFirst(query) ?: return createJSNull())
            }
            "select" -> {
                val query = args.getProperty(0).cast(JSString::class.java).string
                createJSValueFrom(element.select(query)) { createJsoupObject(it) }
            }
            "selectXpath" -> {
                val query = args.getProperty(0).cast(JSString::class.java).string
                createJSValueFrom(element.selectXpath(query)) { createJsoupObject(it) }
            }
            "getElementsByTag" -> {
                val query = args.getProperty(0).cast(JSString::class.java).string
                createJSValueFrom(element.getElementsByTag(query)) { createJsoupObject(it) }
            }
            "getElementById" -> {
                val query = args.getProperty(0).cast(JSString::class.java).string
                createJsoupObject(element.getElementById(query) ?: return createJSNull())
            }
            "getElementsByClass" -> {
                val query = args.getProperty(0).cast(JSString::class.java).string
                createJSValueFrom(element.getElementsByClass(query)) { createJsoupObject(it) }
            }
            "getElementsByAttribute" -> {
                val key = args.getProperty(0).cast(JSString::class.java).string
                createJSValueFrom(element.getElementsByAttribute(key)) { createJsoupObject(it) }
            }
            "allElements" -> createJSValueFrom(element.allElements) { createJsoupObject(it) }
            "child" -> {
                try {
                    val index = args.getProperty(0).cast(JSNumber::class.java).int
                    createJsoupObject(element.child(index))
                } catch (e: Exception) { createJSNull() }
            }
            "children" -> createJSValueFrom(element.children()) { createJsoupObject(it) }
            "parent" -> createJsoupObject(element.parent() ?: return createJSNull())
            "parents" -> createJSValueFrom(element.parents()) { createJsoupObject(it) }
            "siblingIndex" -> createJSNumber(element.siblingIndex())
            "elementSiblingIndex" -> createJSNumber(element.elementSiblingIndex())
            "siblingElements" -> createJSValueFrom(element.siblingElements()) { createJsoupObject(it) }
            "getElementsByIndexEquals" -> {
                val index = args.getProperty(0).cast(JSNumber::class.java).int
                createJSValueFrom(element.getElementsByIndexEquals(index)) { createJsoupObject(it) }
            }
            "getElementsByIndexGreaterThan" -> {
                val index = args.getProperty(0).cast(JSNumber::class.java).int
                createJSValueFrom(element.getElementsByIndexGreaterThan(index)) { createJsoupObject(it) }
            }
            "getElementsByIndexLessThan" -> {
                val index = args.getProperty(0).cast(JSNumber::class.java).int
                createJSValueFrom(element.getElementsByIndexLessThan(index)) { createJsoupObject(it) }
            }
            "firstElementSibling" -> createJsoupObject(element.firstElementSibling() ?: return createJSNull())
            "lastElementSibling" -> createJsoupObject(element.lastElementSibling() ?: return createJSNull())
            "previousElementSibling" -> createJsoupObject(element.previousElementSibling() ?: return createJSNull())
            "nextElementSibling" -> createJsoupObject(element.nextElementSibling() ?: return createJSNull())
            "cssSelector" -> createJSString(element.cssSelector())
            "tagName" -> createJSString(element.tagName())
            "id" -> createJSString(element.id())
            "className" -> createJSString(element.className())
            "classNames" -> createJSValueFrom(element.classNames())
            "attr" -> {
                val key = args.getProperty(0).cast(JSString::class.java).string
                createJSString(element.attr(key))
            }
            "text" -> createJSString(element.text())
            "ownText" -> createJSString(element.ownText())
            "wholeText" -> createJSString(element.wholeText())
            "html" -> {
                if (args.length == 0) return createJSString(element.html())
                val html = args.getProperty(0).cast(JSString::class.java).string
                createJsoupObject(element.html(html))
            }
            "outerHtml" -> createJSString(element.outerHtml())
            "toString" -> createJSString(element.toString())
            "data" -> createJSString(element.data())
            "textNodes" -> createJSValueFrom(element.textNodes()) { createJsoupObject(it) }
            "dataNodes" -> createJSValueFrom(element.dataNodes()) { createJsoupObject(it) }
            "dataset" -> createJSValueFrom(element.dataset())
            "before" -> {
                val html = args.getProperty(0).cast(JSString::class.java).string
                createJsoupObject(element.before(html))
            }
            "after" -> {
                val html = args.getProperty(0).cast(JSString::class.java).string
                createJsoupObject(element.after(html))
            }
            "append" -> {
                val html = args.getProperty(0).cast(JSString::class.java).string
                createJsoupObject(element.append(html))
            }
            "prepend" -> {
                val html = args.getProperty(0).cast(JSString::class.java).string
                createJsoupObject(element.prepend(html))
            }
            "remove" -> {
                if (args.length == 0) return createJsoupObject(element.apply { remove() })
                val query = args.getProperty(0).cast(JSString::class.java).string
                element.select(query).remove()
                createJSObject(element)
            }
            "wrap" -> {
                val html = args.getProperty(0).cast(JSString::class.java).string
                createJsoupObject(element.wrap(html))
            }
            "unwrap" -> {
                if (args.length == 0) return createJsoupObject(element.apply{ unwrap() })
                val query = args.getProperty(0).cast(JSString::class.java).string
                element.select(query).unwrap()
                createJSObject(element)
            }
            "`val`" -> createJSString(element.`val`())
            "`is`" -> {
                val query = args.getProperty(0).cast(JSString::class.java).string
                createJSBoolean(element.`is`(query))
            }
            "hasClass" -> {
                val className = args.getProperty(0).cast(JSString::class.java).string
                createJSBoolean(element.hasClass(className))
            }
            "hasAttr" -> {
                val key = args.getProperty(0).cast(JSString::class.java).string
                createJSBoolean(element.hasAttr(key))
            }
            "hasText" -> createJSBoolean(element.hasText())
            "hasParent" -> createJSBoolean(element.hasParent())
            "childrenSize" -> createJSNumber(element.childrenSize())
            "baseUri" -> createJSString(element.baseUri())
            "absUrl" -> {
                val absUrl = args.getProperty(0).cast(JSString::class.java).string
                createJSString(element.absUrl(absUrl))
            }
            else -> createJSNull()
        }
    }

    private fun JSContext.nodeOperations(node: Node, operation: String, args: JSArray): JSValue {
        return when (operation) {
            "nodeName" -> createJSString(node.nodeName())
            "outerHtml" -> createJSString(node.outerHtml())
            else -> createJSNull()
        }
    }

    private fun JSContext.textNodeOperations(node: TextNode, operation: String, args: JSArray): JSValue {
        return when (operation) {
            "text" -> createJSString(node.text())
            "outerHtml" -> createJSString(node.outerHtml())
            "wholeText" -> createJSString(node.wholeText)
            else -> nodeOperations(node, operation, args)
        }
    }

    private fun JSContext.dataNodeOperations(node: DataNode, operation: String, args: JSArray): JSValue {
        return when (operation) {
            "wholeData" -> createJSString(node.wholeData)
            else -> nodeOperations(node, operation, args)
        }
    }

    private fun JSContext.connectionOperations(connection: Connection, operation: String, args: JSArray): JSValue {
        return when (operation) {
            "method" -> {
                try {
                    val method = args.getProperty(0).cast(JSString::class.java).string.uppercase()
                    createJsoupObject(connection.method(Connection.Method.valueOf(method)))
                } catch (e: Exception) { createJSNull() }
            }
            "data" -> {
                val data = args.getProperty(0).toMap(this, "data")
                createJsoupObject(connection.data(data))
            }
            "cookie" -> {
                val key = args.getProperty(0).cast(JSString::class.java).string
                val value = args.getProperty(1).cast(JSString::class.java).string
                createJsoupObject(connection.cookie(key, value))
            }
            "cookies" -> {
                val cookies = args.getProperty(0).toMap(this, "cookies")
                createJsoupObject(connection.cookies(cookies))
            }
            "header" -> {
                val key = args.getProperty(0).cast(JSString::class.java).string
                val value = args.getProperty(1).cast(JSString::class.java).string
                createJsoupObject(connection.header(key, value))
            }
            "headers" -> {
                val headers = args.getProperty(0).toMap(this, "headers")
                createJsoupObject(connection.headers(headers))
            }
            "userAgent" -> {
                val userAgent = args.getProperty(0).cast(JSString::class.java).string
                createJsoupObject(connection.userAgent(userAgent))
            }
            "get" -> try {
                createJsoupObject(connection.get())
            } catch (e: Exception) { createJSNull() }
            "post" -> try{
                createJsoupObject(connection.post())
            } catch (e: Exception) { createJSNull() }
            "execute" -> try {
                createJsoupObject(connection.execute())
            } catch (e: Exception) { createJSNull() }
            "response" -> createJsoupObject(connection.response())
            "url" -> {
                val url = args.getProperty(0).cast(JSString::class.java).string
                createJsoupObject(connection.url(url))
            }
            "timeout" -> {
                val timeout = args.getProperty(0).cast(JSNumber::class.java).int
                createJsoupObject(connection.timeout(timeout))
            }
            "referrer" -> {
                val referrer = args.getProperty(0).cast(JSString::class.java).string
                createJsoupObject(connection.referrer(referrer))
            }
            "requestBody" -> {
                val requestBody = args.getProperty(0).cast(JSString::class.java).string
                createJsoupObject(connection.requestBody(requestBody))
            }
            "postDataCharset" -> {
                val charset = args.getProperty(0).cast(JSString::class.java).string
                createJsoupObject(connection.postDataCharset(charset))
            }
            "ignoreContentType" -> {
                val ignoreContentType = args.getProperty(0).cast(JSBoolean::class.java).boolean
                createJsoupObject(connection.ignoreContentType(ignoreContentType))
            }
            "ignoreHttpErrors" -> {
                val ignoreHttpErrors = args.getProperty(0).cast(JSBoolean::class.java).boolean
                createJsoupObject(connection.ignoreHttpErrors(ignoreHttpErrors))
            }
            "followRedirects" -> {
                val followRedirects = args.getProperty(0).cast(JSBoolean::class.java).boolean
                createJsoupObject(connection.followRedirects(followRedirects))
            }
            "maxBodySize" -> {
                val maxBodySize = args.getProperty(0).cast(JSNumber::class.java).int
                createJsoupObject(connection.maxBodySize(maxBodySize))
            }
            "toString" -> createJSString(connection.toString())
            else -> createJSNull()
        }
    }

    private fun JSContext.responseOperations(response: Connection.Response, operation: String, args: JSArray): JSValue {
        return when (operation) {
            "url" -> {
                if (args.length == 0) return createJSString(response.url().toString())
                val url = args.getProperty(0).cast(JSString::class.java).string
                createJsoupObject(response.url(URL(url)))
            }
            "charset" -> {
                if (args.length == 0) return createJSString(response.charset() ?: return createJSNull())
                val charset = args.getProperty(0).cast(JSString::class.java).string
                createJsoupObject(response.charset(charset))
            }
            "statusCode" -> createJSNumber(response.statusCode())
            "parse" -> createJsoupObject(response.parse())
            "body" -> createJSString(response.body())
            "headers" -> createJSValueFrom(response.headers())
            "contentType" -> createJSString(response.contentType() ?: return createJSNull())
            "cookies" -> createJSValueFrom(response.cookies())
            "toString" -> createJSString(response.toString())
            else -> createJSNull()
        }
    }

    private fun JSContext.createJsoup() {

        globalObject.setProperty("Parser", createJSObject().apply {
            setProperty("htmlParser", createJSObject(Parser.htmlParser()))
            setProperty("xmlParser", createJSObject(Parser.xmlParser()))
        })

        globalObject.setProperty("Jsoup", createJSObject().apply {
            setProperty("connect", createJSFunction { context, args ->
                val connection = Jsoup.connect(args[0].cast(JSString::class.java).string)
                return@createJSFunction context.createJsoupObject(connection)
            })
            setProperty("parse",
                createJSFunction { context, args ->
                    val html = args[0].cast(JSString::class.java).string
                    val url =
                        try { args[1].cast(JSString::class.java).string }
                        catch (e: Exception) { "" }
                    val document =
                        try {
                            val parser = args[2].cast(JSObject::class.java).javaObject as Parser
                            Jsoup.parse(html, url, parser)
                        } catch (e: Exception) { Jsoup.parse(html, url) }
                    return@createJSFunction context.createJsoupObject(document)
                }
            )
            setProperty("parseBodyFragment",
                createJSFunction { context, args ->
                    val html = args[0].cast(JSString::class.java).string
                    val url =
                        try { args[1].cast(JSString::class.java).string }
                        catch (e: Exception) { "" }
                    val document = Jsoup.parseBodyFragment(html, url)
                    return@createJSFunction context.createJsoupObject(document)
                }
            )
        })
    }

    fun inject(runtime: JSContext, filename: String) {

        runtime.evaluate("""
            const JsoupOperationsHandler = {
                get(target, prop) {
                    if (prop === 'JsoupObject') {
                        return target;
                    }
                    if (prop === 'type' || prop === 'isJsoupObject') {
                        return target[prop];
                    }
                    return function(...args) {
                        return JSOUP_OPERATIONS(target, prop, args);
                    }
                }
            }
            function jsoupObjectProxy(jsoupObject) {
                return new Proxy(jsoupObject, JsoupOperationsHandler);
            }
            function TO_STRING(value) {
                if (Array.isArray(value)) {
                    return JSON.stringify(value.map(item => TO_STRING(item)));
                }
                if (value.isJsoupObject) {
                    return value.toString();
                }
                return typeof value === 'object' && value !== null
                   ? JSON.stringify(value) : String(value)
            }
        """.trimIndent(), filename)

        runtime.createJsoup()

        runtime.jsoupOperations()
    }

    fun JSContext.createJSValueFrom(input: Any, action: (Any) -> JSValue = { createJSValueFrom(it) }): JSValue {
        return when (input) {
            is String -> createJSString(input)
            is Number -> createJSNumber(input.toDouble())
            is Boolean -> createJSBoolean(input)
            is Map<*, *> -> createJSObject().apply {
                input.forEach { (key, value) ->
                    setProperty(key.toString(), action(value ?: return@forEach))
                }
            }
            is Iterable<*> -> createJSArray().apply {
                input.forEach { item ->
                    setProperty(length, action(item ?: return@forEach))
                }
            }
            else -> createJsoupObject(input)
        }
    }

    private fun JSContext.createJsoupObject(obj: Any): JSValue {
        return createJSObject(obj).apply {
            val type = when (obj) {
                is Connection -> "connection"
                is Connection.Response -> "response"
                is Document -> "document"
                is Element -> "element"
                is Parser -> "parser"
                is TextNode -> "textNode"
                is DataNode -> "dataNode"
                is Node -> "node"
                else -> return createJSNull()
            }
            setProperty("type", createJSString(type))
            setProperty("isJsoupObject", createJSBoolean(true))
        }.let {
            globalObject.getProperty("jsoupObjectProxy").cast(JSFunction::class.java)
                .invoke(null, arrayOf(it))
        }
    }

    fun JSContext.string(value: JSValue): String {
        return globalObject.getProperty("TO_STRING").cast(JSFunction::class.java)
           .invoke(null, arrayOf(value)).cast(JSString::class.java).string
    }

    private fun JSValue.toMap(content: JSContext, mode: String): Map<String, String> {
        val (s, l) = when (mode) {
            "data" -> Pair("&", "=")
            "cookies" -> Pair(";", "=")
            "headers" -> Pair("\n", ":")
            else -> return emptyMap()
        }
        return when (this) {
            is JSString -> string.split(s).associate { item ->
                item.substringBefore(l).trim() to item.substringAfter(l).trim()
            }
            is JSArray -> (0 until length).associate { index ->
                val item = content.string(getProperty(index))
                item.substringBefore(l).trim() to item.substringAfter(l).trim()
            }
            is JSObject -> content.globalObject.getProperty("Object").cast(JSObject::class.java)
                .getProperty("keys").cast(JSFunction::class.java)
                .invoke(null, arrayOf<JSValue>(this)).cast(JSArray::class.java)
                .let { array ->
                    (0 until array.length).associate { index ->
                        val key = array.getProperty(index).cast(JSString::class.java).string
                        val value = content.string(getProperty(key))
                        key to value
                    }
                }
            else -> emptyMap()
        }
    }
}
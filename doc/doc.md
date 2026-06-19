LightInk 支持两种书源格式：

| 类型     | 扩展名   | 说明             |
|--------|-------|----------------|
| JSON书源 | .json | 声明式规则书源        |
| JS书源   | .js   | JavaScript脚本书源 |

## JSON书源

JSON书源的格式如下：

```json
{
  "name": "",
  "url": "",
  "version": 100,
  "search": {},
  "detail": {},
  "catalog": {},
  "chapter": {},
  "auth": {},
  "rank": []
}
```

### 基础信息

| 字段      | 类型     | 说明   |
|---------|--------|------|
| name    | String | 书源名称 |
| url     | String | 网站域名 |
| version | Int    | 版本号  |

> url是书源唯一标识，不应带有 http:// 或 https://，url相同的书源会被视为同一个书源

### 搜索

```json
{
    "search": {
      "url": "",
      "charset": "utf-8",
      "unit": 1,
      "page": -1,
      "list": "",
      "name": "",
      "author": "",
      "cover": "",
      "summary": "",
      "category": "",
      "status": "",
      "words": "",
      "tags": {
        "list": "",
        "item": ""
      },
      "update": "",
      "lastChapter": "",
      "other": "",
      "filter": "",
      "detail": ""
    }
}
```

| 字段          | 类型     | 说明                          |
|-------------|--------|-----------------------------|
| url         | String | 搜索URL，搜索关键词用`${key}`表示      |
| charset     | String | 关键词编码，默认编码为utf-8            |
| unit        | Int    | 页码单位                        |
| page        | Int    | 初始页码，默认值为-1 ,无分页            |
| list        | String | 书籍列表                        |
| name        | String | 书籍名称                        |
| author      | String | 书籍作者                        |
| cover       | String | 封面链接                        |
| summary     | String | 简介                          |
| category    | String | 分类                          |
| status      | String | 状态                          |
| words       | String | 字数                          |
| tags        | Object | 标签                          |
| tags.list   | String | 标签列表                        |
| tags.item   | String | 标签项                         |
| update      | String | 更新时间                        |
| lastChapter | String | 最新章节                        |
| other       | String | 其他信息                        |
| filter      | String | 过滤条件，默认过滤条件为作者非空且书名或作者含有关键词 |
| detail      | String | 详情页链接                       |

### 详情

```json
{
  "detail": {
    "name": "",
    "author": "",
    "cover": "",
    "summary": "",
    "category": "",
    "status": "",
    "words": "",
    "tags": {
      "list": "",
      "item": ""
    },
    "update": "",
    "lastChapter": "",
    "other": "",
    "catalog": ""
  }
}
```

| 字段          | 类型     | 说明   |
|-------------|--------|------|
| name        | String | 书籍名称 |
| author      | String | 书籍作者 |
| cover       | String | 封面链接 |
| summary     | String | 简介   |
| category    | String | 分类   |
| status      | String | 状态   |
| words       | String | 字数   |
| tags        | Object | 标签   |
| tags.list   | String | 标签列表 |
| tags.item   | String | 标签项  |
| update      | String | 更新时间 |
| lastChapter | String | 最新章节 |
| other       | String | 其他信息 |
| catalog     | String | 目录链接 |

### 目录

```json
{
  "catalog": {
    "list": "",
    "orderBy": 0,
    "booklet": {
      "name": "",
      "list": "",
      "url": ""
    },
    "name": "",
    "chapter": "",
    "date": "",
    "words": "",
    "page": ""
  }
}
```

| 字段           | 类型     | 说明                                                       |
|--------------|--------|----------------------------------------------------------|
| list         | String | 章节或分卷列表，若无booklet会将章节链接为空的视为卷章节                          |
| orderBy      | Int    | 排序方式：0 - 分卷正序章节正序，1 - 分卷倒序章节倒序，2 - 分卷正序章节倒序，3 - 分卷倒序章节正序 |
| booklet      | Object | 存在分卷                                                     |
| booklet.name | String | 分卷名称                                                     |
| booklet.list | String | 卷章节列表                                                    |
| booklet.url  | String | 章节列表链接，无此字段时，章节位于卷子级；字段留空时，章节与卷同级；字段非空时，尝试从链接提取章节列表      |
| name         | String | 章节名称                                                     |
| chapter      | String | 章节链接                                                     |
| date         | String | 更新时间                                                     |
| words        | String | 字数                                                       |
| page         | String | 下一页链接                                                    |

### 正文

```json
{
  "chapter": {
    "content": "",
    "filter": [],
    "purify": [],
    "page": ""
  }
}
```

| 字段      | 类型     | 说明                           |
|---------|--------|------------------------------|
| content | String | 正文内容                         |
| filter  | Array  | 过滤规则，支持 CSSQuery 和 标签(以@为前缀) |
| purify  | Array  | 净化规则，支持正则                    |
| page    | String | 下一页链接                        |

### 授权

```json
{
  "auth": {
    "login": "",
    "logged": "",
    "verify": "",
    "header": "",
    "cookie": "",
    "params": "",
    "vip": "",
    "buy": ""
  }
}
```

| 字段     | 类型     | 说明                         |
|--------|--------|----------------------------|
| login  | String | 登录URL                      |
| logged | String | 验证依据                       |
| verify | String | 验证URL                      |
| header | String | 统一请求头，${key}会被替换为相应cookie  |
| cookie | String | 登录依据                       |
| params | String | 统一请求参数，${key}会被替换为相应cookie |
| vip    | String | VIP章节判断依据                  |
| buy    | String | 已购章节判断依据                   |

### 排行榜

```json
{
  "rank": [
    {
      "title": "",
      "url": "",
      "unit": 1,
      "page": -1,
      "size": 20,
      "list": "",
      "name": "",
      "author": "",
      "cover": "",
      "summary": "",
      "detail": "",
      "category": "",
      "status": "",
      "words": "",
      "tags": {
        "list": "",
        "item": ""
      },
      "update": "",
      "lastChapter": "",
      "other": "",
      "categories": [
        {
          "value": "",
          "key": ""
        }
      ]
    }
  ]
}
```

| 字段          | 类型     | 说明                                 |
|-------------|--------|------------------------------------|
| title       | String | 组标题                                |
| url         | String | 组链接，类关键词用`${key}`表示，页码用`${page}`表示 |
| unit        | Int    | 页码单位                               |
| page        | Int    | 初始页码，默认值为-1 ,无分页                   |
| size        | Int    | 单次加载书籍数量                           |
| list        | String | 书籍列表                               |
| name        | String | 书籍名称                               |
| author      | String | 书籍作者                               |
| cover       | String | 封面链接                               |
| summary     | String | 简介                                 |
| detail      | String | 详情页链接                              |
| category    | String | 分类                                 |
| status      | String | 状态                                 |
| words       | String | 字数                                 |
| tags        | Object | 标签                                 |
| tags.list   | String | 标签列表                               |
| tags.item   | String | 标签项                                |
| update      | String | 更新时间                               |
| lastChapter | String | 最新章节                               |
| other       | String | 其他信息                               |
| categories  | Array  | 分组下子分类列表                           |
| value       | String | 分类标题                               |
| key         | String | 分类关键词                              |

> 省略的字段会使用search中的相应字段

### JSON源语法

表达式格式为`(CSSQuery + 属性/内容提取)/JSONPath + 操作符`

> `Jsoup Selector`用法见：https://jsoup.org/apidocs/org/jsoup/select/Selector
> `JSONPath`用法见：https://goessner.net/articles/JsonPath/index.html

#### 变量

| 变量              | 说明                                |
|-----------------|-----------------------------------|
| ${key}          | search.url中的搜索关键词或rank.url中的分类关键词 |
| ${page}         | rank.url中的页码                      |
| ${$params.name} | 当前请求 URL 中的相应的查询参数                |
| ${$}            | JSON响应体的完整内容                      |
| ${expression}   | 子表达式                              |

> 表达式嵌套仅当表达式结果为字符串或正文内容时支持，存在表达式嵌套时子表达式优先独立执行，外表达式视为字符串只支持操作符处理

### 属性提取

| 后缀              | 说明             |
|-----------------|----------------|
| @attr->attrName | 元素对应属性名的属性值    |
| @text           | 元素的文本内容        |
| @wholeText      | 元素的文本内容，包括子元素  |
| @ownText        | 元素的文本内容，不包括子元素 |
| @data           | 元素的data        |
| @textNodes      | textNode节点文本   |
| @href           | 节点链接           |
| @html           | 元素的inner HTML  |
| @outerHtml      | 元素的outer HTML  |

> 仅响应体为HTML或XML且返回结果应为字符串的表达式或正文内容列表时支持属性提取
> 在无属性/内容提取后缀时，若提取结果应为链接则默认提取href并自动补全链接，若为style标签或script标签则默认提取data，其他默认提取text

#### 操作符
| 操作符                            | 说明                                |
|--------------------------------|-----------------------------------|
| @post->RequestBody             | POST请求的请求体	                       |
| @header->key:value             | 请求头                               |
| @replace->old->new             | 文本替换                              |
| @replace->/regex/->replacement | 正则替换                              |
| @match->regex                  | 文本正则匹配                            |
| @equal->string                 | 判断字符串是否相同                         |
| @equalNot->string              | 判断字符串是否不同                         |
| @js->script->end               | 将当前字符串或列表结果作为变量$，并执行JavaScript脚本， |
| @decrypt->base64               | 解密base64编码的字符串                    |
| @url->link->experssion         | 从链接中提取内容                          |

> `@post->`和`@header->`仅结果应为链接时支持
> `@replace/match/equal/equalNot/decrypt`仅结果应为string或正文内容列表时时支持，正文列表在执行这些操作符时会先将结果转换为string再执行，最终结果变为string后不再支持过滤
> `@js->`执行脚本前会将前面的表达式结果赋值给$，之前的请求响应会储存在对象responseCache中，responseCache.search, responseCache.catalog, responseCache.rank为响应对象的矩阵，responseCache.detail和responseCache.chapter为单个响应对象，每个响应对象包含url和response两个属性，类型均为String，当前响应会也可通过responseCache.lastResponse访问
> `@url->`中`link`支持`@post->`和`@header->`

## JS书源

JS书源需实现的功能如下：

```js
/**
 * 搜索
 * @params {string} key
 * @returns {[{name, author, cover, summary, detail, category, update, words, status, [tag], other, filter}]}
 */
const search = (key) => {}

/**
 * 详情
 * @params {string} url
 * @returns {{end, books: [{name, author, cover, summary, status, category, words, update, lastChapter, [tag], other, catalog}]}}
 */
const detail = (url) => {}


/**
 * 目录
 * @params {string} url
 * @returns {[{name, url, vip, level, date, words}]}
 */
const catalog = (url) => {}

/**
 * 章节
 * @params {string} url
 * @returns {string}
 */
const chapter = (url) => {}

/**
* 登录验证
* @returns {boolean}
*/
const verify = () => {}


/**
 * 分类/排行/书城
 * @params {string} title
 * @params {string} category
 * @params {int} page
 * @returns {{end, books:[{name, author, cover, summary, detail, category, update, words, status, [tag], other}]}}
 */
const rank = (title, category, page) => {
}

var bookSource = JSON.stringify({
  name: '',
  url: '',
  version: 100,
  authorization: "",
  cookie: [],
  ranks: []
})
```

> 函数若返回值为对象，需使用 JSON.stringify() 编码成字符串

### 基础信息

```js
var bookSource = JSON.stringify({
  name: '',
  url: '',
  version: 100,
  authorization: "",
  cookie: [],
  ranks: ranks
})
```

| 字段            | 类型     | 说明    |
|---------------|--------|-------|
| name          | String | 书源名称  |
| url           | String | 网站域名  |
| version       | Int    | 版本号   |
| authorization | String | 登录链接  |
| cookie        | Array  | 登录依据  |
| ranks         | Array  | 排行榜列表 |

### 搜索

```js
/**
 * 搜索
 * @params {string} key // 关键词
 * @params {int} page // 页码，初始页码为1，页码单位为1
 * @returns {[{name, author, cover, summary, detail, category, update, words, status, [tag], other, filter}]}
 */
const search = (key, page) => {
    let response = GET(``)
    let $ = HTML.parse(response)
    let books = $('').map(item => {
        let book = HTML.parse(item)
        return {
            name: book('').text(),
            author: book('').text(),
            cover: BASE_URL + book('').attr('src'),
            category: book('').text(),
            update: book('').text(),
            words: book('').text(),
            status: book('').text(),
            tags: book('').text().split(' '),
            other: book('').text(),
            summary: book('').text(),
            detail: BASE_URL + book('').attr('href'),
            filter: true
        }
    })
    return JSON.stringify({
       end: books.length == 0,
       books
    })
}
```

| 字段       | 类型      | 说明                             |
|----------|---------|--------------------------------|
| books    | Array   | 书籍列表                           |
| end      | Boolean | 是否搜索完成，搜索完成时返回true，否则返回false   |
| name     | String  | 书籍名称                           |
| author   | String  | 书籍作者                           |
| cover    | String  | 封面链接                           |
| category | String  | 分类                             |
| update   | String  | 更新时间                           |
| words    | String  | 字数                             |
| status   | String  | 状态                             |
| tags     | Array   | 标签                             |
| other    | String  | 其他信息                           |
| summary  | String  | 简介                             |
| detail   | String  | 详情页链接                          |
| filter   | Boolean | 过滤规则，默认使用作者非空且书名或作者含有关键词作为过滤规则 |

> 若以books是否为空判断搜索是否完成可省略end直接返回JSON.stringify(books)

### 详情
```js
/**
 * 详情
 * @params {string} url // 详情页链接
 * @returns {[{name, author, cover, summary, status, category, words, update, lastChapter, [tag], other, catalog}]}
 */
const detail = (url) => {
    let response = GET(url)
    let $ = HTML.parse(response)
    let book = {
        name: $('').text(),
        author: $('').text(),
        cover: BASE_URL + $('').attr('src'),
        summary: $('').text(),
        status: $('').text(),
        category: $('').text(),
        words: $('').text(),
        update: $('').text(),
        lastChapter: $('').text(),
        tags: $('').text().split(' '),
        other: $('').text(),
        catalog: BASE_URL + $('').attr('href')
    }
    return JSON.stringify(book)
}
```

| 字段          | 类型     | 说明   |
|-------------|--------|------|
| name        | String | 书籍名称 |
| author      | String | 书籍作者 |
| cover       | String | 封面链接 |
| summary     | String | 简介   |
| status      | String | 状态   |
| category    | String | 分类   |
| words       | String | 字数   |
| update      | String | 更新时间 |
| lastChapter | String | 最新章节 |
| tags        | Array  | 标签   |
| other       | String | 其他信息 |
| catalog     | String | 目录链接 |

### 目录

```js
/**
 * 目录
 * @params {string} url // 目录页链接
 * @returns {[{name, url, vip, level, date, words}]}
 */
const catalog = (url) => {
    let response = GET(url)
    let $ = HTML.parse(response)
    let chapters = []
    $('').forEach(element => {
        let list = HTML.parse(element)('')
        switch (list.length) {
            case 1:
                chapters.push({ name: list.text() })
                break
            case 4:
                list.forEach(chapter => {
                    chapters.push({
                        name: chapter.text(),
                        url: BASE_URL + chapter.attr('href'),
                        level: true,
                    })
                })
                break
            default: break
        }
    })
    return JSON.stringify(chapters)
}
```

| 字段    | 类型      | 说明                    |
|-------|---------|-----------------------|
| name  | String  | 章节名称                  |
| url   | String  | 章节链接                  |
| vip   | Boolean | 是否VIP章节，默认值为false     |
| level | Boolean | 是否缩进，无缩进为卷章节，默认值为true |
| date  | String  | 更新时间                  |
| words | String  | 字数                    |

### 正文

```js
/**
 * 章节
 * @params {string} url // 章节链接
 * @returns {string}
 */
const chapter = (url) => {
    let response = GET(url)
    let $ = HTML.parse(response)
    let content = $('#content')
    return content.remove('ul#contentdp')
}
```

| 字段      | 类型     | 说明   |
|---------|--------|------|
| content | String | 正文内容 |

### 授权

```js
/**
* 登录验证
* @returns {boolean}
*/
const verify = () => {
    let response = GET(BASE_URL)
    let $ = HTML.parse(response)
    return $('').text() == ''
}
```

| 字段     | 类型      | 说明     |
|--------|---------|--------|
| result | Boolean | 登录验证结果 |

### 排行榜

```js
/**
 * 分类/排行/书城
 * @params {string} title // group关键词
 * @params {string} category // category关键词
 * @params {int} page // 页码
 * @returns {{end, books:[{name, author, cover, summary, detail, category, update, words, status, [tag], other}]}}
 */
const rank = (title, category, page) => {
    let query = title.replace('${key}', category).replace('${page}', page)
    let response = GET(BASE_URL + query)
    let $ = HTML.parse(response)
    let books = $('').map(element => {
    let book = HTML.parse(element)
        return {
            name: book('').text(),
            author: book('').text(),
            cover: BASE_URL + book('').attr('src'),
            category: book('').text(),
            update: book('').text(),
            words: book('').text(),
            status: book('').text(),
            tags: book('').text().split(' '),
            other: book('').text(),
            summary: book('').text(),
            detail: BASE_URL + book('').attr('href')
        }
    })
    return JSON.stringify({
        end: books.length == 0,
        books: books
    })
}
```

| 字段       | 类型      | 说明     |
|----------|---------|--------|
| books    | Array   | 书籍列表   |
| end      | Boolean | 列表是否结束 |
| name     | String  | 书籍名称   |
| author   | String  | 书籍作者   |
| cover    | String  | 封面链接   |
| category | String  | 分类     |
| update   | String  | 更新时间   |
| words    | String  | 字数     |
| status   | String  | 状态     |
| tags     | Array   | 标签     |
| other    | String  | 其他信息   |
| summary  | String  | 简介     |
| detail   | String  | 详情页链接  |

> 若以books是否为空判断列表是否结束可省略end直接返回JSON.stringify(books)

### 排行榜列表

```js
const ranks = [
    {
        title: { value: "",  key: "" },
        page: 1,
        unit: 1,
        categories: [
            { value: "", key: "" }
        ]
    }
]
```

| 字段         | 类型     | 说明       |
|------------|--------|----------|
| title      | String | 组标题      |
| page       | Int    | 初始页码     |
| unit       | Int    | 页码单位     |
| categories | Array  | 分组下子分类列表 |
| value      | String | 分类标题     |
| key        | String | 分类关键词    |

### api

#### 网络请求

| api                                                              | 返回值类型  | 说明       |
|------------------------------------------------------------------|--------|----------|
| GET/PUT/POST/DELETE/PATCH/HEAD/OPTIONS/TRACE(url, config?)       | String | 基于OkHttp |
| JSOUP_GET/PUT/POST/DELETE/PATCH/HEAD/OPTIONS/TRACE(url, config?) | String | 基于Jsoup  |

参数：

| 参数             | 类型     | 说明                        |
|----------------|--------|---------------------------|
| url            | String | 请求URL                     |
| config         | Object | 请求配置                      |
| config.data    | String | 请求体                       |
| config.headers | Array  | 请求头 ，"Key: Value"字符串数组    |
| config.charset | String | 自动识别有误时可指定请求体编码           |
| config.zip     | String | zip响应提取的文件名，基于OkHttp的请求可用 |

#### HTML 解析与提取

| api                                                 | 返回值类型  | 说明                                      |
|-----------------------------------------------------|--------|-----------------------------------------|
| SELLECT(html, cssQuery), HTML.parse(html)(cssQuery) | Array  | 匹配 CSS 选择器的元素数组的 outerHTML              |
| .text()                                             | String | 获取 HTML 片段或数组第一个元素的文本                   |
| .attr(attrName)                                     | String | 获取 HTML 片段或数组第一个元素的属性值                  |
| .remove(cssQuery)                                   | String | 移除 HTML 片段或数组中所有匹配的匹配的元素并返回修改后的 HTML 片段 |

#### 编码 / 解码

| api                       | 返回值类型  | 说明                     |
|---------------------------|--------|------------------------|
| ENCODE(content, charset?) | String | 对字符串编码（URL 编码或 Base64） |
| DECODE(content, charset?) | String | 解码（URL 解码或 Base64 解码）  |

> charset 默认为 utf8（URL 编码 / 解码），设为 base64 时执行 Base64 操作

#### Cookie 操作

| api               | 返回值类型     | 说明                       |
|-------------------|-----------|--------------------------|
| COOKIE(key)       | String    | 获取当前域名下指定 key 的 cookie 值 |
| SET_COOKIE(value) | Undefined | 设置当前域名的 cookie           |

#### 本地存储

| api                              | 返回值类型     | 说明                |
|----------------------------------|-----------|-------------------|
| localStorage.getItem(key)        | String    | 获取本地存储中指定 key 的值  |
| localStorage.setItem(key, value) | Undefined | 设置本地存储中指定 key 的值  |
| localStorage.removeItem(key)     | Undefined | 移除本地存储中指定 key 的值  |
| localStorage.clear()             | Undefined | 清空本地存储            |
| localStorage.key                 | Array     | 获取本地存储中所有 key 的数组 |
| localStorage.length              | Number    | 获取本地存储中 key 的数量   |

#### 控制台输出

| api              | 返回值类型     | 说明            |
|------------------|-----------|---------------|
| console.log(msg) | Undefined | 将内容输出到应用的日志视图 |

#### 模块加载

| api                 | 返回值类型     | 说明     |
|---------------------|-----------|--------|
| require(dependency) | Undefined | 加载依赖模块 |

#### URL 参数提取

| api               | 返回值类型  | 说明                 |
|-------------------|--------|--------------------|
| String.query(key) | String | 提取 URL 参数中的 key 的值 |

#### Jsoup

##### 全局对象与函数

* Jsoup 对象

| 方法                                | 参数                                                  | 返回值           | 说明                                                                                 |
|-----------------------------------|-----------------------------------------------------|---------------|------------------------------------------------------------------------------------|
| connect(url)                      | url: string                                         | Connection 对象 | 创建一个新的 HTTP 连接                                                                     |
| parse(html, baseUrl?, parser?)    | html: string<br>baseUrl?: string<br>parser?: Parser | Document 对象   | 将 HTML 字符串解析为 Document，可选 baseUrl 用于解析绝对路径，可选 parser 指定解析器（htmlParser 或 xmlParser） |
| parseBodyFragment(html, baseUrl?) | html: string<br>baseUrl?: string                    | Document 对象   | 将 HTML 片段解析为文档                                                                     |

* Parser 对象

| 属性/方法        | 返回值       | 说明            |
|--------------|-----------|---------------|
| htmlParser() | Parser 对象 | 获取 HTML 解析器实例 |
| xmlParser()  | Parser 对象 | 获取 XML 解析器实例  |

##### 支持的对象类型及主要操作

| 类型         | JavaScript 中的表示                                  | 说明               |
|------------|--------------------------------------------------|------------------|
| Element    | 代理对象，带有 isJsoupObject: true 和 type: "element"    | 代表一个 HTML/XML 元素 |
| Document   | 代理对象，带有 isJsoupObject: true 和 type: "document"   | 代表整个文档           |
| Connection | 代理对象，带有 isJsoupObject: true 和 type: "connection" | 代表一个 HTTP 连接配置   |
| Response   | 代理对象，带有 isJsoupObject: true 和 type: "response"   | 代表一个 HTTP 响应     |
| TextNode   | 代理对象，带有 isJsoupObject: true 和 type: "textNode"   | 代表一个文本节点         |
| DataNode   | 代理对象，带有 isJsoupObject: true 和 type: "dataNode"   | 代表一个数据节点         |
| Node       | 代理对象，带有 isJsoupObject: true 和 type: "node"       | 通用节点             |

* Document 对象

| 方法         | 返回值        | 说明           |
|------------|------------|--------------|
| title()    | string     | 获取文档标题       |
| head()     | Element 对象 | 获取 <head> 元素 |
| body()     | Element 对象 | 获取 <body> 元素 |
| charset()  | string     | 获取文档字符集名称    |
| location() | string     | 获取文档 URL     |

> Document 对象继承自 Element ，支持 Element的所有操作

* Element 对象

| 方法                                   | 参数                   | 返回值             | 说明                       |
|--------------------------------------|----------------------|-----------------|--------------------------|
| selectFirst(cssQuery)                | cssQuery: string     | Element 或 null  | 返回匹配 CSS 选择器的第一个元素       |
| select(cssQuery)                     | cssQuery: string     | Array<Element>  | 返回匹配 CSS 选择器的元素集合        |
| selectXpath(xpathQuery)              | xpathQuery: string   | Array<Element>  | 使用 XPath 选择元素            |
| getElementById(id)                   | id: string           | Element 或 null  | 通过 id 获取元素               |
| getElementsByTag(tagName)            | tagName: string      | Array<Element>  | 通过标签名获取元素集合              |
| getElementsByClass(className)        | className: string    | Array<Element>  | 通过类名获取元素集合               |
| getElementsByAttribute(key)          | key: string          | Array<Element>  | 通过属性名获取元素集合              |
| allElements                          |                      | Array<Element>  | 获取所有后代元素集合               |
| child(index)                         | index: number        | Element 或 null  | 获取第 index 个子元素           |
| children()                           |                      | Array<Element>  | 获取所有子元素集合                |
| parent()                             |                      | Element 或 null  | 获取父元素                    |
| parents()                            |                      | Array<Element>  | 获取所有祖先元素集合               |
| siblingIndex()                       |                      | number          | 获取在父节点中的索引（包含文本节点等）      |
| elementSiblingIndex()                |                      | number          | 获取在同辈元素中的索引              |
| siblingElements()                    |                      | Array<Element>  | 获取所有同辈元素                 |
| getElementsByIndexEquals(index)      | index: number        | Array<Element>  | 获取指定索引的元素集合（索引基于元素集合）    |
| getElementsByIndexGreaterThan(index) | index: number        | Array<Element>  | 获取索引大于指定值的元素集合           |
| getElementsByIndexLessThan(index)    | index: number        | Array<Element>  | 获取索引小于指定值的元素集合           |
| firstElementSibling()                |                      | Element 或 null  | 第一个同辈元素                  |
| lastElementSibling()                 |                      | Element 或 null  | 最后一个同辈元素                 |
| previousElementSibling()             |                      | Element 或 null  | 前一个同辈元素                  |
| nextElementSibling()                 |                      | Element 或 null  | 后一个同辈元素                  |
| cssSelector()                        |                      | string          | 获取生成该元素的 CSS 选择器路径       |
| tagName()                            |                      | string          | 获取标签名                    |
| id()                                 |                      | string          | 获取 id 属性值                |
| className()                          |                      | string          | 获取 class 属性值             |
| classNames()                         |                      | Array<string>   | 获取所有 class 名称的数组         |
| attr(key)                            | key: string          | string          | 获取属性值                    |
| text()                               |                      | string          | 获取元素及其子元素的文本内容           |
| ownText()                            |                      | string          | 获取元素自身的文本（不包括子元素）        |
| wholeText()                          |                      | string          | 获取包含空白符的未标准化文本           |
| html()                               |                      | string          | 返回内部 HTML 字符串            |
| html(html)                           | html: string         | Element         | 设置内部 HTML 并返回当前元素        |
| outerHtml()                          |                      | string          | 获取元素的外层 HTML 字符串         |
| toString()                           |                      | string          | 返回元素的字符串表示               |
| data()                               |                      | string          | 获取元素的数据内容                |
| textNodes()                          |                      | Array<TextNode> | 获取包含的文本节点集合              |
| dataNodes()                          |                      | Array<DataNode> | 获取包含的数据节点集合              |
| dataset()                            |                      | object          | 获取自定义属性 data-* 的键值对对象    |
| before(html)                         | html: string         | Element         | 在当前元素之前插入 HTML 内容，返回当前元素 |
| after(html)                          | html: string         | Element         | 在当前元素之后插入 HTML 内容，返回当前元素 |
| append(html)                         | html: string         | Element         | 在元素内部末尾插入 HTML 内容，返回当前元素 |
| prepend(html)                        | html: string         | Element         | 在元素内部开头插入 HTML 内容，返回当前元素 |
| remove()                             |                      | void            | 移除当前元素                   |
| remove(query)                        | query: string        | Element         | 移除匹配 CSS 选择器的子元素，返回当前元素  |
| wrap(html)                           | html: string         | Element         | 用给定的 HTML 包裹当前元素，返回当前元素  |
| unwrap()                             |                      | void            | 无参数时移除当前元素的父元素           |
| unwrap(query)                        | query: string        | Element         | 移除匹配的子元素的父元素，返回当前元素      |
| val()                                |                      | string          | 获取表单元素的值（value 属性）       |
| is(cssQuery)                         | cssQuery: string     | boolean         | 判断元素是否匹配 CSS 选择器         |
| hasClass(className)                  | className: string    | boolean         | 是否包含指定的 class            |
| hasAttr(key)                         | key: string          | boolean         | 是否有指定属性                  |
| hasText()                            |                      | boolean         | 是否有非空白文本内容               |
| hasParent()                          |                      | boolean         | 是否有父节点                   |
| childrenSize()                       |                      | number          | 子元素的数量（不包括文本节点等）         |
| href()                               |                      | string          | 获取 href 属性值              |
| src(url)                             | url: string          | string          | 返回补全的 URL                |
| baseUri()                            |                      | string          | 获取基础 URI                 |
| absUrl(attributeKey)                 | attributeKey: string | string          | 获取属性值的绝对 URL             |

* Connection 对象（HTTP 请求配置）

| 方法                        | 参数                               | 返回值           | 说明                                                      |
|---------------------------|----------------------------------|---------------|---------------------------------------------------------|
| method(method)            | method: string                   | Connection 对象 | 设置请求方法，返回自身以链式调用                                        |
| data(data)                | data: string 或 Array 或 object    | Connection 对象 | 设置请求参数，支持格式："a=b&c=d"、["a=b","c=d"]、{a:"b", c:"d"}      |
| cookie(key, value)        | key: string, value: string       | Connection 对象 | 设置单个 Cookie                                             |
| cookies(cookies)          | cookies: string 或 Array 或 object | Connection 对象 | 设置多个 Cookie，支持格式："a=b;c=d"、["a=b","c=d"]、{a:"b", c:"d"} |
| header(key, value)        | key: string, value: string       | Connection 对象 | 设置单个请求头                                                 |
| headers(headers)          | headers: string 或 Array 或 object | Connection 对象 | 设置多个请求头，支持格式："a:b\nc:d"、["a:b","c:d"]、{a:"b", c:"d"}    |
| userAgent(userAgent)      | userAgent: string                | Connection 对象 | 设置 User-Agent 头                                         |
| timeout(millis)           | millis: number                   | Connection 对象 | 设置超时时间（毫秒）                                              |
| referrer(referrer)        | referrer: string                 | Connection 对象 | 设置 Referer 头                                            |
| requestBody(body)         | body: string                     | Connection 对象 | 设置请求体                                                   |
| postDataCharset(charset)  | charset: string                  | Connection 对象 | 设置 POST 数据的字符集                                          |
| ignoreContentType(ignore) | ignore: boolean                  | Connection 对象 | 是否忽略 Content-Type 检查                                    |
| ignoreHttpErrors(ignore)  | ignore: boolean                  | Connection 对象 | 是否忽略 HTTP 错误状态码                                         |
| followRedirects(follow)   | follow: boolean                  | Connection 对象 | 是否跟随重定向                                                 |
| maxBodySize(bytes)        | bytes: number                    | Connection 对象 | 限制响应体最大字节数                                              |
| get()                     |                                  | Response 对象   | 执行 GET 请求，返回 Response                                   |
| post()                    |                                  | Response 对象   | 执行 POST 请求，返回 Response                                  |
| execute()                 |                                  | Response 对象   | 执行请求（根据当前 method），返回 Response                           |
| response()                |                                  | Response 对象   | 获取当前连接的响应对象（需先执行请求）                                     |
| url(url)                  | url: string                      | Connection 对象 | 设置请求 URL                                                |
| toString()                |                                  | string        | 返回 Connection 的字符串表示                                    |

* Response 对象

| 方法            | 参数             | 返回值           | 说明                 |
|---------------|----------------|---------------|--------------------|
| url()         |                | string        | 获取响应 URL           |
| url(newUrl)   | newUrl: string | Response 对象   | 设置响应 URL           |
| charset()     |                | string 或 null | 获取响应的字符集           |
| charset(name) | name: string   | Response 对象   | 设置响应的字符集           |
| statusCode()  |                | number        | 获取 HTTP 状态码        |
| parse()       |                | Document 对象   | 将响应体解析为 Document   |
| body()        |                | string        | 获取响应体字符串           |
| headers()     |                | object        | 获取响应头键值对对象         |
| contentType() |                | string 或 null | 获取 Content-Type 头  |
| cookies()     |                | object        | 获取 Cookie 键值对对象    |
| toString()    |                | string        | 返回 Response 的字符串表示 |

* TextNode 对象

| 方法          | 返回值    | 说明                 |
|-------------|--------|--------------------|
| text()      | string | 获取文本节点的内容（不包含空白修正） |
| wholeText   | string | 获取包含原始空白符的完整文本     |
| outerHtml() | string | 获取文本节点的 HTML 表示    |
| nodeName()  | string | 返回 "#text"         |

* DataNode 对象

| 方法          | 返回值    | 说明              |
|-------------|--------|-----------------|
| wholeData   | string | 获取数据节点的完整原始数据   |
| outerHtml() | string | 获取数据节点的 HTML 表示 |
| nodeName()  | string | 返回 "#data"      |

* Node 对象

| 方法          | 返回值    | 说明               |
|-------------|--------|------------------|
| nodeName()  | string | 获取节点名称           |
| outerHtml() | string | 获取节点的外层 HTML 字符串 |

> Element、TextNode、DataNode 均继承自 Node，也支持上述两个方法

#### 辅助函数

| 函数               | 返回值类型  | 说明                                                               |
|------------------|--------|------------------------------------------------------------------|
| TO_STRING(value) | String | 将任意 JavaScript 值转换为字符串。Jsoup 对象会调用其 Java 的 toString() 方法，数组会递归处理 |

## 导入

书源支持本地导入和网址导入两种方式，本地导入同时支持单个文件和仓库导入，书源类型识别以文件名后缀为准，网址导入只支持仓库导入
仓库索引文件格式为 JSON，内容是书源文件名的数组，源文件应位于`sources`目录下
仓库导入默认书源为json源，可通过`‘---js---’`和`‘---json---’`切换至js源或切换回json源，也可以在索引文件名中补全后缀表明单个源文件的类型

## 书源和仓库示例

```markdown
[repository.json](repository.json)
    [novel.tingroom.com](sources/novel.tingroom.com.json)
    [www.wenku8.net](sources/www.wenku8.net.js)
    [www.xyyuedu.com](sources/www.xyyuedu.com.json)
    [zq.bookan.com.cn](sources/zq.bookan.com.cn.js)

```

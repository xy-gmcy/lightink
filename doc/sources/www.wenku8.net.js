/**
 * 一些约定如下：
 *
 * 1.如果需要 AES 能力，请在头部声明 require("crypto-js");
 * 声明后可直接使用 CryptoJS (用法同 crypto-js 库)
 *
 * 2.若需要存储数据，可使用 localStorage 函数，用法同浏览器环境
 *
 * 3.书源内若返回值为对象，均需要使用 JSON.stringify() 进行编码
 */

const BASE_URL = 'https://www.wenku8.net'

/**
 * 搜索
 * @params {string} key
 * @returns {[{name, author, cover, summary, detail, category, update, words, status, [tag], other, filter}]}
 */
const search = (key, page) => {
    let response = GET(`https://www.wenku8.net/modules/article/search.php?searchtype=articlename&searchkey=${ENCODE(key, 'gbk')}&page=${page}`)
    let $ = Jsoup.parse(response)
    return JSON.stringify(getBooks($))
}

/**
 * 详情
 * @params {string} url
 * @returns {[{name, author, cover, summary, status, category, words, update, lastChapter, [tag], other, catalog}]}
 */
const detail = (url) => {
    let response = GET(url)
    let $ = Jsoup.parse(response)
    return JSON.stringify(getDetail($))
}


/**
 * 目录
 * @params {string} url
 * @returns {[{name, url, vip, level, date, words}]}
 */
const catalog = (url) => {
    let $ = Jsoup.connect(url).method('GET').execute().charset('gbk').parse()
    return JSON.stringify($.select('table.css > tbody > tr').flatMap(element => {
        if (element.childrenSize() === 1) return { name: element.text(), level: false }
        return element.select('a').map(chapter => ({
            name: chapter.text(),
            url: url.replace('index.htm', '') + chapter.attr('href'),
            level: true,
        }))
    }))
}

/**
 * 章节
 * @params {string} url
 * @returns {string}
 */
const chapter = (url) => {
    let response = GET(url)
    let $ = Jsoup.parse(response)
    let content = $.selectFirst('#content').remove('ul#contentdp')
    return content.outerHtml()
}

/**
* 登录验证
* @returns {boolean}
*/
const verify = () => {
    let response = GET('https://www.wenku8.net/index.php')
    let $ = HTML.parse(response)
    return $('.main > .fl > a').text() === '退出登录'
}


/**
 * 分类/排行/书城
 * @params {string} title
 * @params {string} category
 * @params {int} page
 * @returns {{end, books:[{name, author, cover, summary, detail, category, update, words, status, [tag], other}]}}
 */
const rank = (title, category, page) => {
    let query = title.replace('${key}', category).replace('${page}', page)
    let response = GET(`https://www.wenku8.net/modules/article/${query}`)
    let $ = Jsoup.parse(response)
    if (title.match(/bookcase/) != null) {
        let books = $.select('table.grid > tbody > tr:not(:first-child):not(:last-child)').map(book => {
            let url = book.selectFirst('td:nth-child(2) > a').attr('href')
            let id = Number(url.query('aid'))
            return {
                name: book.selectFirst('td:nth-child(2) > a').text(),
                author: book.selectFirst('td:nth-child(3) > a').text(),
                cover: `http://img.wenku8.com/image/${Math.floor(id/1000)}/${id}/${id}s.jpg`,
                update: book.selectFirst('td:nth-child(6)').text(),
                lastChapter: book.selectFirst('td:nth-child(4) > a').text(),
                status: book.selectFirst('td:nth-child(2)').text().match('[完结]') ? '完结' : '连载',
                detail: url
            }
        })
        return JSON.stringify({
            end: true,
            books: books
        })
    }
    return JSON.stringify(getBooks($))
}

const ranks = [
    {
        title: { value: "轻小说列表",  key: "articlelist.php?initial=${key}&page=${page}" },
        page: 1,
        categories: [
            { value: "全部", key: "" }, { value: "0-9", key: "1" },
            { value: "A", key: "A" }, { value: "B", key: "B" },
            { value: "C", key: "C" }, { value: "D", key: "D" },
            { value: "E", key: "E" }, { value: "F", key: "F" },
            { value: "G", key: "G" }, { value: "H", key: "H" },
            { value: "I", key: "I" }, { value: "J", key: "J" },
            { value: "K", key: "K" }, { value: "L", key: "L" },
            { value: "M", key: "M" }, { value: "N", key: "N" },
            { value: "O", key: "O" }, { value: "P", key: "P" },
            { value: "Q", key: "Q" }, { value: "R", key: "R" },
            { value: "S", key: "S" }, { value: "T", key: "T" },
            { value: "U", key: "U" }, { value: "V", key: "V" },
            { value: "W", key: "W" }, { value: "X", key: "X" },
            { value: "Y", key: "Y" }, { value: "Z", key: "Z" }
        ]
    },
    {
        title: { value: "排行榜",  key: "toplist.php?sort=${key}&page=${page}" },
        page: 1,
        categories: [
            { value: "总排行榜", key: "allvisit" }, { value: "月排行榜", key: "monthvisit" },
            { value: "周排行榜", key: "weekvisit" }, { value: "日排行榜", key: "dayvisit" },
            { value: "总推荐榜", key: "allvote" }, { value: "月推荐榜", key: "monthvote" },
            { value: "周推荐榜", key: "weekvote" }, { value: "日推荐榜", key: "dayvote" },
            { value: "已动画化", key: "anime" }, { value: "最近更新", key: "lastupdate" },
            { value: "最新入库", key: "postdate" }, { value: "总收藏榜", key: "goodnum" },
            { value: "字数排行", key: "size" }
        ]
    },
    {
        title: { value: "分类阅读",  key: "articlelist.php?class=${key}&page=${page}" },
        page: 1,
        categories: [
            { value: "全部轻小说", key: "" }, { value: "电击文库", key: "1" },
            { value: "富士见文库", key: "2" }, { value: "角川文库", key: "3" },
            { value: "MF文库J", key: "4" }, { value: "Fami通文库", key: "5" },
            { value: "GA文库", key: "6" }, { value: "HJ文库", key: "7" },
            { value: "一迅社", key: "8" }, { value: "集英社", key: "9" },
            { value: "小学馆", key: "10" }, { value: "讲谈社", key: "11" },
            { value: "少女文库", key: "12" }, { value: "其他文库", key: "13" },
            { value: "游戏剧本", key: "14" }
        ]
    },
    {
        title: { value: "完结轻小说列表",  key: "articlelist.php?fullflag=1&initial=${key}&page=${page}" },
        page: 1,
        categories: [
            { value: "全部", key: "" }, { value: "0-9", key: "1" },
            { value: "A", key: "A" }, { value: "B", key: "B" },
            { value: "C", key: "C" }, { value: "D", key: "D" },
            { value: "E", key: "E" }, { value: "F", key: "F" },
            { value: "G", key: "G" }, { value: "H", key: "H" },
            { value: "I", key: "I" }, { value: "J", key: "J" },
            { value: "K", key: "K" }, { value: "L", key: "L" },
            { value: "M", key: "M" }, { value: "N", key: "N" },
            { value: "O", key: "O" }, { value: "P", key: "P" },
            { value: "Q", key: "Q" }, { value: "R", key: "R" },
            { value: "S", key: "S" }, { value: "T", key: "T" },
            { value: "U", key: "U" }, { value: "V", key: "V" },
            { value: "W", key: "W" }, { value: "X", key: "X" },
            { value: "Y", key: "Y" }, { value: "Z", key: "Z" }
        ]
    },
    {
        title: { value: "日常系属性Tags",  key: "tags.php?t=${key}&page=${page}" },
        page: 1,
        categories: [
            { value: "校园", key: "%D0%A3%D4%B0" }, { value: "青春", key: "%C7%E0%B4%BA" },
            { value: "恋爱", key: "%C1%B5%B0%AE" }, { value: "治愈", key: "%D6%CE%D3%FA" },
            { value: "群像", key: "%C8%BA%CF%F1" }, { value: "竞技", key: "%BE%BA%BC%BC" },
            { value: "音乐", key: "%D2%F4%C0%D6" }, { value: "美食", key: "%C3%C0%CA%B3" },
            { value: "旅行", key: "%C2%C3%D0%D0" }, { value: "欢乐向", key: "%BB%B6%C0%D6%CF%F2" },
            { value: "经营", key: "%BE%AD%D3%AA" }, { value: "职场", key: "%D6%B0%B3%A1" },
            { value: "斗智", key: "%B6%B7%D6%C7" }, { value: "脑洞", key: "%C4%D4%B6%B4" },
            { value: "宅文化", key: "%D5%AC%CE%C4%BB%AF" }
        ]
    },
    {
        title: { value: "幻想系属性Tags",  key: "tags.php?t=${key}&page=${page}" },
        page: 1,
        categories: [
            { value: "穿越", key: "%B4%A9%D4%BD" }, { value: "奇幻", key: "%C6%E6%BB%C3" },
            { value: "魔法", key: "%C4%A7%B7%A8" }, { value: "异能", key: "%D2%EC%C4%DC" },
            { value: "战斗", key: "%D5%BD%B6%B7" }, { value: "科幻", key: "%BF%C6%BB%C3" },
            { value: "机战", key: "%BB%FA%D5%BD" }, { value: "战争", key: "%D5%BD%D5%F9" },
            { value: "冒险", key: "%C3%B0%CF%D5" }, { value: "龙傲天", key: "%C1%FA%B0%C1%CC%EC" }
        ]
    },
    {
        title: { value: "黑深残属性Tags",  key: "tags.php?t=${key}&page=${page}" },
        page: 1,
        categories: [
            { value: "悬疑", key: "%D0%FC%D2%C9" }, { value: "犯罪", key: "%B7%B8%D7%EF" },
            { value: "复仇", key: "%B8%B4%B3%F0" }, { value: "黑暗", key: "%BA%DA%B0%B5" },
            { value: "猎奇", key: "%C1%D4%C6%E6" }, { value: "惊悚", key: "%BE%AA%E3%A4" },
            { value: "间谍", key: "%BC%E4%B5%FD" }, { value: "末日", key: "%C4%A9%C8%D5" },
            { value: "游戏", key: "%D3%CE%CF%B7" }, { value: "大逃杀", key: "%B4%F3%CC%D3%C9%B1" }
        ]
    },
    {
        title: { value: "人物属性类Tags",  key: "tags.php?t=${key}&page=${page}" },
        page: 1,
        categories: [
            { value: "青梅竹马", key: "%C7%E0%C3%B7%D6%F1%C2%ED" }, { value: "妹妹", key: "%C3%C3%C3%C3" },
            { value: "女儿", key: "%C5%AE%B6%F9" }, { value: "JK", key: "JK" },
            { value: "JC", key: "JC" }, { value: "大小姐", key: "%B4%F3%D0%A1%BD%E3" },
            { value: "性转", key: "%D0%D4%D7%AA" }, { value: "伪娘", key: "%CE%B1%C4%EF" },
            { value: "人外", key: "%C8%CB%CD%E2" }
        ]
    },
    {
        title: { value: "特殊属性类Tags",  key: "tags.php?t=${key}&page=${page}" },
        page: 1,
        categories: [
            { value: "后宫", key: "%BA%F3%B9%AC" }, { value: "百合", key: "%B0%D9%BA%CF" },
            { value: "耽美", key: "%B5%A2%C3%C0" }, { value: "NTR", key: "NTR" },
            { value: "女性视角", key: "%C5%AE%D0%D4%CA%D3%BD%C7" }
        ]
    },
    {
        title: { value: "我的书架",  key: "bookcase.php?classid=${key}" },
        page: 1,
        categories: [
            { value: "默认书架", key: "0" }, { value: "第1组书架", key: "1" },
            { value: "第2组书架", key: "2" }, { value: "第3组书架", key: "3" },
            { value: "第4组书架", key: "4" }, { value: "第5组书架", key: "5" }
        ]
    }
]

var bookSource = JSON.stringify({
  name: '轻小说文库',
  url: 'www.wenku8.net',
  version: 100,
  authorization: "https://www.wenku8.net/login.php",
  cookie: ["www.wenku8.net"],
  ranks: ranks
})

function getBooks(html) {
  let table = html.selectFirst('div#content > table.grid > tbody')
  if (table === null) return {
    end: true,
    books: [ getDetail(html) ]
  }
  let list = table.select('tr > td > div').map(book => {
    let author_category = book.selectFirst('div:not(:only-child) > p:first-of-type').text().split('/').map(item => item.replace(/^.*?:/, ''))
    let update_words_status = book.selectFirst('div:not(:only-child) > p:nth-child(2)').text().split('/').map(item => item.replace(/^.*?:/, ''))
    return {
      name: book.selectFirst('b > a').text(),
      author: author_category[0],
      cover: book.selectFirst('div > a > img').attr('src'),
      summary: book.selectFirst('div:not(:only-child) > p:nth-child(5)').text().replace('简介:', ''),
      words: update_words_status[1],
      update: update_words_status[0],
      status: update_words_status[2],
      category: author_category[1],
      tags: [book.selectFirst('div:not(:only-child) > p > span:not(.hottext)').text().split(' '), (update_words_status[3] ? (' ' + update_words_status[3]) : '')].flat(),
      detail: BASE_URL + book.selectFirst('div > b > a').attr('href')
    }
  })
  return {
    end: list.length === 0,
    books: list
  }
}

function getDetail(html) {
    let info = html.select('div#content > div > table > tbody > tr[align=center] + tr > td').map(item => item.text().split('：'))
    let url = BASE_URL + html.selectFirst('fieldset > div > a').attr('href')
    return {
        name: html.selectFirst('tbody > tr > td[align=center] > span > b').text(),
        author: info.find(item => item[0] === '小说作者')[1],
        cover: html.selectFirst('td > img').attr('src'),
        summary: html.selectFirst('tr > td:not([align=center])[valign=top] > span:last-of-type').text(),
        lastChapter: info.length === 5 ? html.selectFirst('tr > td:not([align=center])[valign=top] > span > a').text() : '',
        words: info.length === 5 ? info.find(item => item[0] === '全文长度')[1] : '',
        update: info.length === 5 ? info.find(item => item[0] === '最后更新')[1] : '',
        status: info.find(item => item[0] === '文章状态')[1],
        category: info.find(item => item[0] === '文库分类')[1],
        tags: html.selectFirst('tr > td:not([align=center])[valign=top] > span > b').text().replace('作品Tags：', '').split(' '),
        detail: url.replace(/novel\/./, 'book').replace('/index', ''),
        catalog: url
    }
}
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

/**
 * 搜索
 * @params {string} key
 * @returns {[{name, author, cover, detail}]}
 */
const search = (key, page) => {
    let response = GET(`https://es.bookan.com.cn/api/searchByType?debug=1&keyword=${key}&instanceId=23048&pageNum=${page}&limit=18&resourceType=3&searchType=1`)
    let $ = JSON.parse(response)
    return JSON.stringify({
        end: $.data.length === 0,
        books: $.data.map(book => ({
            name: book.name,
            author: book.author,
            cover: getCover(book.issueInfo),
            summary: book.issueInfo.text,
            category: book.issueInfo.categoryName,
            tags: book.issueInfo.tags.map(tag => tag.name),
            update: book.issueInfo.publish,
            detail: `https://api.bookan.com.cn/resource/issueInfoList?instanceId=37627&resourceType=3&issueIds=${book.issueId}&isDetail=1&isQRCode=`
        }))
    })
}

/**
 * 详情
 * @params {string} url
 * @returns {[{summary, status, category, words, update, lastChapter, catalog}]}
 */
const detail = (url) => {
    let response = GET(url)
    let $ = JSON.parse(response)
    return JSON.stringify({
        name: $.data[0].resourceName,
        author: $.data[0].owner,
        updeate: $.data[0].publish,
        summary: $.data[0].text,
        category: $.data[0].categoryName,
        tags: $.data[0].tags.map(tag => tag.name),
        catalog: JSON.stringify(Object.assign($.data[0], {
            start: Number($.data[0].start),
            text: '',
            explainRecommend: ''
        }))
    })
}

/**
 * 目录
 * @params {string} url
 * @returns {[{name, url, vip}]}
 */
const catalog = (url) => {
    let book = JSON.parse(url)
    let resourceId = book.resourceId
    let issueId = book.issueId
    if (book.html == 2) {
        let response = GET(`https://api.bookan.com.cn/resource/getHash?resourceType=3&resourceId=${resourceId}&issueId=${issueId}&start=0&end=0`)
        let hash = JSON.parse(response).data[0].hash
        // let container = GET(`https://epub.bookan.com.cn/epub2/${resourceId}/${resourceId}-${issueId}/${issueId}_${hash}/META-INF/container.xml`)
        // let toc = GET(`https://epub.bookan.com.cn/epub2/${resourceId}/${resourceId}-${issueId}/${issueId}_${hash}/OEBPS/toc.ncx`)
        let link = `https://epub.bookan.com.cn/epub2/${resourceId}/${resourceId}-${issueId}/${issueId}_${hash}/directories.json`
        let $ = JSON.parse(GET(link))
        let spine = $.spine.map(item => item.src.replace(/#.*$/, ''))
        let list = getToc($.catalog, (item) => ({
            index: spine.indexOf(item.src.replace(/#.*$/, '')),
            id: item.src.includes('#') ? item.src.split('#')[1] : ''
        }))
        return JSON.stringify(list.map((chapter, index) => {
            let urls = []
            let lastIndex = index === list.length - 1 ? spine.length - 1 : list[index + 1].link.index
            for (let i = chapter.link.index; i <= lastIndex; i++) {
                urls.push(link.replace('directories.json', spine[i]))
            }
            if (index < list.length - 1 && list[index + 1].link.id === '') {
                urls.pop()
            }
            return {
                name: chapter.name,
                url: JSON.stringify({
                    urls: urls,
                    start: chapter.link.id,
                    end: index === list.length - 1 ? '' : list[index + 1].link.id
                }),
                level: chapter.level
            }
        }))
    } else {
        let link = `https://api.bookan.com.cn/resource/catalogInfo?resourceType=3&categoryId=${issueId}&statusType=1`
        let $ = JSON.parse(GET(link))
        let response = GET(`https://api.bookan.com.cn/resource/getHash?resourceType=3&resourceId=${resourceId}&issueId=${issueId}&start=1&end=${book.count}`)
        let spine = JSON.parse(response).data
        let list = getToc($.data, (item) => ({
            index: spine.findIndex(({ page, hash })=> page === book.start + item.page),
            page: item.page
        }))
        return JSON.stringify(list.map((chapter, index) => {
            let urls = []
            let lastIndex = index === list.length - 1 ? spine.length - 1 : list[index + 1].link.index
            for (let i = chapter.link.index; i < lastIndex; i++) {
                urls.push(`https://img1-qn.bookan.com.cn/jpage8/${resourceId}/${resourceId}-${issueId}/${spine[i].hash}_big.jpg`)
            }
            return {
                name: chapter.name,
                url: JSON.stringify(urls),
                level: chapter.level
            }
        }))
    }
}

/**
 * 章节
 * @params {string} url
 * @returns {string}
 */
const chapter = (url) => {
    let params = JSON.parse(url)
    switch (true) {
        case params.urls && params.urls.length === 1:
            return getContent(params.urls[0], params.start, params.end)
        case params.urls && params.urls.length > 1:
            return params.urls.map((url, index) => {
                switch (index) {
                    case 0: return getContent(url, params.start, '')
                    case params.urls.length - 1: return getContent(url, '', params.end)
                    default: return getContent(url, '', '')
                }
            }).join('')
        default:
            return params.map(url => `![](${url})`).join('\n')
    }
}

/**
 * 分类/排行/书城
 * @params {string} title
 * @params {string} category
 * @params {int} page
 * @returns {{end, books:[{name, author, cover, detail}]}}
 */
const rank = (title, category, page) => {
    let query = title.replace('${key}', category).replace('${page}', page)
    let url = 'https://apidata.bookan.com.cn/resource_categoryIssues__instanceId_23048_libraryType__' + query + '_limitNum_20_onlyEpub__resourceType_3_sort__lang_'
    let response = GET(url)
    let $ = JSON.parse(response)
    return JSON.stringify({
        end: $.data.length === 0,
        books: $.data.map(book => ({
            name: book.resourceName,
            author: book.owner,
            cover: getCover(book),
            summary: book.text,
            category: book.categoryName,
            tags: book.tags.map(tag => tag.name),
            update: book.publish,
            detail: `https://api.bookan.com.cn/resource/issueInfoList?instanceId=37627&resourceType=3&issueIds=${book.issueId}&isDetail=1&isQRCode=`
        }))
    })
}

const ranks = [
    {
        title: { value: "上榜好书", key: "categoryId_596_pageNum_${page}"  },
        page: 1,
    },
    {
        title: { value: "党政学习", key: "categoryId_${key}_pageNum_${page}"  },
        page: 1,
        categories: [
            { value: "党政学习", key: "2" }, { value: "军政形势", key: "3" },
            { value: "党政", key: "4" }, { value: "党政军史", key: "5" },
            { value: "军事", key: "6" }
        ]
    },
    {
        title: { value: "新书推荐", key: "categoryId_8913_pageNum_${page}"  },
        page: 1,
    },
    {
        title: { value: "经典名著", key: "categoryId_47_pageNum_${page}"  },
        page: 1,
    },
    {
        title: { value: "经管职场", key: "categoryId_${key}_pageNum_${page}"  },
        page: 1,
        categories: [
            { value: "经管职场", key: "22" }, { value: "理财", key: "23" },
            { value: "管理", key: "7" }, { value: "励志/成功", key: "8" },
            { value: "创业策划", key: "9" }, { value: "人际沟通", key: "10" },
            { value: "经济", key: "11" }, { value: "职场技巧", key: "12" },
            { value: "销售", key: "13" }, { value: "商业史传", key: "1" }
        ]
    },
    {
        title: { value: "人文社科", key: "categoryId_${key}_pageNum_{page}"  },
        page: 1,
        categories: [
            { value: "人文社科", key: "39" }, { value: "世界文化", key: "40" },
            { value: "哲学", key: "14" }, { value: "历史", key: "15" },
            { value: "社会学", key: "16" }, { value: "建筑", key: "17" },
            { value: "文化", key: "18" }, { value: "宗教", key: "19" },
            { value: "心理", key: "20" }, { value: "法律", key: "21" }
        ]
    },
    {
        title: { value: "文学艺术", key: "categoryId_${key}_pageNum_{page}"  },
        page: 1,
        categories: [
            { value: "文学艺术", key: "61" }, { value: "摄影绘画", key: "62" },
            { value: "收藏鉴赏", key: "41" }, { value: "小说", key: "42" },
            { value: "文学", key: "43" }, { value: "传记", key: "44" },
            { value: "散文诗歌", key: "45" }, { value: "青春", key: "46" },
            { value: "武侠玄幻", key: "48" }, { value: "科幻魔幻", key: "49" },
            { value: "侦探推理", key: "50" }, { value: "漫画幽默", key: "51" },
            { value: "戏剧电影", key: "52" }, { value: "艺术", key: "24" },
            { value: "书法篆刻", key: "25" }, { value: "音乐乐器", key: "26" }
        ]
    },
    {
        title: { value: "少儿幼教", key: "categoryId_${key}_pageNum_{page}"  },
        page: 1,
        categories: [
            { value: "少儿幼教", key: "73" }, { value: "儿童文学", key: "74" },
            { value: "少儿科普", key: "75" }, { value: "绘本", key: "76" },
            { value: "幼儿启蒙", key: "77" }, { value: "少儿卡通", key: "63" }
        ]
    },
    {
        title: { value: "情感家庭", key: "categoryId_${key}_pageNum_{page}"  },
        page: 1,
        categories: [
            { value: "情感家庭", key: "71" }, { value: "美食烹饪", key: "72" },
            { value: "手工DIY", key: "53" }, { value: "孕产育儿", key: "54" },
            { value: "健康养生", key: "55" }, { value: "家居", key: "56" },
            { value: "亲子", key: "57" }, { value: "医学", key: "58" },
            { value: "婚恋情感", key: "59" }, { value: "两性", key: "60" }
        ]
    },
    {
        title: { value: "时尚娱乐", key: "categoryId_${key}_pageNum_{page}"  },
        page: 1,
        categories: [
            { value: "时尚娱乐", key: "64" }, { value: "旅游", key: "65" },
            { value: "健身", key: "66" }, { value: "休闲", key: "67" },
            { value: "美食", key: "68" }, { value: "时尚", key: "69" },
            { value: "体育", key: "70" }
        ]
    },
    {
        title: { value: "教育科技", key: "categoryId_${key}_pageNum_{page}"  },
        page: 1,
        categories: [
            { value: "教育科技", key: "77" }, { value: "教辅", key: "28" },
            { value: "职业技能", key: "29" }, { value: "生物医学丨教育科技", key: "30" },
            { value: "自然科学", key: "31" }, { value: "农业", key: "32" },
            { value: "教育理论", key: "33" }, { value: "科普", key: "34" },
            { value: "工业", key: "35" }, { value: "计算机/网络", key: "36" },
            { value: "外语", key: "37" }, { value: "工具书", key: "38" }
        ]
    }
]

var bookSource = JSON.stringify({
  name: '福建江夏学院',
  url: 'zq.bookan.com.cn',
  version: 100,
  authorization: "https://zq.bookan.com.cn/?t=login&id=23048"
})

function getToc(list, action) {
    return list.flatMap(item => {
        let level = item.sublevels === undefined || item.sublevels.length === 0
        return [
            {
                name: item.title ?? item.name,
                level: level,
                link: action(item),
            },
            level ? [] : getToc(item.sublevels, action)
        ].flat()
    })
}

function getContent(url, start, end) {
    let response = GET(url).match(/(?<=<body[^>]*>).*(?=<\/body>)/s)[0]
        .replace(/<script[^>]*>(<(?!\/script>)|[^<])*<\/script>/g, '')
        .replace(/<style[^>]*>(<(?!\/style>)|[^<])*<\/style>/g, '')
        .replace(/<!-- .* -->/g, '')
    let empty = /<(br|hr|img|input)[^>]*?>/g
    let regex = /<(?<tag>[^/\s]+)[^>]*>(?:<(?!\/|\k<tag>)|[^<])*<\/\k<tag>>/g
    if (start && response.includes(start)) {
        let temp = response.split(`id="${start}"`)[0].replace(empty, '')
        while (regex.test(temp)) {
            temp = temp.replace(regex, '')
        }
        response = temp + response.split(`id="${start}"`)[1]
    }
    if (end && response.includes(end)) {
        let temp = response.split(`id="${end}"`)[1].replace(empty, '')
        while (regex.test(temp)) {
            temp = temp.replace(regex, '')
        }
        response = response.split(`id="${end}"`)[0].replace(/<[^<]*$/s, '') + temp.replace(/^[^>]*>.*?<\/[^/\s]+>/s, '')
    }
    return response.replace(/<[^<>]+>/g, tag => tag.replace(/\n/g, ''))
        .replace(/<img[^>]*?>/g, img => img.replace('..', url.replace(/\/Text.*/i, '')))
}

function getCover(item) {
    let resourceId = item.resourceId;
    let issueId   = item.issueId;
    if ((item.webp && item.webp !== '0') || (item.jpg && item.jpg !== '0')) {
        let pageNum = item.webp || item.jpg;
        return `http://img1-qn.bookan.com.cn/page${pageNum}/${resourceId}/${resourceId}-${issueId}/cover_small.mg`;
    } else {
        return `https://epub.bookan.com.cn/epub2/${resourceId}/${resourceId}-${issueId}/cover_small.jpg`;
    }
}
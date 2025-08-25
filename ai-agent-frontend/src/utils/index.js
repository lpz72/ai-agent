// 工具函数集合
import { marked } from 'marked'

// 配置marked选项
marked.setOptions({
  breaks: true,        // 支持换行符转换为<br>
  gfm: true,          // 启用GitHub风格的Markdown
  sanitize: false,    // 不清理HTML（我们会手动处理安全性）
  smartLists: true,   // 智能列表
  smartypants: true   // 智能标点符号
})

/**
 * 生成唯一ID
 * @param {string} prefix - ID前缀
 * @returns {string} 唯一ID
 */
export const generateId = (prefix = '') => {
  const timestamp = Date.now()
  const random = Math.random().toString(36).substr(2, 9)
  return prefix ? `${prefix}_${timestamp}_${random}` : `${timestamp}_${random}`
}

/**
 * 格式化当前时间
 * @param {string} locale - 地区代码，默认中文
 * @returns {string} 格式化的时间字符串
 */
export const formatCurrentTime = (locale = 'zh-CN') => {
  return new Date().toLocaleTimeString(locale, { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

/**
 * 防抖函数
 * @param {Function} func - 要防抖的函数
 * @param {number} wait - 等待时间（毫秒）
 * @returns {Function} 防抖后的函数
 */
export const debounce = (func, wait) => {
  let timeout
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout)
      func(...args)
    }
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
  }
}

/**
 * 节流函数
 * @param {Function} func - 要节流的函数
 * @param {number} limit - 限制时间（毫秒）
 * @returns {Function} 节流后的函数
 */
export const throttle = (func, limit) => {
  let inThrottle
  return function executedFunction(...args) {
    if (!inThrottle) {
      func.apply(this, args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

/**
 * 深拷贝对象
 * @param {any} obj - 要拷贝的对象
 * @returns {any} 拷贝后的对象
 */
export const deepClone = (obj) => {
  if (obj === null || typeof obj !== 'object') return obj
  if (obj instanceof Date) return new Date(obj.getTime())
  if (obj instanceof Array) return obj.map(item => deepClone(item))
  if (typeof obj === 'object') {
    const clonedObj = {}
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        clonedObj[key] = deepClone(obj[key])
      }
    }
    return clonedObj
  }
}

/**
 * 安全的JSON解析
 * @param {string} jsonString - JSON字符串
 * @param {any} defaultValue - 解析失败时的默认值
 * @returns {any} 解析后的对象或默认值
 */
export const safeJsonParse = (jsonString, defaultValue = null) => {
  try {
    return JSON.parse(jsonString)
  } catch (error) {
    console.warn('JSON解析失败:', error)
    return defaultValue
  }
}

/**
 * 格式化文件大小
 * @param {number} bytes - 字节数
 * @returns {string} 格式化后的文件大小
 */
export const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 检查是否为移动设备
 * @returns {boolean} 是否为移动设备
 */
export const isMobile = () => {
  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
}

/**
 * 检查是否为触摸设备
 * @returns {boolean} 是否为触摸设备
 */
export const isTouchDevice = () => {
  return 'ontouchstart' in window || navigator.maxTouchPoints > 0
}

/**
 * 清理AI响应文本，去除多余的换行和格式化内容
 * @param {string} text - 原始文本
 * @returns {string} 清理后的文本
 */
export const cleanAIResponse = (text) => {
  if (!text || typeof text !== 'string') return text

  // 去除多余的换行符和空白
  let cleaned = text
    .replace(/\r\n/g, '\n') // 统一换行符
    .replace(/\r/g, '\n') // 统一换行符
    .replace(/^\s+/, '') // 去除开头的所有空白字符（包括空格、换行、制表符）
    .replace(/\s+$/, '') // 去除结尾的所有空白字符
    .replace(/\n{3,}/g, '\n\n') // 3个或以上连续换行符替换为2个
    .replace(/[ \t]+\n/g, '\n') // 去除行尾的空格和制表符
    .replace(/\n[ \t]+/g, '\n') // 去除行首的空格和制表符

  return cleaned
}

/**
 * 将文本中的URL转换为可点击的链接，并处理换行符
 * @param {string} text - 包含URL的文本
 * @returns {string} 转换后的HTML文本
 */
export const linkifyText = (text) => {
  if (!text || typeof text !== 'string') return text

  // 处理各种形式的换行符
  let processedText = text
    .replace(/\r\n/g, '\n')      // Windows换行符
    .replace(/\r/g, '\n')        // Mac换行符
    .replace(/\\n/g, '\n')       // 转义的换行符
    .replace(/\n/g, '<br>')      // 转换为HTML换行

  console.log('linkifyText处理前:', text.substring(0, 100))
  console.log('linkifyText处理后:', processedText.substring(0, 100))

  // URL正则表达式，匹配http、https、ftp协议的链接
  const urlRegex = /(https?:\/\/[^\s<>"']+|ftp:\/\/[^\s<>"']+)/gi

  return processedText.replace(urlRegex, (url) => {
    // 清理URL末尾可能的标点符号
    const cleanUrl = url.replace(/[.,;:!?'")\]}]*$/, '')
    return `<a href="${cleanUrl}" target="_blank" rel="noopener noreferrer" class="auto-link">${cleanUrl}</a>`
  })
}

/**
 * 清理JSON数据，移除无用字段并格式化
 * @param {any} data - JSON数据
 * @returns {any} 清理后的数据
 */
export const cleanJSONData = (data) => {
  if (!data) return data

  // 如果是字符串，尝试解析为JSON
  if (typeof data === 'string') {
    try {
      data = JSON.parse(data)
    } catch (e) {
      return data
    }
  }

  // 如果是数组，递归清理每个元素
  if (Array.isArray(data)) {
    return data.map(item => cleanJSONData(item)).filter(item => item !== null && item !== undefined)
  }

  // 如果是对象，清理无用字段
  if (typeof data === 'object' && data !== null) {
    const usefulFields = [
      'title', 'name', 'description', 'content', 'text', 'info', 'summary',
      'link', 'url', 'author', 'date', 'time', 'snippet', 'displayed_link',
      'source', 'category', 'tags', 'keywords', 'price', 'rating', 'image'
    ]

    // 需要跳过的无用字段
    const skipFields = [
      'position', 'rank', 'index', 'id', 'uuid', 'guid', 'hash',
      'thumbnail', 'favicon', 'breadcrumb', 'navigation', 'menu',
      'sidebar', 'footer', 'header', 'ads', 'advertisement', 'tracking',
      'analytics', 'metadata', 'seo', 'schema', 'json_ld', 'microdata',
      'og_', 'twitter_', 'fb_', 'social_', 'share_', 'like_', 'comment_',
      'related_', 'similar_', 'recommended_', 'popular_', 'trending_',
      'cached', 'timestamp', 'crawled', 'indexed', 'scraped',
      'debug', 'trace', 'log', 'error', 'warning', 'info_log'
    ]

    const cleaned = {}

    for (const [key, value] of Object.entries(data)) {
      const lowerKey = key.toLowerCase()

      // 跳过明确的无用字段
      if (key.startsWith('_') ||
          skipFields.some(skip => lowerKey.includes(skip)) ||
          lowerKey.includes('internal') ||
          lowerKey.includes('debug') ||
          lowerKey.includes('temp') ||
          lowerKey.includes('cache')) {
        continue
      }

      // 只保留有用字段且有实际内容的值
      if (usefulFields.includes(lowerKey)) {
        if (typeof value === 'string') {
          const trimmed = value.trim()
          if (trimmed && trimmed.length > 0) {
            cleaned[key] = trimmed
          }
        } else if (value !== null && value !== undefined) {
          const cleanedValue = cleanJSONData(value)
          if (cleanedValue !== null && cleanedValue !== undefined) {
            cleaned[key] = cleanedValue
          }
        }
      }
    }

    return Object.keys(cleaned).length > 0 ? cleaned : null
  }

  return data
}

/**
 * 格式化搜索结果为易读的HTML
 * @param {Array} results - 搜索结果数组
 * @returns {string} 格式化后的HTML
 */
export const formatSearchResults = (results) => {
  if (!results || !Array.isArray(results) || results.length === 0) {
    return '<div class="empty-result">暂无搜索结果</div>'
  }

  const cleanedResults = results
    .map(item => cleanJSONData(item))
    .filter(item => item && (item.title || item.name || item.link))

  if (cleanedResults.length === 0) {
    return '<div class="empty-result">暂无有效搜索结果</div>'
  }

  return cleanedResults.map((item, index) => {
    const title = item.title || item.name || '未知标题'
    const link = item.link || item.url || '#'
    const description = item.description || item.snippet || item.content || item.summary || ''
    const source = item.source || item.displayed_link || ''

    // 清理和截断文本
    const cleanTitle = title.length > 100 ? title.substring(0, 100) + '...' : title
    const cleanDesc = description.length > 200 ? description.substring(0, 200) + '...' : description
    const cleanSource = source.length > 50 ? source.substring(0, 50) + '...' : source

    return `
      <div class="search-result-item">
        <div class="result-index">${index + 1}.</div>
        <div class="result-content">
          <div class="result-title">
            <a href="${link}" target="_blank" class="result-link" rel="noopener noreferrer">${cleanTitle}</a>
          </div>
          ${cleanDesc ? `<div class="result-snippet">${linkifyText(cleanDesc)}</div>` : ''}
          ${cleanSource ? `<div class="result-source">来源: ${cleanSource}</div>` : ''}
        </div>
      </div>
    `
  }).join('')
}

/**
 * 处理Markdown内容
 * @param {string} text - 包含Markdown的文本
 * @returns {string} 转换后的HTML
 */
export const processMarkdown = (text) => {
  if (!text || typeof text !== 'string') return text

  try {
    // 注意：这里不再调用cleanAIResponse，因为调用方已经清理过了
    let processedText = text

    // 预处理：确保换行符格式统一
    processedText = processedText
      .replace(/\r\n/g, '\n')
      .replace(/\r/g, '\n')
      .replace(/\\n/g, '\n')  // 处理转义的换行符

    // 解析Markdown
    let htmlContent = marked.parse(processedText)

    // 处理链接（为marked生成的链接添加安全属性）
    htmlContent = htmlContent.replace(
      /<a href="([^"]*)"([^>]*)>/g,
      '<a href="$1" target="_blank" rel="noopener noreferrer" class="auto-link"$2>'
    )

    // 处理代码块样式
    htmlContent = htmlContent.replace(
      /<pre><code([^>]*)>/g,
      '<pre class="code-block"><code$1>'
    )

    // 处理内联代码样式
    htmlContent = htmlContent.replace(
      /<code([^>]*)>/g,
      '<code class="inline-code"$1>'
    )

    // 处理可能遗漏的普通链接（不在Markdown链接语法中的）
    htmlContent = htmlContent.replace(
      /(?<!href=")(?<!href=')(https?:\/\/[^\s<>"']+)/gi,
      '<a href="$1" target="_blank" rel="noopener noreferrer" class="auto-link">$1</a>'
    )

    return htmlContent

  } catch (error) {
    console.error('Markdown解析失败:', error)
    // 解析失败时，回退到普通文本处理
    return linkifyText(text)
  }
}

/**
 * 智能内容处理：自动检测并处理Markdown或普通文本
 * @param {string} content - 内容文本
 * @returns {string} 处理后的HTML
 */
export const processContent = (content) => {
  if (!content || typeof content !== 'string') return content

  // 首先清理内容
  const cleanedContent = cleanAIResponse(content)

  // 检查是否包含明显的Markdown语法
  const strongMarkdownIndicators = [
    '```',           // 代码块
    '## ',           // 标题
    '### ',          // 标题
    '#### ',         // 标题
    '##### ',        // 标题
    '###### ',       // 标题
    '**',            // 粗体
    '__',            // 粗体
    /^\s*- /m,       // 列表（考虑缩进）
    /^\s*\* /m,      // 列表（考虑缩进）
    /^\s*\d+\. /m,   // 有序列表（考虑缩进）
    /\[.*\]\(.*\)/,  // 链接
    /!\[.*\]\(.*\)/  // 图片
  ]

  const hasStrongMarkdown = strongMarkdownIndicators.some(indicator => {
    if (indicator instanceof RegExp) {
      return indicator.test(cleanedContent)
    }
    return cleanedContent.includes(indicator)
  })

  if (hasStrongMarkdown) {
    console.log('检测到Markdown语法，使用Markdown处理')
    return processMarkdown(cleanedContent)
  } else {
    console.log('未检测到Markdown语法，使用普通文本处理')
    // 普通文本处理：确保换行符被正确处理
    return linkifyText(cleanedContent)
  }
}

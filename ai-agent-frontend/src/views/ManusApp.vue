<template>
  <div class="manus-app">
    <div class="header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h1>AI超级智能体 🤖</h1>
      <div class="chat-id">会话ID: {{ chatId }}</div>
    </div>

    <div class="chat-container">
      <div class="chat-messages" ref="chatMessages">
        <div
          v-for="(message, index) in messages"
          :key="index"
          :class="['message', message.type]"
          :data-step="message.isStep"
        >
          <div class="message-content">
            <div class="message-text" v-html="message.content"></div>
            <div class="message-time">{{ message.time }}</div>
          </div>
        </div>
      </div>

      <div class="input-container">
        <div class="input-wrapper">
          <input
            v-model="inputMessage"
            @keyup.enter="sendMessage"
            placeholder="输入您的问题..."
            class="message-input"
            :disabled="isLoading"
          />
          <button
            @click="sendMessage"
            class="send-btn"
            :disabled="isLoading || !inputMessage.trim()"
          >
            {{ isLoading ? '发送中...' : '发送' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { aiService } from '../api/aiService.js'
import { generateId, formatCurrentTime, cleanAIResponse, linkifyText, cleanJSONData, formatSearchResults, processContent, processMarkdown } from '../utils/index.js'
import api from "../api/config";

export default {
  name: 'ManusApp',
  data() {
    return {
      chatId: '',
      messages: [],
      inputMessage: '',
      isLoading: false
    }
  },

  mounted() {
    this.generateChatId()
    this.addWelcomeMessage()
  },

  methods: {
    generateChatId() {
      this.chatId = generateId('manus')
    },

    addWelcomeMessage() {
      this.messages.push({
        type: 'ai',
        content: '你好！我是AI超级智能体 🤖<br>我可以帮助你解决各种问题，包括但不限于：<br>• 技术问题<br>• 学习辅导<br>• 创意写作<br>• 数据分析<br>• 生活建议<br>请告诉我你需要什么帮助！',
        time: formatCurrentTime()
      })
    },

    async sendMessage() {
      if (!this.inputMessage.trim() || this.isLoading) return

      const userMessage = this.inputMessage.trim()
      this.messages.push({
        type: 'user',
        content: userMessage,
        time: formatCurrentTime()
      })

      this.inputMessage = ''
      this.isLoading = true

      // 添加AI正在输入的提示
      const loadingMessageIndex = this.messages.length
      this.messages.push({
        type: 'ai',
        content: '正在思考中...',
        time: formatCurrentTime()
      })

      try {
        // 使用新的API服务，实时更新内容
        const result = await this.callManusSSE(userMessage, loadingMessageIndex)

        // 直接处理后端返回的完整数据
        if (result && result.content) {
          console.log('=== 处理最终结果 ===')
          console.log('最终结果内容:', result.content)

          // 智能处理内容：自动检测Markdown并处理
          const processedContent = processContent(result.content)
          this.messages[loadingMessageIndex].content = processedContent
        } else {
          // this.messages.content = '抱歉，发送消息失败，请重试。'
          this.messages[loadingMessageIndex].content = '抱歉，发送消息失败，请重试。'
          console.log('没有最终结果内容')
        }
      } catch (error) {
        console.error('发送消息失败:', error)
        this.messages[loadingMessageIndex].content = this.getErrorMessage(error)
      } finally {
        this.isLoading = false
        this.scrollToBottom()
      }
    },
    





    // 格式化工具执行结果（SSE版本）
    formatToolResultForSSE(stepNumber, content) {
      // 提取工具名称
      const toolMatch = content.match(/🔧\s*(\w+)/)
      const toolName = toolMatch ? toolMatch[1] : '未知工具'

      // 提取结果内容
      const resultMatch = content.match(/结果:\s*([\s\S]*)$/)
      const resultContent = resultMatch ? resultMatch[1].trim() : content

      return `
        <div class="step-header">Step ${stepNumber}</div>
        <div class="tool-info">
          <span class="tool-name">🔧 ${toolName}</span>
          <span class="tool-status">✅ 任务完成</span>
        </div>
        <div class="tool-result">
          <div class="result-label">结果:</div>
          <div class="result-content">${linkifyText(resultContent)}</div>
        </div>
      `
    },

    // 格式化搜索结果（SSE版本）
    formatSearchResultForSSE(stepNumber, content) {
      // 提取搜索结果数量
      const countMatch = content.match(/找到\s*(\d+)\s*个搜索结果/)
      const count = countMatch ? countMatch[1] : '若干'

      // 提取结果内容
      const resultsMatch = content.match(/搜索结果:\s*([\s\S]*)$/)
      const resultsContent = resultsMatch ? resultsMatch[1] : content

      return `
        <div class="step-header">Step ${stepNumber}</div>
        <div class="web-result">
          <div class="result-label">🔍 搜索结果</div>
          <div class="search-results-count">找到 ${count} 个相关结果</div>
          <div class="result-content">${this.formatSearchResultContent(resultsContent)}</div>
        </div>
      `
    },

    // 格式化搜索结果内容
    formatSearchResultContent(content) {
      // 按行分割并处理
      const lines = content.split('\n').filter(line => line.trim())
      let formattedResults = ''
      let currentResult = null

      for (const line of lines) {
        const trimmedLine = line.trim()

        // 检查是否是新的结果项（以数字开头）
        const resultMatch = trimmedLine.match(/^(\d+)\.\s*(.+)$/)
        if (resultMatch) {
          // 保存上一个结果
          if (currentResult) {
            formattedResults += this.buildSearchResultItem(currentResult)
          }

          // 开始新的结果
          currentResult = {
            index: resultMatch[1],
            title: resultMatch[2],
            link: '',
            description: '',
            source: ''
          }
        } else if (currentResult) {
          // 解析结果的其他字段
          if (trimmedLine.startsWith('链接:')) {
            currentResult.link = trimmedLine.replace('链接:', '').trim()
          } else if (trimmedLine.startsWith('描述:')) {
            currentResult.description = trimmedLine.replace('描述:', '').trim()
          } else if (trimmedLine.startsWith('来源:')) {
            currentResult.source = trimmedLine.replace('来源:', '').trim()
          }
        }
      }

      // 保存最后一个结果
      if (currentResult) {
        formattedResults += this.buildSearchResultItem(currentResult)
      }

      return formattedResults || linkifyText(content)
    },

    // 构建搜索结果项
    buildSearchResultItem(result) {
      return `
        <div class="search-result-item">
          <div class="result-index">${result.index}</div>
          <div class="result-content">
            <div class="result-title">
              ${result.link ?
                `<a href="${result.link}" target="_blank" class="result-link" rel="noopener noreferrer">${result.title}</a>` :
                result.title
              }
            </div>
            ${result.description ? `<div class="result-snippet">${linkifyText(result.description)}</div>` : ''}
            ${result.source ? `<div class="result-source">来源: ${result.source}</div>` : ''}
          </div>
        </div>
      `
    },

    // 直接调用SSE，实时更新内容
    async callManusSSE(message, messageIndex) {
      try {
        const eventSource = new EventSource(
          api.defaults.baseURL + `/ai/manus/chat?message=${encodeURIComponent(message)}`
        )

        let fullContent = ''

        return new Promise((resolve, reject) => {
          const cleanup = () => {
            eventSource.close()
            resolve({
              success: true,
              content: fullContent,
              isComplete: true
            })
          }

          // 设置超时
          const timeout = setTimeout(cleanup, 30000)

          eventSource.onmessage = (event) => {
            if (event.data && event.data.trim() !== '') {
              fullContent += event.data
              console.log('SSE收到数据:', event.data)

              // 智能处理数据：自动检测Markdown并处理
              const processedContent = processContent(fullContent)

              // 实时更新消息内容
              this.messages[messageIndex].content = processedContent
              this.scrollToBottom()
            }
          }

          eventSource.onerror = (error) => {
            console.error('SSE连接错误:', error)
            cleanup()
            clearTimeout(timeout)
            reject(new Error('SSE连接失败'))
          }

          // 监听连接关闭
          eventSource.addEventListener('close', () => {
            console.log('SSE连接关闭，最终内容:', fullContent)
            cleanup()
            clearTimeout(timeout)
          })

          // 监听连接打开
          eventSource.onopen = () => {
            console.log('SSE连接已建立')
          }
        })
      } catch (error) {
        console.error('SSE调用失败:', error)
        throw new Error('连接失败，请检查网络连接或稍后重试')
      }
    },

    // 处理多个Steps的内容
    processMultipleSteps(content, messageIndex) {
      console.log('开始处理多个step内容:', content)

      // 移除原始的"正在思考中..."消息
      this.messages.splice(messageIndex, 1)

      // 按Step分割内容
      const stepParts = content.split(/(?=Step\s+\d+)/).filter(part => part.trim())

      stepParts.forEach(stepPart => {
        const stepMatch = stepPart.match(/^Step\s+(\d+)\s*\n\n([\s\S]*)$/)
        if (stepMatch) {
          const stepNumber = stepMatch[1]
          const stepContent = stepMatch[2]

          this.messages.push({
            type: 'ai',
            content: this.formatStepContentForSSE(stepNumber, stepContent),
            time: formatCurrentTime(),
            isStep: true,
            stepNumber: stepNumber
          })
        }
      })

      this.scrollToBottom()
    },

    processStepContent(content, messageIndex) {
      console.log('开始处理step内容:', content.substring(0, 500) + '...')

      // 移除原始的"正在思考中..."消息
      this.messages.splice(messageIndex, 1)

      // 首先清理整个内容
      const cleanedFullContent = cleanAIResponse(content)
      console.log('清理后的完整内容长度:', cleanedFullContent.length)

      // 使用更精确的正则表达式匹配step内容
      // 匹配格式：Step 数字: 内容 (直到下一个Step或结尾)
      const stepRegex = /Step\s*(\d+):\s*([^]*?)(?=Step\s*\d+:|$)/gi
      const steps = []
      let match

      // 重置正则表达式的lastIndex
      stepRegex.lastIndex = 0

      while ((match = stepRegex.exec(cleanedFullContent)) !== null) {
        const stepNumber = match[1]
        let stepContent = match[2]

        // 清理每个step的内容
        stepContent = cleanAIResponse(stepContent)
        console.log(`找到Step ${stepNumber}, 内容长度: ${stepContent.length}`)
        console.log(`Step ${stepNumber} 内容预览:`, stepContent.substring(0, 200) + '...')

        if (stepContent && stepContent.length > 0) {
          steps.push({
            number: stepNumber,
            content: stepContent,
            formattedContent: this.formatStepContent(stepNumber, stepContent)
          })
        }
      }

      console.log('总共找到', steps.length, '个step')

      if (steps.length > 0) {
        // 为每个step创建一个新的消息
        steps.forEach((step, index) => {
          console.log(`创建Step ${step.number} 消息:`, step.formattedContent.substring(0, 100) + '...')
          this.messages.push({
            type: 'ai',
            content: step.formattedContent,
            time: formatCurrentTime(),
            isStep: true,
            stepNumber: step.number
          })
        })
      } else {
        // 如果没有匹配到step，显示原始内容
        console.log('没有找到step，显示原始内容')
        this.messages.push({
          type: 'ai',
          content: content,
          time: formatCurrentTime()
        })
      }
    },

    formatStepContent(stepNumber, content) {
      console.log(`格式化Step ${stepNumber} 内容长度: ${content.length}`)
      console.log(`Step ${stepNumber} 内容预览:`, content.substring(0, 200) + '...')

      // 首先清理内容中的多余换行
      const cleanedContent = cleanAIResponse(content)

      // 检查是否是工具执行结果
      if (cleanedContent.includes('工具') && cleanedContent.includes('完成了它的任务')) {
        console.log('检测到工具执行结果')
        return this.formatToolResult(stepNumber, cleanedContent)
      }

      // 检查是否包含搜索或抓取结果
      if (cleanedContent.includes('searchWeb') || cleanedContent.includes('scrapeWeb') || cleanedContent.includes('结果:')) {
        console.log('检测到搜索结果')
        return this.formatWebResult(stepNumber, cleanedContent)
      }

      // 检查是否包含JSON数据
      if (cleanedContent.includes('{') && cleanedContent.includes('}')) {
        console.log('检测到JSON数据')
        return this.formatJsonStep(stepNumber, cleanedContent)
      }

      // 检查是否是空内容或只有空白
      const trimmedContent = cleanedContent.trim()
      if (!trimmedContent || trimmedContent.length < 10) {
        console.log('内容为空或过短，显示默认消息')
        return `
          <div class="step-header">Step ${stepNumber}</div>
          <div class="step-content">思考完成 - 无需行动</div>
        `
      }

      // 普通step内容 - 应用链接转换
      console.log('使用普通step格式，内容长度:', trimmedContent.length)
      const linkedContent = linkifyText(trimmedContent)
      return `
        <div class="step-header">Step ${stepNumber}</div>
        <div class="step-content">${linkedContent}</div>
      `
    },

    formatToolResult(stepNumber, content) {
      console.log('格式化工具结果:', content.substring(0, 200) + '...')

      // 解析工具执行结果
      const toolMatch = content.match(/工具\s+(\w+)\s+完成了它的任务！结果:\s*(.+)/)

      if (toolMatch) {
        const toolName = toolMatch[1]
        const result = toolMatch[2]

        console.log(`工具名称: ${toolName}, 结果: ${result.substring(0, 100)}...`)

        let formattedResult = result

        // 尝试解析JSON结果
        try {
          let jsonData = null

          if (result.startsWith('"') && result.endsWith('"')) {
            const jsonStr = result.slice(1, -1).replace(/\\"/g, '"').replace(/\\\\/g, '\\')
            jsonData = JSON.parse(jsonStr)
          } else if (result.startsWith('{') || result.startsWith('[')) {
            jsonData = JSON.parse(result)
          }

          if (jsonData) {
            // 清理JSON数据
            const cleanedData = cleanJSONData(jsonData)
            console.log('清理后的JSON数据:', cleanedData)

            if (Array.isArray(cleanedData)) {
              formattedResult = this.formatJsonArray(cleanedData)
              console.log('解析为JSON数组，长度:', cleanedData.length)
            } else if (cleanedData && typeof cleanedData === 'object') {
              formattedResult = this.formatJsonObject(cleanedData)
              console.log('解析为JSON对象')
            } else {
              formattedResult = linkifyText(cleanAIResponse(result))
            }
          } else {
            formattedResult = linkifyText(cleanAIResponse(result))
          }
        } catch (e) {
          console.log('JSON解析失败，使用原始内容:', e)
          formattedResult = linkifyText(cleanAIResponse(result))
        }

        return `
          <div class="step-header">Step ${stepNumber}</div>
          <div class="tool-info">
            <span class="tool-name">🔧 ${toolName}</span>
            <span class="tool-status">✅ 任务完成</span>
          </div>
          <div class="tool-result">
            <div class="result-label">结果:</div>
            <div class="result-content">${formattedResult}</div>
          </div>
        `
      }

      // 如果无法解析，返回原始格式
      console.log('无法解析工具结果，返回原始格式')
      return `
        <div class="step-header">Step ${stepNumber}</div>
        <div class="step-content">${this.escapeHtml(content)}</div>
      `
    },

    formatWebResult(stepNumber, content) {
      console.log('格式化Web结果:', content.substring(0, 200) + '...')

      // 尝试多种方式提取JSON内容
      let jsonData = null

      // 方式1: 匹配 "结果:" 后面的JSON字符串
      const jsonMatch1 = content.match(/结果:\s*"(.+)"/)
      if (jsonMatch1) {
        try {
          const jsonStr = jsonMatch1[1].replace(/\\"/g, '"').replace(/\\\\/g, '\\')
          jsonData = JSON.parse(jsonStr)
          console.log('方式1解析成功')
        } catch (e) {
          console.log('方式1解析失败:', e)
        }
      }

      // 方式2: 直接匹配JSON对象
      if (!jsonData) {
        const jsonMatch2 = content.match(/\{[\s\S]*\}/)
        if (jsonMatch2) {
          try {
            jsonData = JSON.parse(jsonMatch2[0])
            console.log('方式2解析成功')
          } catch (e) {
            console.log('方式2解析失败:', e)
          }
        }
      }

      // 方式3: 匹配数组格式
      if (!jsonData) {
        const jsonMatch3 = content.match(/\[[\s\S]*\]/)
        if (jsonMatch3) {
          try {
            jsonData = JSON.parse(jsonMatch3[0])
            console.log('方式3解析成功')
          } catch (e) {
            console.log('方式3解析失败:', e)
          }
        }
      }

      if (jsonData) {
        console.log('原始JSON数据:', jsonData)

        // 清理JSON数据
        const cleanedData = cleanJSONData(jsonData)
        console.log('清理后的JSON数据:', cleanedData)

        if (Array.isArray(cleanedData) && cleanedData.length > 0) {
          // 使用专门的搜索结果格式化函数
          const formattedResults = formatSearchResults(cleanedData)
          return `
            <div class="step-header">Step ${stepNumber}</div>
            <div class="web-result">
              <div class="result-label">🔍 搜索结果</div>
              <div class="search-results-count">找到 ${cleanedData.length} 个相关结果</div>
              <div class="result-content">${formattedResults}</div>
            </div>
          `
        } else if (cleanedData && typeof cleanedData === 'object') {
          return `
            <div class="step-header">Step ${stepNumber}</div>
            <div class="web-result">
              <div class="result-label">🔍 搜索结果</div>
              <div class="result-content">${this.formatJsonObject(cleanedData)}</div>
            </div>
          `
        } else if (Array.isArray(jsonData) && jsonData.length > 0) {
          // 如果清理后的数据为空，但原始数据不为空，使用原始数据
          console.log('清理后数据为空，使用原始数据')
          const formattedResults = formatSearchResults(jsonData)
          return `
            <div class="step-header">Step ${stepNumber}</div>
            <div class="web-result">
              <div class="result-label">🔍 搜索结果</div>
              <div class="search-results-count">找到 ${jsonData.length} 个相关结果</div>
              <div class="result-content">${formattedResults}</div>
            </div>
          `
        }
      }

      // 如果无法解析，返回清理后的原始格式
      console.log('无法解析JSON，返回原始格式')
      const cleanedContent = cleanAIResponse(content)
      return `
        <div class="step-header">Step ${stepNumber}</div>
        <div class="step-content">${linkifyText(cleanedContent)}</div>
      `
    },

    formatJsonArray(jsonArray) {
      console.log('格式化JSON数组，长度:', jsonArray.length)

      if (!jsonArray || jsonArray.length === 0) return '<div class="empty-result">暂无数据</div>'

      // 检查是否是搜索结果格式
      const hasSearchResults = jsonArray.some(item =>
        item && (item.title || item.name) && (item.link || item.url)
      )

      if (hasSearchResults) {
        // 使用专门的搜索结果格式化函数
        return formatSearchResults(jsonArray)
      }

      // 其他类型的数组数据
      return jsonArray.map((item, index) => {
        console.log(`处理第${index + 1}项:`, item)

        // 使用新的清理函数
        const cleanItem = cleanJSONData(item)

        if (!cleanItem) {
          return '' // 跳过无效项
        }

        return this.formatGenericItem(cleanItem)
      }).filter(item => item.trim() !== '').join('')
    },

    cleanTitle(title) {
      // 清理标题，去掉无用的前缀和后缀
      if (!title) return ''

      // 去掉常见的无用前缀和后缀
      let cleanTitle = title
        .replace(/^【图片】/, '')
        .replace(/^【.*?】/, '')
        .replace(/^\[.*?\]/, '')
        .replace(/^\(.*?\)/, '')
        .replace(/^.*?[：:]\s*/, '')
        .replace(/^.*?[-\s]+/, '')
        .replace(/[-\s]+.*?$/, '')
        .replace(/[（\(].*?[）\)]$/, '')
        .replace(/[【\[].*?[】\]]$/, '')
        .replace(/^.*?[|｜]\s*/, '') // 去掉竖线分隔符
        .replace(/^.*?[>＞]\s*/, '') // 去掉大于号
        .replace(/^.*?[<＜]\s*/, '') // 去掉小于号
        .replace(/^.*?[~～]\s*/, '') // 去掉波浪号
        .replace(/^.*?[`'"]\s*/, '') // 去掉引号
        .replace(/^.*?[`'"]$/, '') // 去掉结尾引号
        .replace(/^.*?[^\u4e00-\u9fa5a-zA-Z0-9\s\-_\.]/, '') // 去掉特殊字符开头
        .replace(/[^\u4e00-\u9fa5a-zA-Z0-9\s\-_\.]$/, '') // 去掉特殊字符结尾

      // 限制标题长度
      if (cleanTitle.length > 80) {
        cleanTitle = cleanTitle.substring(0, 80) + '...'
      }

      return cleanTitle.trim() || title
    },

    cleanSnippet(snippet) {
      if (!snippet) return ''

      // 清理摘要，去掉无用的标记和过长的内容
      let cleanSnippet = snippet
        .replace(/\[.*?\]/g, '') // 去掉方括号内容
        .replace(/【.*?】/g, '') // 去掉中文方括号内容
        .replace(/\(.*?\)/g, '') // 去掉圆括号内容
        .replace(/[0-9]+楼:\s*/, '') // 去掉楼层信息
        .replace(/\.\.\./g, '') // 去掉省略号
        .replace(/[|｜].*?$/, '') // 去掉竖线后的内容
        .replace(/[>＞].*?$/, '') // 去掉大于号后的内容
        .replace(/[<＜].*?$/, '') // 去掉小于号后的内容
        .replace(/[~～].*?$/, '') // 去掉波浪号后的内容
        .replace(/[`'"].*?[`'"]/g, '') // 去掉引号包围的内容
        .replace(/^\s*[^\u4e00-\u9fa5a-zA-Z0-9\s\-_\.]+\s*/, '') // 去掉开头的特殊字符
        .replace(/\s*[^\u4e00-\u9fa5a-zA-Z0-9\s\-_\.]+\s*$/, '') // 去掉结尾的特殊字符
        .replace(/\s+/g, ' ') // 合并多个空格

      // 限制长度
      if (cleanSnippet.length > 120) {
        cleanSnippet = cleanSnippet.substring(0, 120) + '...'
      }

      return cleanSnippet.trim()
    },

    cleanSource(source) {
      if (!source) return ''

      // 清理来源信息，去掉无用的后缀
      let cleanSource = source
        .replace(/[-\s]+.*$/, '')
        .replace(/[（\(].*?[）\)]$/, '')
        .replace(/[【\[].*?[】\]]$/, '')
        .replace(/[|｜].*?$/, '') // 去掉竖线后的内容
        .replace(/[>＞].*?$/, '') // 去掉大于号后的内容
        .replace(/[<＜].*?$/, '') // 去掉小于号后的内容
        .replace(/[~～].*?$/, '') // 去掉波浪号后的内容
        .replace(/[`'"].*?[`'"]/g, '') // 去掉引号包围的内容
        .replace(/^\s*[^\u4e00-\u9fa5a-zA-Z0-9\s\-_\.]+\s*/, '') // 去掉开头的特殊字符
        .replace(/\s*[^\u4e00-\u9fa5a-zA-Z0-9\s\-_\.]+\s*$/, '') // 去掉结尾的特殊字符
        .replace(/\s+/g, ' ') // 合并多个空格

      // 限制长度
      if (cleanSource.length > 50) {
        cleanSource = cleanSource.substring(0, 50) + '...'
      }

      return cleanSource.trim()
    },



    formatGenericItem(item) {
      // 使用新的清理函数
      const cleanItem = cleanJSONData(item)

      if (!cleanItem) {
        return ''
      }

      // 尝试提取有用信息，优先显示有意义的内容
      const usefulKeys = ['name', 'title', 'description', 'content', 'text', 'info', 'summary']
      let usefulContent = ''
      let usefulLink = ''

      // 提取链接
      if (cleanItem.link || cleanItem.url) {
        usefulLink = cleanItem.link || cleanItem.url
      }

      // 提取有用内容
      for (const key of usefulKeys) {
        if (cleanItem[key] && typeof cleanItem[key] === 'string' && cleanItem[key].trim()) {
          usefulContent = this.cleanSnippet(cleanItem[key])
          break
        }
      }

      // 如果有链接和内容，显示为搜索结果格式
      if (usefulLink && usefulContent) {
        const cleanTitle = this.cleanTitle(cleanItem.title || cleanItem.name || '未知标题')
        return `
          <div class="search-result-item">
            <div class="result-content">
              <div class="result-title">
                <a href="${usefulLink}" target="_blank" class="result-link" rel="noopener noreferrer">${cleanTitle}</a>
              </div>
              <div class="result-snippet">${linkifyText(usefulContent)}</div>
            </div>
          </div>
        `
      }

      // 如果只有内容，显示为普通结果
      if (usefulContent) {
        return `
          <div class="result-item">
            <div class="result-content">${linkifyText(usefulContent)}</div>
          </div>
        `
      }

      // 如果只有链接，显示链接
      if (usefulLink) {
        return `
          <div class="result-item">
            <div class="result-content">
              <a href="${usefulLink}" target="_blank" class="result-link" rel="noopener noreferrer">${usefulLink}</a>
            </div>
          </div>
        `
      }

      // 如果都没有，尝试显示其他有用字段
      const displayFields = ['name', 'title', 'description', 'content', 'text', 'info']
      const displayContent = []

      for (const field of displayFields) {
        if (cleanItem[field] && typeof cleanItem[field] === 'string' && cleanItem[field].trim()) {
          displayContent.push(`${field}: ${linkifyText(this.cleanSnippet(cleanItem[field]))}`)
        }
      }

      if (displayContent.length > 0) {
        return `
          <div class="result-item">
            <div class="result-content">${displayContent.join('<br>')}</div>
          </div>
        `
      }

      // 最后的选择：显示清理后的JSON
      if (Object.keys(cleanItem).length > 0) {
        const jsonStr = JSON.stringify(cleanItem, null, 2)
        return `
          <div class="result-item">
            <div class="result-content">${linkifyText(jsonStr)}</div>
          </div>
        `
      }

      // 如果完全没有有用信息，显示空结果
      return '<div class="empty-result">暂无有效数据</div>'
    },

    formatJsonObject(jsonObj) {
      // 使用新的清理函数
      const cleanedObj = cleanJSONData(jsonObj)

      if (!cleanedObj || Object.keys(cleanedObj).length === 0) {
        return '<div class="empty-result">暂无有效数据</div>'
      }

      // 如果对象包含链接信息，格式化为卡片样式
      if (cleanedObj.title && cleanedObj.link) {
        const title = this.cleanTitle(cleanedObj.title)
        const description = cleanedObj.description || cleanedObj.content || cleanedObj.snippet || ''
        const cleanDesc = description ? linkifyText(this.cleanSnippet(description)) : ''

        return `
          <div class="search-result-item">
            <div class="result-title">
              <a href="${cleanedObj.link}" target="_blank" class="result-link" rel="noopener noreferrer">${title}</a>
            </div>
            ${cleanDesc ? `<div class="result-snippet">${cleanDesc}</div>` : ''}
          </div>
        `
      }

      // 否则显示为格式化的JSON
      const jsonStr = JSON.stringify(cleanedObj, null, 2)
      return `<div class="result-object">${linkifyText(jsonStr)}</div>`
    },

    // formatJsonStep(stepNumber, content) {
    //   // 尝试提取JSON内容
    //   const jsonMatch = content.match(/\{[\s\S]*\}/)
    //   if (jsonMatch) {
    //     try {
    //       const jsonData = JSON.parse(jsonMatch[0])
    //       const cleanedData = cleanJSONData(jsonData)
    //
    //       if (Array.isArray(cleanedData) && cleanedData.length > 0) {
    //         return `
    //           <div class="step-header">Step ${stepNumber}</div>
    //           <div class="web-result">
    //             <div class="result-label">📊 数据结果</div>
    //             <div class="search-results-count">找到 ${cleanedData.length} 条数据</div>
    //             <div class="result-content">${this.formatJsonArray(cleanedData)}</div>
    //           </div>
    //         `
    //       } else if (cleanedData && typeof cleanedData === 'object') {
    //         return `
    //           <div class="step-header">Step ${stepNumber}</div>
    //           <div class="web-result">
    //             <div class="result-label">📊 数据结果</div>
    //             <div class="result-content">${this.formatJsonObject(cleanedData)}</div>
    //           </div>
    //         `
    //       }
    //     } catch (e) {
    //       console.log('JSON解析失败:', e)
    //     }
    //   }
    //
    //   // 如果无法解析JSON，返回清理后的原始格式
    //   const cleanedContent = cleanAIResponse(content)
    //   return `
    //     <div class="step-header">Step ${stepNumber}</div>
    //     <div class="step-content">${linkifyText(cleanedContent)}</div>
    //   `
    // },

    escapeHtml(text) {
      const div = document.createElement('div')
      div.textContent = text
      return div.innerHTML
    },

    truncateText(text, maxLength = 500) {
      if (!text || text.length <= maxLength) {
        return text
      }

      // 在合适的位置截断文本
      const truncated = text.substring(0, maxLength)
      const lastSpace = truncated.lastIndexOf(' ')

      if (lastSpace > maxLength * 0.8) {
        return truncated.substring(0, lastSpace) + '...'
      }

      return truncated + '...'
    },

    scrollToBottom() {
      this.$nextTick(() => {
        if (this.$refs.chatMessages) {
          this.$refs.chatMessages.scrollTop = this.$refs.chatMessages.scrollHeight
        }
      })
    },

    goBack() {
      this.$router.push('/')
    }
  }
}
</script>

<style scoped>
.manus-app {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  overflow: hidden;
}

.header {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  padding: 1rem 2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.back-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.header h1 {
  color: white;
  margin: 0;
  font-size: 1.5rem;
}

.chat-id {
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.8rem;
  background: rgba(255, 255, 255, 0.1);
  padding: 0.3rem 0.8rem;
  border-radius: 15px;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 1rem;
  min-height: 0;
  background: #f8f9fa;
  border-radius: 20px;
  margin: 0.5rem;
  border: 3px solid #4facfe;
  box-shadow: 0 4px 16px rgba(79, 172, 254, 0.2);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  min-height: 0;
  background: #f8f9fa;
  border-radius: 15px;
}

.message {
  display: flex;
  margin-bottom: 1rem;
}

.message.user {
  justify-content: flex-end;
}

.message.ai {
  justify-content: flex-start;
}

.message-content {
  max-width: 70%;
  padding: 1rem;
  border-radius: 20px;
  position: relative;
  min-width: 200px;
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: break-word;
  overflow-x: hidden;
  text-align: left;
  line-height: 1.6;
}

.message.user .message-content {
  background: #4facfe;
  color: white;
  border-bottom-right-radius: 5px;
}

.message.ai .message-content {
  background: #ffffff;
  color: #333;
  border-bottom-left-radius: 5px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.message-text {
  line-height: 1.6;
  margin-bottom: 0.5rem;
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: break-word;
  white-space: pre-wrap;
  overflow-x: hidden;
  max-width: 100%;
  text-align: left;
  font-size: 0.95rem;
}

/* Step内容样式 */
.step-header {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
  padding: 1rem 1.5rem;
  border-radius: 15px 15px 0 0;
  font-weight: bold;
  font-size: 1.1rem;
  margin: -1rem -1rem 1rem -1rem;
  text-align: center;
  box-shadow: 0 2px 8px rgba(79, 172, 254, 0.3);
  position: relative;
  overflow: hidden;
}

.step-header::before {
  content: "📋";
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  font-size: 1.2rem;
  opacity: 0.8;
}

.step-content {
  padding: 0.8rem 0;
  color: #333;
  font-size: 0.95rem;
  line-height: 1.6;
  text-align: left;
  white-space: pre-wrap;
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: break-word;
}

/* 工具执行结果样式 */
.tool-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding: 0.8rem;
  background: rgba(79, 172, 254, 0.1);
  border-radius: 12px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.tool-name {
  font-weight: bold;
  color: #4facfe;
  font-size: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.tool-name::before {
  content: "⚙️";
  font-size: 1.2rem;
}

.tool-status {
  color: #00d4aa;
  font-size: 0.9rem;
  font-weight: 600;
  background: rgba(0, 212, 170, 0.1);
  padding: 0.3rem 0.8rem;
  border-radius: 20px;
  border: 1px solid rgba(0, 212, 170, 0.3);
}

.tool-result {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  padding: 1.2rem;
  border-left: 4px solid #4facfe;
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: break-word;
  overflow-x: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  text-align: left;
}

.result-label {
  font-weight: bold;
  color: #4facfe;
  margin-bottom: 0.8rem;
  font-size: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.result-label::before {
  content: "📋";
  font-size: 1.2rem;
}

.result-content {
  color: #333;
  font-size: 0.95rem;
  line-height: 1.6;
  text-align: left;
}

/* 搜索结果样式 */
.search-result-item {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  padding: 1.2rem;
  margin-bottom: 1rem;
  border-left: 4px solid #00d4aa;
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: break-word;
  overflow-x: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  text-align: left;
  max-width: 100%;
  display: flex;
  gap: 1rem;
  align-items: flex-start;
}

.result-index {
  background: #00d4aa;
  color: white;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  font-weight: bold;
  flex-shrink: 0;
  margin-top: 0.2rem;
}

.result-content {
  flex: 1;
  min-width: 0;
}

.search-result-item:hover {
  background: rgba(255, 255, 255, 1);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.search-result-item:last-child {
  margin-bottom: 0;
}

.result-title {
  margin-bottom: 0.5rem;
  text-align: left;
}

.result-link {
  color: #4facfe;
  text-decoration: none;
  font-weight: bold;
  font-size: 1.1rem;
  line-height: 1.4;
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: break-word;
  display: block;
  padding: 0.5rem 0;
  transition: all 0.3s ease;
  border-radius: 6px;
  max-width: 100%;
  text-align: left;
}

.result-link:hover {
  text-decoration: none;
  color: #00d4aa;
  background: rgba(79, 172, 254, 0.1);
  padding: 0.5rem 0.8rem;
  margin: 0 -0.8rem;
}

.result-snippet {
  color: #666;
  font-size: 0.9rem;
  line-height: 1.5;
  margin-bottom: 0.5rem;
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: break-word;
  text-align: left;
  max-width: 100%;
}

.result-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.8rem;
  color: #888;
  text-align: left;
}

.result-source {
  color: #00d4aa;
  font-weight: 500;
  font-size: 0.85rem;
  background: rgba(0, 212, 170, 0.1);
  padding: 0.3rem 0.6rem;
  border-radius: 12px;
  display: inline-block;
  margin-top: 0.5rem;
}

.result-date {
  color: #888;
}

/* 其他结果样式 */
.result-item, .result-object {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  padding: 1.2rem;
  font-family: 'Courier New', monospace;
  font-size: 0.9rem;
  color: #333;
  white-space: pre-wrap;
  overflow-x: auto;
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: break-word;
  max-width: 100%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-left: 4px solid #4facfe;
  text-align: left;
  line-height: 1.6;
}

.empty-result {
  text-align: center;
  color: #888;
  font-style: italic;
  padding: 2rem;
}

.web-result {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  padding: 1.2rem;
  border-left: 4px solid #00d4aa;
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: break-word;
  overflow-x: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  text-align: left;
}

.message-time {
  font-size: 0.7rem;
  opacity: 0.7;
  text-align: right;
}

.message.user .message-time {
  color: rgba(255, 255, 255, 0.8);
}

.message.ai .message-time {
  color: rgba(0, 0, 0, 0.6);
}

/* 通用内容溢出处理 */
* {
  box-sizing: border-box;
}

/* 确保所有文本内容都能正确换行 */
.message-content *,
.search-result-item *,
.tool-result *,
.web-result *,
.result-item *,
.result-object * {
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: break-word;
  max-width: 100%;
  text-align: left;
  line-height: 1.6;
}

/* 优化step内容的显示 */
.step-content,
.result-content,
.tool-result,
.web-result {
  max-width: 100%;
  overflow-x: auto;
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: break-word;
  text-align: left;
  line-height: 1.6;
}

/* 确保所有文本内容左对齐 */
.message.ai .message-content,
.step-header,
.step-content,
.tool-info,
.tool-result,
.search-result-item,
.result-item,
.result-object,
.web-result,
.result-label,
.result-content,
.result-title,
.result-snippet,
.result-source {
  text-align: left !important;
}

/* 确保链接不会超出容器 */
.result-link,
.result-title a,
.auto-link {
  max-width: 100%;
  display: inline-block;
  word-wrap: break-word;
  word-break: break-all;
  overflow-wrap: break-word;
}

/* 自动链接样式 */
.auto-link {
  color: #4facfe;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
  border-radius: 4px;
  padding: 0.1rem 0.3rem;
  margin: 0 0.1rem;
  background: rgba(79, 172, 254, 0.1);
  border: 1px solid rgba(79, 172, 254, 0.2);
}

.auto-link:hover {
  color: #00d4aa;
  background: rgba(0, 212, 170, 0.1);
  border-color: rgba(0, 212, 170, 0.3);
  text-decoration: none;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}



/* 限制气泡最大宽度，防止过长 */
.message.ai .message-content {
  max-width: 85% !important;
  min-width: 250px;
}

.message.ai[data-step="true"] .message-content {
  max-width: 90% !important;
  min-width: 300px;
}

/* 确保长文本能够正确换行 */
.message.ai .message-content * {
  max-width: 100% !important;
  word-wrap: break-word !important;
  word-break: break-word !important;
  overflow-wrap: break-word !important;
}

/* 优化长文本显示 */
.message-text p,
.step-content p,
.result-content p {
  margin: 0.5rem 0;
  text-align: left;
}

/* 确保链接和按钮左对齐 */
.message-text a,
.result-link {
  text-align: left;
  display: inline-block;
}

/* Step消息特殊样式 */
.message.ai[data-step="true"] .message-content {
  background: rgba(255, 255, 255, 0.95);
  color: #333;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(79, 172, 254, 0.2);
  margin-bottom: 1.5rem;
  max-width: 85%;
  min-width: 300px;
  text-align: left;
  line-height: 1.6;
  padding: 1.2rem;
  border-radius: 15px;
}

/* 优化step内容的宽度控制 */
.message.ai[data-step="true"] .step-content,
.message.ai[data-step="true"] .result-content,
.message.ai[data-step="true"] .tool-result,
.message.ai[data-step="true"] .web-result {
  max-width: 100%;
  overflow-x: auto;
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: break-word;
}

/* 搜索结果计数 */
.search-results-count {
  background: rgba(79, 172, 254, 0.1);
  color: #4facfe;
  font-size: 0.9rem;
  font-weight: 600;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  margin-bottom: 1rem;
  text-align: center;
  border: 1px solid rgba(79, 172, 254, 0.2);
}

/* 空结果样式 */
.empty-result {
  text-align: center;
  color: #888;
  font-style: italic;
  padding: 2rem;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 12px;
  border: 2px dashed rgba(79, 172, 254, 0.3);
}

/* Step消息特殊样式 */
.message.ai[data-step="true"] .message-content {
  background: rgba(255, 255, 255, 0.95);
  color: #333;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(79, 172, 254, 0.2);
  max-width: 85%;
  min-width: 300px;
}

/* 工具名称和状态的特殊样式 */
.tool-name {
  font-weight: bold;
  color: #4facfe;
  font-size: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.tool-name::before {
  content: "⚙️";
  font-size: 1.2rem;
}

.tool-status {
  color: #00d4aa;
  font-size: 0.9rem;
  font-weight: 600;
  background: rgba(0, 212, 170, 0.1);
  padding: 0.3rem 0.8rem;
  border-radius: 20px;
  border: 1px solid rgba(0, 212, 170, 0.3);
}

.input-container {
  padding: 1rem;
  background: transparent;
  border-radius: 20px;
  margin-top: 1rem;
}

.input-wrapper {
  display: flex;
  gap: 1rem;
}

.message-input {
  flex: 1;
  padding: 1rem;
  border: none;
  border-radius: 25px;
  background: #ffffff;
  font-size: 1rem;
  outline: none;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.message-input:focus {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.send-btn {
  padding: 1rem 2rem;
  border: none;
  border-radius: 25px;
  background: #6c757d;
  color: white;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.send-btn:hover:not(:disabled) {
  background: #5a6268;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
}

@media (max-width: 768px) {
  .header {
    padding: 1rem;
    flex-direction: column;
    gap: 0.5rem;
  }

  .header h1 {
    font-size: 1.2rem;
  }

  .chat-id {
    font-size: 0.7rem;
  }

  .message-content {
    max-width: 85%;
  }
}

/* Markdown样式 */
.message-content h1,
.message-content h2,
.message-content h3,
.message-content h4,
.message-content h5,
.message-content h6 {
  color: #333;
  margin: 1.2rem 0 0.8rem 0;
  font-weight: bold;
  line-height: 1.3;
}

.message-content h1 {
  font-size: 1.8rem;
  border-bottom: 2px solid #4facfe;
  padding-bottom: 0.5rem;
}

.message-content h2 {
  font-size: 1.5rem;
  border-bottom: 1px solid #e1e4e8;
  padding-bottom: 0.3rem;
}

.message-content h3 {
  font-size: 1.3rem;
  color: #4facfe;
}

.message-content h4 {
  font-size: 1.1rem;
}

.message-content h5,
.message-content h6 {
  font-size: 1rem;
  color: #666;
}

/* 列表样式 */
.message-content ul,
.message-content ol {
  margin: 1rem 0;
  padding-left: 2rem;
  line-height: 1.6;
}

.message-content li {
  margin: 0.5rem 0;
}

.message-content ul li {
  list-style-type: disc;
}

.message-content ol li {
  list-style-type: decimal;
}

/* 代码样式 */
.message-content .code-block {
  background: #f6f8fa;
  border: 1px solid #e1e4e8;
  border-radius: 8px;
  padding: 1rem;
  margin: 1rem 0;
  overflow-x: auto;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.9rem;
  line-height: 1.4;
}

.message-content .code-block code {
  background: none;
  padding: 0;
  border: none;
  font-size: inherit;
}

.message-content .inline-code {
  background: #f3f4f6;
  color: #e83e8c;
  padding: 0.2rem 0.4rem;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.9em;
  border: 1px solid #e1e4e8;
}

/* 引用样式 */
.message-content blockquote {
  border-left: 4px solid #4facfe;
  margin: 1rem 0;
  padding: 0.5rem 1rem;
  background: rgba(79, 172, 254, 0.05);
  color: #666;
  font-style: italic;
}

/* 表格样式 */
.message-content table {
  border-collapse: collapse;
  width: 100%;
  margin: 1rem 0;
  font-size: 0.9rem;
}

.message-content th,
.message-content td {
  border: 1px solid #e1e4e8;
  padding: 0.6rem 1rem;
  text-align: left;
}

.message-content th {
  background: #f6f8fa;
  font-weight: bold;
  color: #333;
}

.message-content tr:nth-child(even) {
  background: #f9f9f9;
}

/* 分割线样式 */
.message-content hr {
  border: none;
  height: 2px;
  background: linear-gradient(to right, #4facfe, #00d4aa);
  margin: 2rem 0;
  border-radius: 1px;
}

/* 强调样式 */
.message-content strong,
.message-content b {
  font-weight: bold;
  color: #333;
}

.message-content em,
.message-content i {
  font-style: italic;
  color: #666;
}

/* 段落样式 */
.message-content p {
  margin: 1rem 0;
  line-height: 1.6;
}
</style>

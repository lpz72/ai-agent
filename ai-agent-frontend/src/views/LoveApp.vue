<template>
  <div class="love-app">
    <div class="header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h1>AI恋爱大师 💕</h1>
      <div class="chat-id">会话ID: {{ chatId }}</div>
    </div>
    
    <div class="chat-container">
      <div class="chat-messages" ref="chatMessages">
        <div 
          v-for="(message, index) in messages" 
          :key="index" 
          :class="['message', message.type]"
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
import axios from 'axios'

export default {
  name: 'LoveApp',
  data() {
    return {
      chatId: '',
      messages: [],
      inputMessage: '',
      isLoading: false,
      eventSource: null
    }
  },
  mounted() {
    this.generateChatId()
    this.addWelcomeMessage()
  },
  beforeUnmount() {
    if (this.eventSource) {
      this.eventSource.close()
    }
  },
  methods: {
    generateChatId() {
      this.chatId = 'love_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
    },
    
    addWelcomeMessage() {
      this.messages.push({
        type: 'ai',
        content: '你好！我是你的AI恋爱大师 💕<br>我可以帮你解答恋爱中的各种问题，提供专业的建议和指导。<br>请告诉我你的困扰，我会尽力帮助你！',
        time: this.getCurrentTime()
      })
    },
    
    async sendMessage() {
      if (!this.inputMessage.trim() || this.isLoading) return
      
      const userMessage = this.inputMessage.trim()
      this.messages.push({
        type: 'user',
        content: userMessage,
        time: this.getCurrentTime()
      })
      
      this.inputMessage = ''
      this.isLoading = true
      
      // 添加AI正在输入的提示
      const loadingMessageIndex = this.messages.length
      this.messages.push({
        type: 'ai',
        content: '正在思考中...',
        time: this.getCurrentTime()
      })
      
      try {
        await this.callLoveAppSSE(userMessage, loadingMessageIndex)
      } catch (error) {
        console.error('发送消息失败:', error)
        this.messages[loadingMessageIndex].content = '抱歉，发送消息失败，请重试。'
      } finally {
        this.isLoading = false
        this.scrollToBottom()
      }
    },
    
    async callLoveAppSSE(message, messageIndex) {
      try {
        // 使用EventSource API，更简单可靠
        const eventSource = new EventSource(`http://localhost:8123/api/ai/love_app/chat/sse?message=${encodeURIComponent(message)}&chatId=${this.chatId}`)
        
        let fullContent = ''
        
        eventSource.onmessage = (event) => {
          console.log('接收到SSE消息:', event.data)
          
          if (event.data && event.data.trim() !== '') {
            fullContent += event.data
            console.log('当前完整内容:', fullContent)
            
            // 检查是否包含step，如果是则分别显示
            if (fullContent.includes('step')) {
              this.processStepContent(fullContent, messageIndex)
            } else {
              // 实时更新显示内容
              this.messages[messageIndex].content = fullContent
            }
            this.scrollToBottom()
          }
        }
        
        eventSource.onerror = (error) => {
          console.error('SSE连接错误:', error)
          eventSource.close()
          
          // 如果内容为空，显示默认回复
          if (!fullContent.trim()) {
            this.messages[messageIndex].content = '感谢您的咨询！我会继续努力为您提供更好的恋爱建议。'
          }
        }
        
        // 设置超时，防止无限等待
        setTimeout(() => {
          eventSource.close()
          console.log('SSE连接超时，最终内容:', fullContent)
          
          // 如果内容为空，显示默认回复
          if (!fullContent.trim()) {
            this.messages[messageIndex].content = '感谢您的咨询！我会继续努力为您提供更好的恋爱建议。'
          } else if (fullContent.includes('step')) {
            // 处理最终的step内容
            this.processStepContent(fullContent, messageIndex)
          }
        }, 30000) // 30秒超时
        
      } catch (error) {
        console.error('SSE调用失败:', error)
        this.messages[messageIndex].content = '抱歉，连接失败，请检查网络连接或稍后重试。'
      }
    },
    
    processStepContent(content, messageIndex) {
      console.log('开始处理step内容:', content)
      
      // 移除原始的"正在思考中..."消息
      this.messages.splice(messageIndex, 1)
      
      // 使用更智能的正则表达式匹配step内容
      // 匹配 step 数字 内容，直到下一个step或结束
      const stepRegex = /step\s*(\d+)\s*([^]*?)(?=step\s*\d+|$)/gi
      const steps = []
      let match
      
      while ((match = stepRegex.exec(content)) !== null) {
        const stepNumber = match[1]
        const stepContent = match[2].trim()
        console.log(`找到Step ${stepNumber}:`, stepContent)
        if (stepContent) {
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
        steps.forEach((step) => {
          this.messages.push({
            type: 'ai',
            content: step.formattedContent,
            time: this.getCurrentTime(),
            isStep: true,
            stepNumber: step.number
          })
        })
      } else {
        // 如果没有匹配到step，显示原始内容
        this.messages.push({
          type: 'ai',
          content: content,
          time: this.getCurrentTime()
        })
      }
    },
    
    formatStepContent(stepNumber, content) {
      // 检查是否是工具执行结果
      if (content.includes('工具') && content.includes('完成了它的任务')) {
        return this.formatToolResult(stepNumber, content)
      }
      
      // 检查是否包含特殊格式的内容
      if (content.includes('searchWeb') || content.includes('scrapeWeb')) {
        return this.formatWebResult(stepNumber, content)
      }
      
      // 普通step内容
      return `<div class="step-header">Step ${stepNumber}</div><div class="step-content">${content}</div>`
    },
    
    formatWebResult(stepNumber, content) {
      // 尝试提取JSON内容
      const jsonMatch = content.match(/结果:\s*"(.+)"/)
      if (jsonMatch) {
        try {
          const jsonStr = jsonMatch[1].replace(/\\"/g, '"').replace(/\\\\/g, '\\')
          const jsonData = JSON.parse(jsonStr)
          
          if (Array.isArray(jsonData)) {
            return `
              <div class="step-header">Step ${stepNumber}</div>
              <div class="web-result">
                <div class="result-label">🔍 搜索结果</div>
                <div class="result-content">${this.formatJsonArray(jsonData)}</div>
              </div>
            `
          }
        } catch (e) {
          console.log('Web结果JSON解析失败:', e)
        }
      }
      
      // 如果无法解析，返回原始格式
      return `<div class="step-header">Step ${stepNumber}</div><div class="step-content">${content}</div>`
    },
    
    formatToolResult(stepNumber, content) {
      // 解析工具执行结果
      const toolMatch = content.match(/工具\s+(\w+)\s+完成了它的任务！结果:\s*(.+)/)
      
      if (toolMatch) {
        const toolName = toolMatch[1]
        const result = toolMatch[2]
        
        let formattedResult = result
        
        // 尝试解析JSON结果
        try {
          if (result.startsWith('"') && result.endsWith('"')) {
            const jsonStr = result.slice(1, -1).replace(/\\"/g, '"').replace(/\\\\/g, '\\')
            const jsonData = JSON.parse(jsonStr)
            
            if (Array.isArray(jsonData)) {
              // 如果是数组，格式化显示
              formattedResult = this.formatJsonArray(jsonData)
            } else {
              formattedResult = this.formatJsonObject(jsonData)
            }
          }
        } catch (e) {
          console.log('JSON解析失败，使用原始内容')
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
      return `<div class="step-header">Step ${stepNumber}</div><div class="step-content">${content}</div>`
    },
    
    formatJsonArray(jsonArray) {
      if (jsonArray.length === 0) return '<div class="empty-result">暂无数据</div>'
      
      return jsonArray.map((item, index) => {
        if (item.title && item.link) {
          // 搜索结果格式
          return `
            <div class="search-result-item">
              <div class="result-title">
                <a href="${item.link}" target="_blank" class="result-link">${item.title}</a>
              </div>
              <div class="result-snippet">${item.snippet || ''}</div>
              <div class="result-meta">
                <span class="result-source">${item.displayed_link || ''}</span>
                ${item.date ? `<span class="result-date">${item.date}</span>` : ''}
              </div>
            </div>
          `
        } else {
          // 其他格式
          return `<div class="result-item">${JSON.stringify(item, null, 2)}</div>`
        }
      }).join('')
    },
    
    formatJsonObject(jsonObj) {
      return `<div class="result-object">${JSON.stringify(jsonObj, null, 2)}</div>`
    },
    
    getCurrentTime() {
      return new Date().toLocaleTimeString('zh-CN', { 
        hour: '2-digit', 
        minute: '2-digit' 
      })
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
.love-app {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #ff6b9d 0%, #c44569 100%);
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
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  min-height: 0;
  background: #f8f9fa;
  border-radius: 15px;
  margin: 0.5rem 0;
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
}

.message.user .message-content {
  background: #007bff;
  color: white;
  border-bottom-right-radius: 5px;
  box-shadow: 0 2px 8px rgba(0, 123, 255, 0.3);
}

.message.ai .message-content {
  background: white;
  color: #333;
  border-bottom-left-radius: 5px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.message-text {
  line-height: 1.5;
  margin-bottom: 0.5rem;
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: break-word;
  white-space: pre-wrap;
}

/* Step内容样式 */
.step-header {
  background: linear-gradient(135deg, #ff6b9d 0%, #c44569 100%);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 15px 15px 0 0;
  font-weight: bold;
  font-size: 0.9rem;
  margin: -1rem -1rem 1rem -1rem;
  text-align: center;
}

.step-content {
  padding: 0.5rem 0;
  color: #333;
}

/* 工具执行结果样式 */
.tool-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding: 0.5rem;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.tool-name {
  font-weight: bold;
  color: #ff6b9d;
}

.tool-status {
  color: #00d4aa;
  font-size: 0.8rem;
}

.tool-result {
  background: white;
  border-radius: 10px;
  padding: 1rem;
  border-left: 4px solid #ff6b9d;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.result-label {
  font-weight: bold;
  color: #ff6b9d;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.result-content {
  color: #333;
}

/* 搜索结果样式 */
.search-result-item {
  background: white;
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 1rem;
  border-left: 3px solid #00d4aa;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.search-result-item:last-child {
  margin-bottom: 0;
}

.result-title {
  margin-bottom: 0.5rem;
}

.result-link {
  color: #ff6b9d;
  text-decoration: none;
  font-weight: bold;
  font-size: 1rem;
  line-height: 1.4;
}

.result-link:hover {
  text-decoration: underline;
  color: #00d4aa;
}

.result-snippet {
  color: #b0b0b0;
  font-size: 0.9rem;
  line-height: 1.5;
  margin-bottom: 0.5rem;
}

.result-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.8rem;
  color: #888;
}

.result-source {
  color: #00d4aa;
  font-weight: 500;
}

.result-date {
  color: #888;
}

/* 其他结果样式 */
.result-item, .result-object {
  background: white;
  border-radius: 8px;
  padding: 1rem;
  font-family: 'Courier New', monospace;
  font-size: 0.85rem;
  color: #333;
  white-space: pre-wrap;
  overflow-x: auto;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.empty-result {
  text-align: center;
  color: #888;
  font-style: italic;
  padding: 2rem;
}

.web-result {
  background: white;
  border-radius: 10px;
  padding: 1rem;
  border-left: 4px solid #00d4aa;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.message-time {
  font-size: 0.7rem;
  opacity: 0.7;
  text-align: right;
}

.input-container {
  padding: 1rem;
  background: #f8f9fa;
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
  background: white;
  font-size: 1rem;
  outline: none;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.message-input:focus {
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.5);
}

.send-btn {
  padding: 1rem 2rem;
  border: none;
  border-radius: 25px;
  background: #007bff;
  color: white;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  box-shadow: 0 2px 6px rgba(0, 123, 255, 0.3);
}

.send-btn:hover:not(:disabled) {
  background: #0056b3;
  transform: translateY(-2px);
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
</style>

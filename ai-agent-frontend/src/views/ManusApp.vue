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
import axios from 'axios'

export default {
  name: 'ManusApp',
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
      this.chatId = 'manus_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
    },
    
    addWelcomeMessage() {
      this.messages.push({
        type: 'ai',
        content: '你好！我是AI超级智能体 🤖<br>我可以帮助你解决各种问题，包括但不限于：<br>• 技术问题<br>• 学习辅导<br>• 创意写作<br>• 数据分析<br>• 生活建议<br>请告诉我你需要什么帮助！',
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
        await this.callManusSSE(userMessage, loadingMessageIndex)
      } catch (error) {
        console.error('发送消息失败:', error)
        this.messages[loadingMessageIndex].content = '抱歉，发送消息失败，请重试。'
      } finally {
        this.isLoading = false
        this.scrollToBottom()
      }
    },
    
    async callManusSSE(message, messageIndex) {
      try {
        // 使用EventSource API，更简单可靠
        const eventSource = new EventSource(`http://localhost:8123/api/ai/manus/chat?message=${encodeURIComponent(message)}`)
        
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
            this.messages[messageIndex].content = '感谢您的咨询！我会继续努力为您提供更好的帮助。'
          }
        }
        
        // 设置超时，防止无限等待
        setTimeout(() => {
          eventSource.close()
          console.log('SSE连接超时，最终内容:', fullContent)
          
          // 如果内容为空，显示默认回复
          if (!fullContent.trim()) {
            this.messages[messageIndex].content = '感谢您的咨询！我会继续努力为您提供更好的帮助。'
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
}

.message.user .message-content {
  background: rgba(255, 255, 255, 0.9);
  color: #333;
  border-bottom-right-radius: 5px;
}

.message.ai .message-content {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border-bottom-left-radius: 5px;
}

.message-text {
  line-height: 1.5;
  margin-bottom: 0.5rem;
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: break-word;
  white-space: pre-wrap;
  overflow-x: hidden;
  max-width: 100%;
}

/* Step内容样式 */
.step-header {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
  padding: 0.8rem 1.2rem;
  border-radius: 15px 15px 0 0;
  font-weight: bold;
  font-size: 1rem;
  margin: -1rem -1rem 1rem -1rem;
  text-align: center;
  box-shadow: 0 2px 8px rgba(79, 172, 254, 0.3);
}

.step-content {
  padding: 0.8rem 0;
  color: #333;
  font-size: 0.95rem;
  line-height: 1.6;
}

/* 工具执行结果样式 */
.tool-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding: 0.8rem;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.tool-name {
  font-weight: bold;
  color: #4facfe;
}

.tool-status {
  color: #00d4aa;
  font-size: 0.8rem;
}

.tool-result {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 1.2rem;
  border-left: 4px solid #4facfe;
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: break-word;
  overflow-x: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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
}

/* 搜索结果样式 */
.search-result-item {
  background: rgba(255, 255, 255, 0.15);
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
}

.search-result-item:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.search-result-item:last-child {
  margin-bottom: 0;
}

.result-title {
  margin-bottom: 0.5rem;
}

.result-link {
  color: #4facfe;
  text-decoration: none;
  font-weight: bold;
  font-size: 1rem;
  line-height: 1.4;
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: break-word;
  display: block;
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
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: break-word;
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
  background: rgba(255, 255, 255, 0.15);
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
}

.empty-result {
  text-align: center;
  color: #888;
  font-style: italic;
  padding: 2rem;
}

.web-result {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  padding: 1.2rem;
  border-left: 4px solid #00d4aa;
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: break-word;
  overflow-x: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.message-time {
  font-size: 0.7rem;
  opacity: 0.7;
  text-align: right;
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
}

/* Step消息特殊样式 */
.message.ai[data-step="true"] .message-content {
  background: rgba(255, 255, 255, 0.95);
  color: #333;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(79, 172, 254, 0.2);
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
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
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
  background: rgba(255, 255, 255, 0.9);
  font-size: 1rem;
  outline: none;
}

.message-input:focus {
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.5);
}

.send-btn {
  padding: 1rem 2rem;
  border: none;
  border-radius: 25px;
  background: #4facfe;
  color: white;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.send-btn:hover:not(:disabled) {
  background: #3d9be8;
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

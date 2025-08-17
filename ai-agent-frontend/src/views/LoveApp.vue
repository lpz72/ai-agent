<template>
  <div class="fitness-app">
    <div class="header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h1>AI减肥大师 💪</h1>
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
            placeholder="输入您的减肥问题..."
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
import { generateId, formatCurrentTime } from '../utils/index.js'

export default {
  name: 'FitnessApp',
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
      this.chatId = generateId('fitness')
    },
    
    addWelcomeMessage() {
      this.messages.push({
        type: 'ai',
        content: '你好！我是你的AI减肥大师 💪<br>我可以帮你制定个性化的减肥计划，提供营养建议、运动指导和生活习惯改善方案。<br>请告诉我你的减肥目标和困扰，我会为你量身定制解决方案！',
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
        content: '正在分析中...',
        time: formatCurrentTime()
      })
      
      try {
        // 使用新的API服务，实时更新内容
        const result = await this.callFitnessAppSSE(userMessage, loadingMessageIndex)
        
        // 如果响应包含step内容，进行处理
        if (result && result.content && result.content.includes('step')) {
          this.processStepContent(result.content, loadingMessageIndex)
        }
      } catch (error) {
        console.error('发送消息失败:', error)
        this.messages[loadingMessageIndex].content = '抱歉，发送消息失败，请重试。'
      } finally {
        this.isLoading = false
        this.scrollToBottom()
      }
    },
    
    // 直接调用SSE，实时更新内容
    async callFitnessAppSSE(message, messageIndex) {
      try {
        const eventSource = new EventSource(
          `http://localhost:8123/api/ai/love_app/chat/sse?message=${encodeURIComponent(message)}&chatId=${this.chatId}`
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
              console.log('SSE收到消息:', event.data)
              console.log('当前完整内容:', fullContent)
              
              // 实时更新消息内容
              this.messages[messageIndex].content = fullContent
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
    
    processStepContent(content, messageIndex) {
      console.log('开始处理step内容:', content)
      
      // 移除原始的"正在分析中..."消息
      this.messages.splice(messageIndex, 1)
      
      // 使用正则表达式匹配step内容
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
            time: formatCurrentTime(),
            isStep: true,
            stepNumber: step.number
          })
        })
      } else {
        // 如果没有匹配到step，显示原始内容
        this.messages.push({
          type: 'ai',
          content: content,
          time: formatCurrentTime()
        })
      }
    },
    
    formatStepContent(stepNumber, content) {
      return `
        <div class="step-header">Step ${stepNumber}</div>
        <div class="step-content">${content}</div>
      `
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
.fitness-app {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  background: #28a745;
  color: white;
  border-bottom-right-radius: 5px;
  box-shadow: 0 2px 8px rgba(40, 167, 69, 0.3);
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  color: #667eea;
}

.tool-status {
  color: #28a745;
  font-size: 0.8rem;
}

.tool-result {
  background: white;
  border-radius: 10px;
  padding: 1rem;
  border-left: 4px solid #667eea;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.result-label {
  font-weight: bold;
  color: #667eea;
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
  border-left: 3px solid #28a745;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.search-result-item:last-child {
  margin-bottom: 0;
}

.result-title {
  margin-bottom: 0.5rem;
}

.result-link {
  color: #667eea;
  text-decoration: none;
  font-weight: bold;
  font-size: 1rem;
  line-height: 1.4;
}

.result-link:hover {
  text-decoration: underline;
  color: #28a745;
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
  color: #28a745;
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
  border-left: 4px solid #28a745;
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
  background: #28a745;
  color: white;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  box-shadow: 0 2px 6px rgba(40, 167, 69, 0.3);
}

.send-btn:hover:not(:disabled) {
  background: #218838;
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

import api from './config.js'

// AI服务API
export const aiService = {
  // AI恋爱大师 - SSE聊天
  async loveAppChatSSE(message, chatId) {
    try {
      const eventSource = new EventSource(
        `${api.defaults.baseURL}/ai/love_app/chat/sse?message=${encodeURIComponent(message)}&chatId=${chatId}`
      )
      
      return new Promise((resolve, reject) => {
        let fullContent = ''
        let isResolved = false
        
        const cleanup = () => {
          eventSource.close()
          if (!isResolved) {
            isResolved = true
            resolve({
              success: true,
              content: fullContent || '感谢您的咨询！我会继续努力为您提供更好的恋爱建议。',
              isComplete: true
            })
          }
        }
        
        // 设置超时
        const timeout = setTimeout(cleanup, 30000)
        
        eventSource.onmessage = (event) => {
          if (event.data && event.data.trim() !== '') {
            fullContent += event.data
            console.log('SSE收到消息:', event.data)
            console.log('当前完整内容:', fullContent)
          }
        }
        
        eventSource.onerror = (error) => {
          console.error('SSE连接错误:', error)
          cleanup()
          clearTimeout(timeout)
          
          if (!isResolved) {
            isResolved = true
            reject(new Error('SSE连接失败'))
          }
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
  
  // AI超级智能体 - SSE聊天
  async manusChatSSE(message) {
    try {
      const eventSource = new EventSource(
        `${api.defaults.baseURL}/ai/manus/chat?message=${encodeURIComponent(message)}`
      )
      
      return new Promise((resolve, reject) => {
        let fullContent = ''
        let isResolved = false
        
        const cleanup = () => {
          eventSource.close()
          if (!isResolved) {
            isResolved = true
            resolve({
              success: true,
              content: fullContent || '感谢您的咨询！我会继续努力为您提供更好的帮助。',
              isComplete: true
            })
          }
        }
        
        // 设置超时
        const timeout = setTimeout(cleanup, 30000)
        
        eventSource.onmessage = (event) => {
          if (event.data && event.data.trim() !== '') {
            fullContent += event.data
            console.log('SSE收到消息:', event.data)
            console.log('当前完整内容:', fullContent)
          }
        }
        
        eventSource.onerror = (error) => {
          console.error('SSE连接错误:', error)
          cleanup()
          clearTimeout(timeout)
          
          if (!isResolved) {
            isResolved = true
            reject(new Error('SSE连接失败'))
          }
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
  
  // 普通HTTP请求方法（如果需要的话）
  async loveAppChat(message, chatId) {
    try {
      const response = await api.post('/ai/love_app/chat', {
        message,
        chatId
      })
      return response.data
    } catch (error) {
      throw error
    }
  },
  
  async manusChat(message) {
    try {
      const response = await api.post('/ai/manus/chat', {
        message
      })
      return response.data
    } catch (error) {
      throw error
    }
  }
}

export default aiService

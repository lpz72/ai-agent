import axios from 'axios'

// 根据环境变量设置 API 基础 URL
const API_BASE_URL = process.env.NODE_ENV === 'production'
    ? 'agent-backend.project-learn.site:8123/api' // 生产环境使用相对路径，适用于前后端部署在同一域名下
    : 'http://localhost:8123/api' // 开发环境指向本地后端服务

const TIMEOUT = 30000

// 创建axios实例
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
  }
})

// 开发环境下的日志函数
export const log = (message, data) => {
  if (process.env.NODE_ENV === 'development') {
    console.log(`[${new Date().toISOString()}] ${message}`, data)
  }
}

// 生产环境下的错误日志
export const errorLog = (message, error) => {
  if (process.env.NODE_ENV === 'production') {
    console.error(`[${new Date().toISOString()}] ${message}`, error)
  }
  // 在生产环境中可以发送错误到监控服务
}

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    log('发送请求', {
      method: config.method?.toUpperCase(),
      url: config.url,
      data: config.data,
      params: config.params
    })
    return config
  },
  (error) => {
    errorLog('请求错误', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    log('收到响应', {
      status: response.status,
      url: response.config.url,
      data: response.data
    })
    return response
  },
  (error) => {
    errorLog('响应错误', error)
    
    // 处理不同类型的错误
    if (error.response) {
      // 服务器返回错误状态码
      errorLog('错误状态码', {
        status: error.response.status,
        data: error.response.data,
        headers: error.response.headers
      })
    } else if (error.request) {
      // 请求已发送但没有收到响应
      errorLog('网络错误', '没有收到响应')
    } else {
      // 请求配置错误
      errorLog('请求配置错误', error.message)
    }
    
    return Promise.reject(error)
  }
)

export default api

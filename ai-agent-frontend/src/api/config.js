import axios from 'axios'
import { API_BASE_URL, TIMEOUT, log, errorLog } from '../config/env.js'

// 创建axios实例
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
  }
})

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

// 环境配置
const env = {
  development: {
    API_BASE_URL: 'http://localhost:8123/api',
    TIMEOUT: 30000,
    DEBUG: true
  },
  production: {
    API_BASE_URL: 'http://agent-backend.project-learn.site/api',
    TIMEOUT: 30000,
    DEBUG: false
  }
}


// 获取当前环境
const currentEnv = import.meta.env.MODE || 'development'

// 导出当前环境配置
export const config = env[currentEnv]

// 导出环境变量
export const API_BASE_URL = config.API_BASE_URL
export const TIMEOUT = config.TIMEOUT
export const DEBUG = config.DEBUG

// 开发环境下的日志函数
export const log = (message, data) => {
  if (DEBUG) {
    console.log(`[${new Date().toISOString()}] ${message}`, data)
  }
}

// 生产环境下的错误日志
export const errorLog = (message, error) => {
  if (DEBUG) {
    console.error(`[${new Date().toISOString()}] ${message}`, error)
  }
  // 在生产环境中可以发送错误到监控服务
}


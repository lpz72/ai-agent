# AI智能助手前端项目

## 项目简介

这是一个基于Vue3开发的AI智能助手前端项目，包含两个主要功能模块：
- **AI减肥大师** 💪 - 专业的减肥塑形指导和建议
- **AI超级智能体** 🤖 - 多功能智能助手，支持技术问题、学习辅导、创意写作等

## 功能特性

### 🏠 首页功能
- 现代化卡片式布局设计
- 响应式设计，支持桌面端和移动端
- 两个功能模块的快速入口
- 美观的渐变背景和动画效果

### 💪 AI减肥大师
- 专业的减肥咨询和建议
- 实时聊天界面
- 个性化减肥计划制定
- 营养建议和运动指导
- 生活习惯改善方案

### 🤖 AI超级智能体
- 多步骤智能处理
- 实时SSE（Server-Sent Events）响应
- 智能内容格式化显示
- 支持工具执行结果展示
- 搜索结果优化显示
- 可点击的链接和描述

## 技术栈

- **前端框架**: Vue 3.3.4
- **路由管理**: Vue Router 4.2.4
- **HTTP客户端**: Axios 1.5.0
- **构建工具**: Vite 4.4.9
- **开发语言**: JavaScript/HTML/CSS
- **实时通信**: EventSource API (SSE)

## 项目结构

```
ai-agent-frontend/
├── public/
│   └── vite.svg
├── src/
│   ├── api/
│   │   └── aiService.js          # API服务封装
│   ├── router/
│   │   └── index.js              # 路由配置
│   ├── utils/
│   │   └── index.js              # 工具函数
│   ├── views/
│   │   ├── Home.vue              # 首页组件
│   │   ├── LoveApp.vue           # AI减肥大师组件
│   │   └── ManusApp.vue          # AI超级智能体组件
│   ├── App.vue                   # 根组件
│   ├── main.js                   # 应用入口
│   └── style.css                 # 全局样式
├── index.html                    # HTML模板
├── package.json                  # 项目配置
├── vite.config.js               # Vite配置
└── README.md                    # 项目说明
```

## 安装和运行

### 环境要求
- Node.js >= 16.0.0
- npm >= 8.0.0

### 安装依赖
```bash
npm install
```

### 开发环境运行
```bash
npm run dev
```
项目将在 `http://localhost:3000` 启动

### 生产环境构建
```bash
npm run build
```

### 预览生产构建
```bash
npm run preview
```

## 使用说明

### 1. 访问首页
打开浏览器访问 `http://localhost:3000`，您将看到两个功能卡片：
- **AI减肥大师** 💪 - 点击进入减肥咨询
- **AI超级智能体** 🤖 - 点击进入智能助手

### 2. AI减肥大师使用
- 点击"AI减肥大师"卡片进入聊天界面
- 输入您的减肥相关问题
- 系统会实时返回专业的减肥建议和指导

### 3. AI超级智能体使用
- 点击"AI超级智能体"卡片进入聊天界面
- 输入您的问题或需求
- 系统会分步骤处理并展示结果
- 支持多种内容格式的智能显示

## 核心功能详解

### 实时通信 (SSE)
项目使用Server-Sent Events实现实时通信：
```javascript
const eventSource = new EventSource(
  `http://localhost:8123/api/ai/manus/chat?message=${encodeURIComponent(message)}`
)
```

### 智能内容处理
AI超级智能体支持多步骤内容处理：
- 自动识别"step"格式的响应
- 将每个步骤分离为独立的聊天气泡
- 智能格式化JSON数据
- 优化搜索结果显示

### 响应式设计
- 桌面端：卡片横向排列
- 移动端：卡片垂直排列
- 自适应聊天界面布局

## 后端API配置

项目默认连接的后端API地址：
- 基础URL: `http://localhost:8123/api`
- AI减肥大师: `/ai/love/chat`
- AI超级智能体: `/ai/manus/chat`

如需修改API地址，请编辑相应组件中的URL配置。

## 开发说明

### 添加新功能
1. 在 `src/views/` 目录下创建新的Vue组件
2. 在 `src/router/index.js` 中添加路由配置
3. 在 `src/views/Home.vue` 中添加功能卡片

### 样式定制
- 全局样式：`src/style.css`
- 组件样式：各组件内的 `<style scoped>` 部分
- 主题色彩：可在组件中修改CSS变量

### 调试技巧
- 打开浏览器开发者工具查看控制台日志
- 使用Vue DevTools进行组件调试
- 检查Network面板查看API请求

## 常见问题

### Q: 页面无法滚动？
A: 检查CSS中的overflow设置，确保容器允许滚动。

### Q: SSE连接失败？
A: 确认后端服务是否正常运行在8123端口。

### Q: 内容显示异常？
A: 检查浏览器控制台是否有JavaScript错误。

## 更新日志

### v1.0.0 (2024-01-XX)
- 初始版本发布
- 实现AI减肥大师功能
- 实现AI超级智能体功能
- 支持实时SSE通信
- 响应式设计支持

## 贡献指南

欢迎提交Issue和Pull Request来改进项目！

## 许可证

MIT License

## 联系方式

如有问题或建议，请通过以下方式联系：
- 提交GitHub Issue
- 发送邮件至项目维护者

---

**注意**: 本项目需要配合后端API服务使用，请确保后端服务正常运行。

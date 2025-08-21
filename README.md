# AI Agent - 智能助手平台

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.6-brightgreen)
![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0--M6-blue)
![Vue.js](https://img.shields.io/badge/Vue.js-3.3.4-green)
![License](https://img.shields.io/badge/License-MIT-yellow)

一个基于Spring AI框架构建的智能助手平台，集成多种AI模型和工具，提供丰富的AI服务功能。

[功能特性](#功能特性) • [快速开始](#快速开始) • [项目架构](#项目架构) • [API文档](#api文档) • [部署指南](#部署指南)

</div>

## 📋 目录

- [项目简介](#项目简介)
- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [项目架构](#项目架构)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [API文档](#api文档)
- [前端应用](#前端应用)
- [MCP服务](#mcp服务)
- [部署指南](#部署指南)
- [开发指南](#开发指南)
- [贡献指南](#贡献指南)
- [许可证](#许可证)

## 🚀 项目简介

AI Agent是一个现代化的智能助手平台，基于Spring AI框架开发，集成了多种AI模型和工具服务。平台提供了两个主要的AI应用：

- **AI减肥大师** 💪 - 专业的减肥塑形指导和建议
- **AI超级智能体** 🤖 - 多功能智能助手，支持工具调用、RAG检索、MCP服务等

## ✨ 功能特性

### 🎯 核心功能
- **多模型支持**: 集成阿里云通义千问、Ollama本地模型
- **RAG检索增强**: 支持PGVector向量数据库、阿里云知识库
- **工具调用**: 内置多种实用工具（搜索、文件操作、PDF生成等）
- **MCP协议**: 支持Model Context Protocol，可扩展外部工具
- **流式响应**: 支持SSE流式输出，提供实时交互体验
- **多轮对话**: 基于文件的对话记忆，支持上下文理解

### 🛠️ 内置工具
- **网络搜索工具**: 基于百度搜索API
- **网页抓取工具**: 智能网页内容提取
- **文件操作工具**: 文件读写、管理功能
- **资源下载工具**: 支持图片、文档下载
- **终端操作工具**: 执行系统命令
- **PDF生成工具**: 动态PDF文档生成
- **图片搜索工具**: 基于Pexels API的图片搜索

### 🔧 高级特性
- **查询重写**: 智能查询优化和重写
- **文档增强**: AI驱动的文档元数据增强
- **自定义顾问**: 可配置的AI顾问系统
- **多种部署模式**: 支持本地、云端部署

## 🏗️ 技术栈

### 后端技术
- **框架**: Spring Boot 3.4.6
- **AI框架**: Spring AI 1.0.0-M6
- **Java版本**: Java 21
- **数据库**: PostgreSQL + PGVector
- **AI模型**:
  - 阿里云通义千问 (DashScope)
  - Ollama本地模型
- **工具库**: Hutool、Lombok
- **文档**: Knife4j (Swagger)

### 前端技术
- **框架**: Vue.js 3.3.4
- **路由**: Vue Router 4.2.4
- **HTTP客户端**: Axios 1.5.0
- **构建工具**: Vite 4.4.9
- **实时通信**: EventSource API (SSE)

### 外部服务
- **搜索API**: 百度搜索
- **图片API**: Pexels
- **向量数据库**: PGVector
- **知识库**: 阿里云知识库服务

## 🏛️ 项目架构

```
ai-agent/
├── src/main/java/org/lpz/aiagent/
│   ├── app/                    # AI应用层
│   │   └── LoveApp.java       # AI减肥大师应用
│   ├── agent/                 # 智能体
│   │   └── YuManus.java       # 超级智能体
│   ├── controller/            # 控制器层
│   │   └── AiController.java  # AI接口控制器
│   ├── tools/                 # 工具集合
│   │   ├── WebSearchTool.java
│   │   ├── FileOperationTool.java
│   │   ├── PDFGenerationTool.java
│   │   └── ...
│   ├── rag/                   # RAG相关
│   │   ├── QueryRewriter.java
│   │   └── LoveAppRagConfig.java
│   └── config/                # 配置类
├── ai-agent-frontend/         # Vue.js前端应用
│   ├── src/
│   │   ├── views/
│   │   │   ├── Home.vue       # 首页
│   │   │   ├── LoveApp.vue    # AI减肥大师
│   │   │   └── ManusApp.vue   # AI超级智能体
│   │   └── api/
│   └── package.json
├── yu-image-search-mcp-server/ # MCP图片搜索服务
│   └── src/main/java/
└── tmp/                       # 临时文件目录
    ├── chat-memory/           # 对话记忆
    ├── download/              # 下载文件
    ├── file/                  # 用户文件
    └── pdf/                   # PDF文件
```

## 🚀 快速开始

### 环境要求
- Java 21+
- Node.js 16+
- PostgreSQL 12+ (带PGVector扩展)
- Maven 3.6+

### 1. 克隆项目
```bash
git clone https://github.com/lpz72/ai-agent.git
cd ai-agent
```

### 2. 配置数据库
```sql
-- 创建数据库
CREATE DATABASE ai_agent;

-- 启用PGVector扩展
CREATE EXTENSION IF NOT EXISTS vector;
```

### 3. 配置应用
复制配置文件并修改相关配置：
```bash
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
```

编辑配置文件，设置以下关键配置：
```yaml
spring:
  ai:
    dashscope:
      api-key: your-dashscope-api-key
    ollama:
      base-url: http://localhost:11434
  datasource:
    url: jdbc:postgresql://localhost:5432/ai_agent
    username: your-username
    password: your-password

search-api:
  api-key: your-search-api-key
```

### 4. 启动后端服务
```bash
# 启动主应用
mvn spring-boot:run

# 启动MCP图片搜索服务
cd yu-image-search-mcp-server
mvn spring-boot:run
```

### 5. 启动前端应用
```bash
cd ai-agent-frontend
npm install
npm run dev
```

### 6. 访问应用
- 前端应用: http://localhost:3000
- 后端API: http://localhost:8123/api
- API文档: http://localhost:8123/api/doc.html
- MCP服务: http://localhost:8127

## ⚙️ 配置说明

### 主要配置文件
- `application.yml`: 主配置文件
- `application-local.yml`: 本地开发配置
- `mcp-servers.json`: MCP服务配置

### 关键配置项

#### AI模型配置
```yaml
spring:
  ai:
    # 阿里云通义千问配置
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}
      chat:
        model: qwen-plus

    # Ollama本地模型配置
    ollama:
      base-url: http://localhost:11434
      chat:
        model: gemma3:1b
```

#### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ai_agent
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

#### MCP客户端配置
```yaml
spring:
  ai:
    mcp:
      client:
        request-timeout: 120s
        timeout: 60000
        connect-timeout: 30000
        stdio:
          servers-configuration: classpath:mcp-servers.json
```

## 📚 API文档

### 主要接口

#### AI减肥大师
- `GET /api/ai/love_app/chat/sync` - 同步对话
- `GET /api/ai/love_app/chat/sse` - 流式对话
- `GET /api/ai/love_app/chat/sse_emitter` - SSE流式对话

#### AI超级智能体
- `GET /api/ai/manus/chat` - 智能体对话（支持工具调用）

### 请求参数
```bash
# 同步对话示例
curl -X GET "http://localhost:8123/api/ai/love_app/chat/sync?message=我想减肥&chatId=test123"

# 流式对话示例
curl -X GET "http://localhost:8123/api/ai/love_app/chat/sse?message=制定减肥计划&chatId=test123"
```

### 响应格式
```json
{
  "code": 0,
  "data": "AI助手的回复内容",
  "message": "success"
}
```

## 🎨 前端应用

### 功能模块

#### 首页 (Home.vue)
- 现代化卡片式布局
- 两个AI应用的快速入口
- 响应式设计

#### AI减肥大师 (LoveApp.vue)
- 实时聊天界面
- 专业减肥建议
- 个性化方案制定

#### AI超级智能体 (ManusApp.vue)
- 多步骤智能处理
- 工具执行结果展示
- 搜索结果优化显示

### 技术特点
- **Vue 3 Composition API**: 现代化开发体验
- **SSE实时通信**: 流式响应展示
- **响应式设计**: 支持桌面端和移动端
- **模块化架构**: 组件化开发

## 🔌 MCP服务

### yu-image-search-mcp-server
基于Model Context Protocol的图片搜索服务，提供以下功能：

#### 功能特性
- **图片搜索**: 基于Pexels API的高质量图片搜索
- **MCP协议**: 标准化的工具接口
- **多种部署模式**: 支持stdio和SSE模式

#### 配置说明
```yaml
spring:
  ai:
    mcp:
      server:
        name: yu-image-search-mcp-server
        version: 0.0.1
        type: SYNC
        stdio: true  # stdio模式
        # stdio: false  # SSE模式
```

#### 工具接口
- `searchImage(query)`: 根据关键词搜索图片

## 🚀 部署指南

### Docker部署
```bash
# 构建镜像
docker build -t ai-agent .

# 运行容器
docker run -d \
  -p 8123:8123 \
  -e DASHSCOPE_API_KEY=your-api-key \
  -e DB_URL=jdbc:postgresql://host:5432/ai_agent \
  ai-agent
```

### 生产环境配置
```yaml
spring:
  profiles:
    active: prod

server:
  port: 8123
  servlet:
    context-path: /api

logging:
  level:
    org.lpz.aiagent: INFO
  file:
    name: logs/ai-agent.log

## 🛠️ 开发指南

### 项目结构说明

#### 核心模块
- **app**: AI应用层，包含具体的AI应用实现
- **agent**: 智能体层，实现复杂的AI代理逻辑
- **controller**: 控制器层，提供REST API接口
- **tools**: 工具层，集成各种外部工具和服务
- **rag**: RAG相关组件，包括检索增强生成功能
- **config**: 配置层，管理应用配置

#### 开发规范
1. **代码风格**: 遵循阿里巴巴Java开发手册
2. **注释规范**: 使用JavaDoc格式
3. **测试覆盖**: 核心功能需要单元测试
4. **日志规范**: 使用SLF4J + Logback

### 添加新工具

#### 1. 创建工具类
```java
@Component
public class CustomTool {

    @Tool(description = "工具描述")
    public String customFunction(
        @ToolParam(description = "参数描述") String param) {
        // 工具实现逻辑
        return "结果";
    }
}
```

#### 2. 注册工具
在 `ToolRegistration.java` 中注册新工具：
```java
@Bean
public ToolCallback[] allTools() {
    CustomTool customTool = new CustomTool();
    return ToolCallbacks.from(
        // 其他工具...
        customTool
    );
}
```

### 扩展RAG功能

#### 1. 自定义文档检索器
```java
@Component
public class CustomDocumentRetriever implements DocumentRetriever {

    @Override
    public List<Document> retrieve(Query query) {
        // 自定义检索逻辑
        return documents;
    }
}
```

#### 2. 配置RAG顾问
```java
@Bean
public Advisor customRagAdvisor() {
    return RetrievalAugmentationAdvisor.builder()
        .documentRetriever(customDocumentRetriever)
        .build();
}
```

### 测试指南

#### 运行测试
```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=LoveAppTest

# 运行集成测试
mvn verify
```

#### 测试示例
```java
@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    @Test
    void testDoChat() {
        String result = loveApp.doChat("测试消息", "test-chat-id");
        Assertions.assertNotNull(result);
    }
}
```

## 🤝 贡献指南

### 贡献流程
1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 提交规范
使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：
```
feat: 添加新功能
fix: 修复bug
docs: 更新文档
style: 代码格式调整
refactor: 代码重构
test: 添加测试
chore: 构建过程或辅助工具的变动
```

### 代码审查
- 确保代码通过所有测试
- 遵循项目代码规范
- 添加必要的文档和注释
- 保持向后兼容性

## 📝 更新日志

### v0.0.1 (2025-01-XX)
- ✨ 初始版本发布
- 🚀 集成Spring AI框架
- 💪 实现AI减肥大师应用
- 🤖 实现AI超级智能体
- 🔧 集成多种AI工具
- 📚 支持RAG检索增强
- 🔌 支持MCP协议

## 🙏 致谢

感谢以下开源项目和服务：
- [Spring AI](https://spring.io/projects/spring-ai) - AI应用开发框架
- [Vue.js](https://vuejs.org/) - 前端框架
- [Hutool](https://hutool.cn/) - Java工具库
- [PGVector](https://github.com/pgvector/pgvector) - 向量数据库扩展
- [Pexels](https://www.pexels.com/) - 图片API服务

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系方式

- **项目地址**: [https://github.com/lpz72/ai-agent](https://github.com/lpz72/ai-agent)
- **问题反馈**: [Issues](https://github.com/lpz72/ai-agent/issues)
- **讨论交流**: [Discussions](https://github.com/lpz72/ai-agent/discussions)

---

<div align="center">

**如果这个项目对您有帮助，请给它一个 ⭐️**

Made with ❤️ by [lpz72](https://github.com/lpz72)

</div>
```
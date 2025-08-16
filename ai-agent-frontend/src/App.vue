<template>
  <div id="app">
    <router-view />
    
    <!-- 全局底部版权信息 - 只在首页显示 -->
    <footer v-if="showFooter" class="global-footer">
      <div class="footer-content">
        <div class="footer-section">
          <h4 class="footer-title">AI智能助手</h4>
          <p class="footer-desc">专业的AI服务提供商，致力于为用户提供最先进的智能解决方案</p>
        </div>
        
        <div class="footer-section">
          <h4 class="footer-title">友情链接</h4>
          <div class="footer-links">
            <a href="https://github.com/lpz72/ai-agent" target="_blank" class="footer-link">Github</a>
            <a href="http://friends.project-learn.site/" class="footer-link">编程伙伴匹配站</a>
            <a href="http://ai.project-learn.site/" class="footer-link">百度智识通</a>
          </div>
        </div>
        
        <div class="footer-section">
          <h4 class="footer-title">联系我们</h4>
          <div class="footer-contact">
            <p>商务合作</p>
            <p>技术支持</p>
            <p>意见反馈</p>
          </div>
        </div>
        
        <div class="footer-section">
          <h4 class="footer-title">关注我们</h4>
          <div class="footer-social">
            <span class="social-icon">📧</span>
            <span class="social-icon">💬</span>
            <span class="social-icon">📱</span>
          </div>
        </div>
      </div>
      
      <div class="footer-bottom">
        <div class="footer-copyright">
          <p>&copy; 2025 AI智能助手. All rights reserved.</p>
          <p>沪ICP备XXXXXXXX号-1 | 增值电信业务经营许可证</p>
        </div>
        <div class="footer-legal">
          <a href="#" class="legal-link">用户协议</a>
          <a href="#" class="legal-link">隐私政策</a>
          <a href="#" class="legal-link">关于我们</a>
        </div>
      </div>
    </footer>
  </div>
</template>

<script>
export default {
  name: 'App',
  computed: {
    showFooter() {
      // 只在首页显示底部栏
      return this.$route.path === '/'
    }
  }
}
</script>

<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
}

/* 全局底部样式 */
.global-footer {
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a2e 100%);
  color: #ffffff;
  padding: 3rem 0 1rem;
  border-top: 2px solid rgba(0, 255, 255, 0.3);
  position: relative;
  overflow: hidden;
  width: 100%;
  z-index: 10;
}

.global-footer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, #00ffff, transparent);
  animation: scan 3s linear infinite;
}

@keyframes scan {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  margin-bottom: 2rem;
}

.footer-section {
  text-align: center;
}

.footer-title {
  color: #00ffff;
  font-size: 1.2rem;
  font-weight: 600;
  margin-bottom: 1rem;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  text-shadow: 0 0 10px rgba(0, 255, 255, 0.5);
}

.footer-desc {
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.6;
  font-size: 0.9rem;
}

.footer-links {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.footer-link {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  transition: all 0.3s ease;
  padding: 0.3rem 0;
}

.footer-link:hover {
  color: #00ffff;
  text-shadow: 0 0 8px rgba(0, 255, 255, 0.6);
  transform: translateX(5px);
}

.footer-contact p {
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
  transition: color 0.3s ease;
}

.footer-contact p:hover {
  color: #00ffff;
}

.footer-social {
  display: flex;
  justify-content: center;
  gap: 1rem;
}

.social-icon {
  font-size: 1.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  filter: drop-shadow(0 0 5px rgba(0, 255, 255, 0.3));
}

.social-icon:hover {
  transform: scale(1.2);
  filter: drop-shadow(0 0 10px rgba(0, 255, 255, 0.8));
}

.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding-top: 1.5rem;
  max-width: 1200px;
  margin: 0 auto;
  padding-left: 2rem;
  padding-right: 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
}

.footer-copyright {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.85rem;
}

.footer-copyright p {
  margin-bottom: 0.3rem;
}

.footer-legal {
  display: flex;
  gap: 1.5rem;
}

.legal-link {
  color: rgba(255, 255, 255, 0.6);
  text-decoration: none;
  font-size: 0.85rem;
  transition: all 0.3s ease;
  position: relative;
}

.legal-link::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 1px;
  background: #00ffff;
  transition: width 0.3s ease;
}

.legal-link:hover {
  color: #00ffff;
}

.legal-link:hover::after {
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .footer-content {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1.5rem;
    padding: 0 1rem;
  }
  
  .footer-bottom {
    flex-direction: column;
    text-align: center;
    gap: 1rem;
    padding-left: 1rem;
    padding-right: 1rem;
  }
  
  .footer-legal {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .global-footer {
    padding: 2rem 0 1rem;
  }
  
  .footer-content {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .footer-section {
    text-align: center;
  }
}
</style>

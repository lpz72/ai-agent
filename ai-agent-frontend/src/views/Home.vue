<template>
  <div class="home">
    <!-- 粒子背景 -->
    <div class="particles">
      <div class="particle" v-for="n in 50" :key="n" :style="getParticleStyle(n)"></div>
    </div>
    
    <!-- 主内容 -->
    <div class="container">
      <!-- 标题区域 -->
      <div class="title-section">
        <div class="title-wrapper">
          <h1 class="title">AI智能助手</h1>
        </div>
        <p class="subtitle">选择您需要的AI服务</p>
        <div class="typing-indicator">
          <span></span>
          <span></span>
          <span></span>
        </div>
      </div>
      
      <!-- 应用卡片网格 -->
      <div class="app-grid">
        <div class="app-card" @click="goToLoveApp">
          <div class="card-glow"></div>
          <div class="app-icon">💕</div>
          <h3>AI恋爱大师</h3>
          <p>专业的恋爱咨询和建议</p>
          <div class="card-overlay">
            <span class="enter-text">进入应用 →</span>
          </div>
        </div>
        
        <div class="app-card" @click="goToManusApp">
          <div class="card-glow"></div>
          <div class="app-icon">🤖</div>
          <h3>AI超级智能体</h3>
          <p>全能AI助手，解决各种问题</p>
          <div class="card-overlay">
            <span class="enter-text">进入应用 →</span>
          </div>
        </div>
      </div>
      
      <!-- 技术标签 -->
      <div class="tech-tags">
        <span class="tag">Vue.js</span>
        <span class="tag">AI</span>
        <span class="tag">Machine Learning</span>
        <span class="tag">Neural Networks</span>
        <span class="tag">Deep Learning</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Home',
  methods: {
    goToLoveApp() {
      this.$router.push('/love-app')
    },
    goToManusApp() {
      this.$router.push('/manus-app')
    },
    getParticleStyle(n) {
      const delay = Math.random() * 20;
      const duration = 15 + Math.random() * 10;
      const size = 2 + Math.random() * 3;
      return {
        '--delay': `${delay}s`,
        '--duration': `${duration}s`,
        '--size': `${size}px`,
        left: `${Math.random() * 100}%`,
        animationDelay: `${delay}s`,
        animationDuration: `${duration}s`
      }
    }
  }
}
</script>

<style scoped>
.home {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a2e 25%, #16213e 50%, #0f3460 75%, #533483 100%);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  position: relative;
  font-family: 'Courier New', 'Monaco', 'Menlo', monospace;
  padding: 2rem 0 6rem 0;
}

/* 粒子背景 */
.particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 1;
}

.particle {
  position: absolute;
  width: var(--size);
  height: var(--size);
  background: linear-gradient(45deg, #00ffff, #ff00ff, #ffff00);
  border-radius: 50%;
  opacity: 0.6;
  animation: float var(--duration) linear infinite;
  z-index: 1;
}

@keyframes float {
  0% {
    transform: translateY(100vh) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 0.6;
  }
  90% {
    opacity: 0.6;
  }
  100% {
    transform: translateY(-100px) rotate(360deg);
    opacity: 0;
  }
}

.container {
  text-align: center;
  color: white;
  max-width: 1000px;
  padding: 2rem;
  position: relative;
  z-index: 2;
  margin: 2rem auto 4rem auto;
}

/* 标题区域 */
.title-section {
  margin-bottom: 4rem;
}

.title-wrapper {
  position: relative;
  margin-bottom: 1rem;
}

.title {
  font-size: 4rem;
  font-weight: 900;
  text-transform: uppercase;
  letter-spacing: 0.2em;
  margin-bottom: 1rem;
  position: relative;
  color: #ffffff;
  text-shadow: 
    3px 3px 0px #000000,
    6px 6px 0px #000000,
    0 0 10px rgba(0, 255, 255, 0.8),
    0 0 20px rgba(0, 255, 255, 0.6);
  animation: titlePulse 4s ease-in-out infinite;
  background: linear-gradient(45deg, #ffffff, #00ffff, #ffffff);
  background-size: 200% 200%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  filter: drop-shadow(0 0 5px rgba(0, 0, 0, 0.8));
}

@keyframes titlePulse {
  0%, 100% {
    background-position: 0% 50%;
    text-shadow: 
      3px 3px 0px #000000,
      6px 6px 0px #000000,
      0 0 10px rgba(0, 255, 255, 0.8),
      0 0 20px rgba(0, 255, 255, 0.6);
  }
  50% {
    background-position: 100% 50%;
    text-shadow: 
      3px 3px 0px #000000,
      6px 6px 0px #000000,
      0 0 15px rgba(0, 255, 255, 1),
      0 0 25px rgba(0, 255, 255, 0.8);
  }
}



.subtitle {
  font-size: 1.4rem;
  margin-bottom: 2rem;
  opacity: 0.8;
  font-weight: 300;
  letter-spacing: 0.1em;
}

/* 打字指示器 */
.typing-indicator {
  display: flex;
  justify-content: center;
  gap: 4px;
  margin-top: 1rem;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #00ffff;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 应用卡片网格 */
.app-grid {
  display: flex;
  justify-content: center;
  gap: 3rem;
  margin: 3rem 0;
  flex-wrap: wrap;
}

.app-card {
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 2.5rem;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  border: 2px solid rgba(0, 255, 255, 0.3);
  width: 320px;
  position: relative;
  overflow: hidden;
  box-shadow: 
    0 0 20px rgba(0, 255, 255, 0.2),
    inset 0 0 20px rgba(0, 255, 255, 0.1);
}

.app-card:hover {
  transform: translateY(-15px) scale(1.05);
  border-color: rgba(0, 255, 255, 0.8);
  box-shadow: 
    0 20px 40px rgba(0, 255, 255, 0.3),
    inset 0 0 30px rgba(0, 255, 255, 0.2);
}

.card-glow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 50% 50%, rgba(0, 255, 255, 0.1) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.app-card:hover .card-glow {
  opacity: 1;
}

.app-icon {
  font-size: 4.5rem;
  margin-bottom: 1.5rem;
  filter: drop-shadow(0 0 10px rgba(0, 255, 255, 0.5));
}

.app-card h3 {
  font-size: 1.8rem;
  margin-bottom: 1rem;
  font-weight: 700;
  color: #00ffff;
  text-shadow: 0 0 10px rgba(0, 255, 255, 0.5);
}

.app-card p {
  opacity: 0.8;
  line-height: 1.6;
  font-size: 1rem;
}

.card-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 20px;
}

.app-card:hover .card-overlay {
  opacity: 1;
}

.enter-text {
  color: #00ffff;
  font-weight: 600;
  font-size: 1.1rem;
  text-shadow: 0 0 10px rgba(0, 255, 255, 0.8);
}

/* 技术标签 */
.tech-tags {
  display: flex;
  justify-content: center;
  gap: 1rem;
  flex-wrap: wrap;
  margin-top: 3rem;
  margin-bottom: 2rem;
}

.tag {
  background: rgba(0, 255, 255, 0.1);
  border: 1px solid rgba(0, 255, 255, 0.3);
  color: #00ffff;
  padding: 0.5rem 1rem;
  border-radius: 25px;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.3s ease;
  cursor: default;
}

.tag:hover {
  background: rgba(0, 255, 255, 0.2);
  border-color: rgba(0, 255, 255, 0.6);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 255, 255, 0.3);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .title {
    font-size: 2.5rem;
    letter-spacing: 0.1em;
    text-shadow: 
      2px 2px 0px #000000,
      4px 4px 0px #000000,
      0 0 8px rgba(0, 255, 255, 0.8),
      0 0 15px rgba(0, 255, 255, 0.6);
  }
  
  .subtitle {
    font-size: 1.1rem;
  }
  
  .app-grid {
    flex-direction: column;
    align-items: center;
    gap: 2rem;
  }
  
  .app-card {
    width: 100%;
    max-width: 350px;
    padding: 2rem;
  }
  
  .tech-tags {
    gap: 0.8rem;
  }
  
  .tag {
    font-size: 0.8rem;
    padding: 0.4rem 0.8rem;
  }
}

@media (max-width: 480px) {
  .title {
    font-size: 2rem;
    text-shadow: 
      1px 1px 0px #000000,
      2px 2px 0px #000000,
      0 0 6px rgba(0, 255, 255, 0.8),
      0 0 12px rgba(0, 255, 255, 0.6);
  }
  
  .container {
    padding: 1rem;
  }
  
  .app-card {
    padding: 1.5rem;
  }
}
</style>

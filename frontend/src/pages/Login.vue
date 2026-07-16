<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'

const router = useRouter()
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: '123456',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', type: 'error' }],
  password: [{ required: true, message: '请输入密码', type: 'error' }],
}

function onSubmit({ validateResult }: { validateResult: boolean }) {
  if (validateResult) {
    loading.value = true
    setTimeout(() => {
      localStorage.setItem('gov_token', 'mock-token-' + Date.now())
      MessagePlugin.success('登录成功')
      router.push('/dashboard')
      loading.value = false
    }, 600)
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-circle c1"></div>
      <div class="bg-circle c2"></div>
      <div class="bg-circle c3"></div>
    </div>
    <div class="login-card">
      <div class="login-left">
        <div class="brand">
          <t-icon name="data-base" size="40" style="color: #fff" />
          <h1>数据治理平台</h1>
        </div>
        <p class="slogan">看得清 · 管得住 · 用得好</p>
        <p class="desc">企业级一体化数据治理解决方案，覆盖元数据、标准、质量、安全、资产、生命周期与协同治理全链路。</p>
        <div class="features">
          <div class="feature-item"><t-icon name="server" /> 元数据血缘</div>
          <div class="feature-item"><t-icon name="check-circle" /> 质量监控</div>
          <div class="feature-item"><t-icon name="lock-on" /> 安全脱敏</div>
          <div class="feature-item"><t-icon name="folder" /> 资产编目</div>
        </div>
      </div>
      <div class="login-right">
        <h2>欢迎登录</h2>
        <t-form
          ref="formRef"
          :data="form"
          :rules="rules"
          label-align="top"
          @submit="onSubmit"
        >
          <t-form-item name="username" label="用户名">
            <t-input v-model="form.username" placeholder="请输入用户名" clearable>
              <template #prefix-icon><t-icon name="user" /></template>
            </t-input>
          </t-form-item>
          <t-form-item name="password" label="密码">
            <t-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              clearable
            >
              <template #prefix-icon><t-icon name="lock-on" /></template>
            </t-input>
          </t-form-item>
          <t-button theme="primary" type="submit" block size="large" :loading="loading">
            登 录
          </t-button>
        </t-form>
        <p class="tip">演示账号: admin / 123456</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #001b4e 0%, #002a6e 50%, #003d8c 100%);
  position: relative;
  overflow: hidden;
}
.login-bg {
  position: absolute;
  inset: 0;
}
.bg-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.15;
  filter: blur(40px);
}
.c1 {
  width: 500px;
  height: 500px;
  background: #00a8e5;
  top: -150px;
  right: -100px;
}
.c2 {
  width: 400px;
  height: 400px;
  background: #266fe8;
  bottom: -120px;
  left: -80px;
}
.c3 {
  width: 300px;
  height: 300px;
  background: #0052d9;
  top: 40%;
  left: 45%;
}
.login-card {
  position: relative;
  z-index: 1;
  width: 880px;
  height: 480px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}
.login-left {
  flex: 1;
  background: linear-gradient(160deg, #002a6e 0%, #0052d9 100%);
  padding: 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #fff;
}
.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}
.brand h1 {
  font-size: 24px;
  margin: 0;
  font-weight: 600;
}
.slogan {
  font-size: 20px;
  margin: 24px 0 16px;
  font-weight: 500;
  letter-spacing: 2px;
}
.desc {
  font-size: 13px;
  line-height: 1.7;
  opacity: 0.85;
  margin: 0 0 32px;
}
.features {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.feature-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 20px;
  backdrop-filter: blur(4px);
}
.login-right {
  width: 380px;
  padding: 48px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.login-right h2 {
  font-size: 22px;
  margin: 0 0 28px;
  color: var(--text-primary);
}
.tip {
  text-align: center;
  color: var(--text-secondary);
  font-size: 12px;
  margin-top: 20px;
}
</style>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-head">
        <span class="seal">观</span>
        <h1>观学阁</h1>
        <p>用心陪伴孩子的每一步成长</p>
      </div>

      <el-tabs v-model="mode" class="auth-tabs" stretch>
        <el-tab-pane label="登录" name="login" />
        <el-tab-pane label="注册" name="register" />
      </el-tabs>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="onSubmit"
      >
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item v-if="mode === 'register'" label="家长昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="如：林妈妈" />
        </el-form-item>

        <el-button
          type="primary"
          class="auth-submit"
          :loading="loading"
          native-type="submit"
        >
          {{ mode === 'login' ? '登 录' : '注 册 并 登 录' }}
        </el-button>
      </el-form>

      <p v-if="error" class="auth-tip">{{ error }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login as apiLogin, register as apiRegister } from '@/api/auth'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const mode = ref('login')
const loading = ref(false)
const error = ref('')
const formRef = ref()

const form = reactive({ phone: '', password: '', nickname: '' })

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

async function onSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  error.value = ''
  try {
    if (mode.value === 'register') {
      await apiRegister({
        phone: form.phone,
        password: form.password,
        nickname: form.nickname
      })
    }
    // 注册或登录后统一走登录拿 token
    const data = await apiLogin(form.phone, form.password)
    userStore.setAuth(data.token, data.user)
    ElMessage.success(mode.value === 'register' ? '注册成功，已自动登录' : '登录成功')
    router.replace(route.query.redirect || '/app/plan')
  } catch (e) {
    error.value = e.response?.data?.message || e.message || '操作失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: var(--gx-paper);
  padding: 24px;
}
.auth-card {
  width: 380px;
  background: var(--gx-warm-white);
  border: 1px solid var(--gx-divider);
  border-radius: 16px;
  padding: 32px 28px;
  box-shadow: 0 18px 50px rgba(43, 38, 32, 0.1);
}
.auth-head {
  text-align: center;
  margin-bottom: 18px;
}
.seal {
  display: inline-grid;
  place-items: center;
  width: 48px;
  height: 48px;
  border-radius: 10px;
  background: var(--gx-vermillion);
  color: #fff;
  font-family: var(--gx-font-serif);
  font-size: 26px;
  font-weight: 700;
  margin-bottom: 12px;
}
.auth-head h1 {
  font-family: var(--gx-font-serif);
  font-size: 26px;
  margin: 0 0 6px;
}
.auth-head p {
  color: var(--gx-sub);
  font-size: 13px;
  margin: 0;
}
.auth-tabs {
  margin-bottom: 8px;
}
.auth-submit {
  width: 100%;
  margin-top: 8px;
  font-family: var(--gx-font-serif);
  letter-spacing: 2px;
}
.auth-submit.el-button--primary {
  --el-button-bg-color: var(--gx-vermillion);
  --el-button-border-color: var(--gx-vermillion);
  --el-button-hover-bg-color: #a3332a;
  --el-button-hover-border-color: #a3332a;
  --el-button-active-bg-color: #8f2c24;
  --el-button-active-border-color: #8f2c24;
}
.auth-tip {
  color: var(--gx-vermillion);
  font-size: 13px;
  text-align: center;
  margin: 12px 0 0;
}
</style>

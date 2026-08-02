<template>
  <div class="layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="brand">
        <span class="seal">观</span>
        <span class="brand-name">观学阁</span>
      </div>
      <nav class="nav">
        <router-link to="/" class="nav-item">
          <span class="nav-ico">⌂</span> 首页
        </router-link>
        <router-link to="/app/plan" class="nav-item" active-class="active">
          <span class="nav-ico">✎</span> 学习计划
        </router-link>
        <router-link to="/app/grade" class="nav-item" active-class="active">
          <span class="nav-ico">✓</span> 成绩管理
        </router-link>
        <router-link to="/app/community" class="nav-item" active-class="active">
          <span class="nav-ico">❝</span> 育儿社区
        </router-link>
        <router-link to="/app/children" class="nav-item" active-class="active">
          <span class="nav-ico">✦</span> 孩子管理
        </router-link>
      </nav>
      <div class="sidebar-foot">用心陪伴<br />每一个家庭</div>
    </aside>

    <!-- 主区 -->
    <div class="main">
      <header class="topbar">
        <div class="topbar-title">{{ title }}</div>
        <div class="topbar-right">
          <el-select v-model="currentChild" placeholder="选择孩子" size="default" class="child-select">
            <el-option v-for="c in children" :key="c.id" :label="`${c.name}（${c.grade}）`" :value="c.id" />
          </el-select>
          <el-avatar :size="32" class="user-avatar">{{ userInitial }}</el-avatar>
          <span class="user-name">{{ user?.nickname || '家长' }}</span>
          <el-button text size="small" class="logout-btn" @click="onLogout">退出</el-button>
        </div>
      </header>
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, provide } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getChildren } from '@/api/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const user = userStore.user

const titleMap = {
  plan: '学习计划管理',
  grade: '成绩管理',
  community: '育儿社区',
  children: '孩子管理'
}
const title = computed(() => titleMap[route.name] || '观学阁')

const children = ref([])
const currentChild = ref(null)
const userInitial = computed(() => (user?.nickname || '家').charAt(0))

// 将「孩子列表」与「当前选中孩子」下发到各业务页面
provide('children', children)
provide('currentChild', currentChild)

function onLogout() {
  userStore.logout()
  router.replace('/login')
}

onMounted(async () => {
  try {
    children.value = await getChildren()
    if (children.value.length) currentChild.value = children.value[0].id
  } catch (e) {
    /* 孩子加载失败时保持空列表 */
  }
})
</script>

<style scoped>
.layout { display: flex; min-height: 100vh; }
.sidebar {
  width: 220px; background: var(--gx-warm-white); border-right: 1px solid var(--gx-divider);
  display: flex; flex-direction: column; padding: 24px 16px; position: sticky; top: 0; height: 100vh;
}
.brand { display: flex; align-items: center; gap: 10px; margin-bottom: 28px; }
.seal {
  width: 38px; height: 38px; border-radius: 8px; background: var(--gx-vermillion); color: #fff;
  display: grid; place-items: center; font-family: var(--gx-font-serif); font-size: 22px; font-weight: 700;
}
.brand-name { font-family: var(--gx-font-serif); font-size: 20px; font-weight: 700; color: var(--gx-ink); }
.nav { display: flex; flex-direction: column; gap: 6px; }
.nav-item {
  padding: 11px 14px; border-radius: 8px; color: var(--gx-sub); font-size: 15px; transition: .2s;
}
.nav-item:hover { background: var(--gx-muted); color: var(--gx-ink); }
.nav-item.active { background: var(--gx-vermillion); color: #fff; }
.nav-ico { margin-right: 8px; }
.sidebar-foot { margin-top: auto; color: var(--gx-ochre); font-family: var(--gx-font-serif); font-size: 13px; line-height: 1.7; opacity: .8; }
.main { flex: 1; display: flex; flex-direction: column; }
.topbar {
  height: 64px; background: var(--gx-warm-white); border-bottom: 1px solid var(--gx-divider);
  display: flex; align-items: center; justify-content: space-between; padding: 0 28px; position: sticky; top: 0; z-index: 10;
}
.topbar-title { font-family: var(--gx-font-serif); font-size: 20px; font-weight: 700; }
.topbar-right { display: flex; align-items: center; gap: 12px; }
.child-select { width: 160px; }
.user-avatar { background: var(--gx-celadon); color: #fff; font-weight: 700; }
.user-name { font-size: 14px; color: var(--gx-sub); }
.logout-btn { color: var(--gx-ochre); margin-left: 4px; }
.logout-btn:hover { color: var(--gx-vermillion); }
.content { padding: 28px; }
</style>

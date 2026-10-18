<template>
  <div class="layout">
    <!-- 侧边栏（桌面 ≥1024 完整；平板 768–1023 收窄为图标栏；移动端隐藏） -->
    <aside class="sidebar">
      <div class="brand">
        <span class="seal">观</span>
        <span class="brand-name">观学阁</span>
      </div>
      <nav class="nav">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :active-class="item.activeClass"
          :exact-active-class="item.exactActiveClass"
        >
          <span class="nav-ico">{{ item.icon }}</span>
          <span class="nav-label">{{ item.label }}</span>
        </router-link>
      </nav>
      <div class="sidebar-foot">用心陪伴<br />每一个家庭</div>
    </aside>

    <!-- 主区 -->
    <div class="main">
      <header class="topbar">
        <button class="hamburger" @click="drawerOpen = true" aria-label="打开导航">☰</button>
        <div class="topbar-title">{{ title }}</div>
        <div class="topbar-right">
          <el-select v-model="currentChild" placeholder="选择孩子" size="default" class="child-select">
            <el-option v-for="c in children" :key="c.id" :label="`${c.name}（${c.grade}）`" :value="c.id" />
          </el-select>
          <!-- 点击头像 / 昵称查看并编辑个人资料 -->
          <el-avatar :size="32" class="user-avatar clickable" @click="openMyProfile">{{ userInitial }}</el-avatar>
          <span class="user-name clickable" @click="openMyProfile">{{ displayName || '家长' }}</span>
          <el-button text size="small" class="logout-btn" @click="onLogout">退出</el-button>
        </div>
      </header>
      <main class="content">
        <router-view />
      </main>
      <footer class="app-foot">
        <router-link to="/privacy">隐私政策</router-link>
        <span class="dot">·</span>
        <router-link to="/child-protection">儿童个人信息保护规则</router-link>
      </footer>
    </div>

    <!-- 移动端导航抽屉 -->
    <el-drawer
      v-model="drawerOpen"
      direction="ltr"
      size="264px"
      :with-header="false"
      class="nav-drawer"
    >
      <div class="drawer-body">
        <div class="drawer-brand">
          <span class="seal">观</span>
          <span class="drawer-brand-name">观学阁</span>
        </div>
        <nav class="drawer-nav">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="drawer-item"
            :active-class="item.activeClass"
            :exact-active-class="item.exactActiveClass"
            @click="drawerOpen = false"
          >
            <span class="nav-ico">{{ item.icon }}</span>
            <span class="nav-label">{{ item.label }}</span>
          </router-link>
        </nav>
        <div class="drawer-foot">
          <button class="drawer-logout" @click="onLogout">退出登录</button>
        </div>
      </div>
    </el-drawer>

    <!-- 移动端底部 TabBar -->
    <nav class="mobile-tabbar">
      <template v-for="tab in tabItems" :key="tab.label">
        <router-link
          v-if="tab.path"
          :to="tab.path"
          class="tab-item"
          :class="{ active: isTabActive(tab.path) }"
        >
          <span class="tab-ico">{{ tab.icon }}</span>
          <span class="tab-label">{{ tab.label }}</span>
        </router-link>
        <button
          v-else
          class="tab-item"
          :class="{ active: userDrawerOpen }"
          @click="userDrawerOpen = true"
        >
          <span class="tab-ico">{{ tab.icon }}</span>
          <span class="tab-label">{{ tab.label }}</span>
        </button>
      </template>
    </nav>

    <!-- 移动端「我的」底部面板 -->
    <el-drawer
      v-model="userDrawerOpen"
      direction="btt"
      size="40%"
      :with-header="false"
      class="user-drawer"
    >
      <div class="user-panel">
        <el-avatar :size="56" class="user-avatar big">{{ userInitial }}</el-avatar>
        <div class="user-meta">
          <div class="user-nick">{{ displayName || '家长' }}</div>
          <div class="user-phone">{{ user?.phone || '' }}</div>
        </div>
        <button class="panel-edit" @click="openMyProfile">编辑资料</button>
        <button class="panel-logout" @click="onLogout">退出登录</button>
      </div>
    </el-drawer>

    <!-- 个人资料：查看与编辑（社区昵称 / 微信 / 邮箱 / 签名） -->
    <UserProfileDialog v-model="profileVisible" :user-id="null" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, provide } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getChildren } from '@/api/auth'
import UserProfileDialog from '@/components/UserProfileDialog.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const user = userStore.user

const titleMap = {
  plan: '学习计划管理',
  contract: '家庭契约',
  grade: '成绩管理',
  community: '育儿社区',
  voice: '心声社区',
  children: '孩子管理'
}
const title = computed(() => titleMap[route.name] || '观学阁')

const children = ref([])
const currentChild = ref(null)
// 展示名：社区昵称优先，与社区内的展示保持一致
const displayName = computed(
  () => (user && (user.communityNickname || user.nickname)) || ''
)
const userInitial = computed(() => (displayName.value || '家').charAt(0))

const profileVisible = ref(false)
function openMyProfile() {
  userDrawerOpen.value = false
  profileVisible.value = true
}

// 将「孩子列表」与「当前选中孩子」下发到各业务页面
provide('children', children)
provide('currentChild', currentChild)

// 导航项：侧边栏与抽屉共用
const navItems = [
  { path: '/', icon: '⌂', label: '首页', activeClass: undefined, exactActiveClass: 'active' },
  { path: '/app/children', icon: '✦', label: '孩子管理', activeClass: 'active' },
  { path: '/app/plan', icon: '✎', label: '学习计划', activeClass: 'active' },
  { path: '/app/contract', icon: '✍', label: '家庭契约', activeClass: 'active' },
  { path: '/app/grade', icon: '✓', label: '成绩管理', activeClass: 'active' },
  { path: '/app/community', icon: '❝', label: '育儿社区', activeClass: 'active' },
  { path: '/app/voice', icon: '♡', label: '心声社区', activeClass: 'active' }
]

// 移动端底部 TabBar：「我的」打开底部面板
const tabItems = [
  { path: '/', icon: '⌂', label: '首页' },
  { path: '/app/plan', icon: '✎', label: '计划' },
  { path: '/app/community', icon: '❝', label: '育儿' },
  { path: '/app/voice', icon: '♡', label: '心声' },
  { path: null, icon: '我', label: '我的' }
]

function isTabActive(path) {
  if (path === '/') return route.path === '/'
  return route.path === path || route.path.startsWith(path + '/')
}

const drawerOpen = ref(false)
const userDrawerOpen = ref(false)

function onLogout() {
  drawerOpen.value = false
  userDrawerOpen.value = false
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
.main { flex: 1; display: flex; flex-direction: column; min-width: 0; }
.topbar {
  height: 64px; background: var(--gx-warm-white); border-bottom: 1px solid var(--gx-divider);
  display: flex; align-items: center; justify-content: space-between; padding: 0 28px; position: sticky; top: 0; z-index: 10;
}
.topbar-title { font-family: var(--gx-font-serif); font-size: 20px; font-weight: 700; }
.topbar-right { display: flex; align-items: center; gap: 12px; }
.child-select { width: 160px; }
.user-avatar { background: var(--gx-celadon); color: #fff; font-weight: 700; }
.user-avatar.big { background: var(--gx-celadon); }
.user-avatar.clickable, .user-name.clickable { cursor: pointer; }
.user-name { font-size: 14px; color: var(--gx-sub); }
.user-name.clickable:hover { color: var(--gx-vermillion); }
.logout-btn { color: var(--gx-ochre); margin-left: 4px; }
.logout-btn:hover { color: var(--gx-vermillion); }
.content { padding: 28px; }
.app-foot { padding: 18px 28px 26px; text-align: center; color: var(--gx-sub); font-size: 12.5px; border-top: 1px solid var(--gx-divider); }
.app-foot a { color: var(--gx-sub); text-decoration: none; }
.app-foot a:hover { color: var(--gx-vermillion); }
.app-foot .dot { margin: 0 8px; opacity: .6; }

/* 汉堡按钮：桌面隐藏，移动端显示 */
.hamburger {
  display: none;
  width: 34px; height: 34px; border: 1px solid var(--gx-divider); border-radius: 8px;
  background: var(--gx-warm-white); color: var(--gx-ink); font-size: 16px; line-height: 1;
  cursor: pointer; flex-shrink: 0;
}

/* 移动端底部 TabBar：桌面隐藏 */
.mobile-tabbar { display: none; }

/* ===== 平板 768–1023px：侧边栏收窄为图标栏 ===== */
@media (max-width: 1023px) and (min-width: 768px) {
  .sidebar { width: 72px; padding: 24px 10px; }
  .brand { justify-content: center; gap: 0; }
  .brand-name, .nav-label, .sidebar-foot { display: none; }
  .nav-item { padding: 12px 0; text-align: center; }
  .nav-ico { margin-right: 0; }
}

/* ===== 移动端 <768px：抽屉导航 + 底部 TabBar ===== */
@media (max-width: 767px) {
  .sidebar { display: none; }
  .topbar { padding: 0 12px; height: 56px; }
  .hamburger { display: flex; align-items: center; justify-content: center; margin-right: 8px; }
  .topbar-title { font-size: 17px; }
  .child-select { width: 118px; }
  .user-name, .logout-btn { display: none; }
  .content { padding: 16px 16px 92px; }
  .app-foot { padding: 16px; }
  .mobile-tabbar {
    display: flex;
    position: fixed; left: 0; right: 0; bottom: 0; z-index: 20;
    height: 56px; padding-bottom: env(safe-area-inset-bottom);
    background: var(--gx-warm-white); border-top: 1px solid var(--gx-divider);
  }
  .tab-item {
    flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center;
    gap: 2px; background: none; border: none; padding: 0; cursor: pointer;
    color: var(--gx-sub); font-size: 10px;
  }
  .tab-item .tab-ico { font-size: 18px; line-height: 1; }
  .tab-item.active { color: var(--gx-vermillion); }
}
</style>

<!-- 抽屉内容被 teleport 到 body，需全局样式 -->
<style>
.nav-drawer .drawer-body { padding: 24px 16px; display: flex; flex-direction: column; height: 100%; box-sizing: border-box; }
.nav-drawer .drawer-brand { display: flex; align-items: center; gap: 10px; margin-bottom: 24px; }
.nav-drawer .drawer-brand .seal {
  width: 38px; height: 38px; border-radius: 8px; background: var(--gx-vermillion); color: #fff;
  display: grid; place-items: center; font-family: var(--gx-font-serif); font-size: 22px; font-weight: 700;
}
.nav-drawer .drawer-brand-name { font-family: var(--gx-font-serif); font-size: 20px; font-weight: 700; color: var(--gx-ink); }
.nav-drawer .drawer-nav { display: flex; flex-direction: column; gap: 6px; }
.nav-drawer .drawer-item {
  display: flex; align-items: center; padding: 12px 14px; border-radius: 8px;
  color: var(--gx-sub); font-size: 15px; transition: .2s;
}
.nav-drawer .drawer-item:hover { background: var(--gx-muted); color: var(--gx-ink); }
.nav-drawer .drawer-item.active { background: var(--gx-vermillion); color: #fff; }
.nav-drawer .drawer-item .nav-ico { margin-right: 10px; }
.nav-drawer .drawer-foot { margin-top: auto; padding-top: 16px; }
.nav-drawer .drawer-logout {
  width: 100%; padding: 12px 0; border: 1px solid var(--gx-divider); border-radius: 8px;
  background: var(--gx-warm-white); color: var(--gx-vermillion); font-size: 15px; cursor: pointer;
}
.user-drawer .user-panel { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 24px 20px 32px; }
.user-drawer .user-panel .user-avatar { width: 56px; height: 56px; }
.user-drawer .user-nick { font-family: var(--gx-font-serif); font-size: 18px; font-weight: 700; color: var(--gx-ink); }
.user-drawer .user-phone { font-size: 13px; color: var(--gx-sub); }
.user-drawer .panel-edit {
  width: 100%; margin-top: 12px; padding: 12px 0; border: 1px solid var(--gx-divider); border-radius: 8px;
  background: var(--gx-warm-white); color: var(--gx-celadon); font-size: 15px; cursor: pointer;
}
.user-drawer .panel-logout {
  width: 100%; margin-top: 12px; padding: 12px 0; border: none; border-radius: 8px;
  background: var(--gx-vermillion); color: #fff; font-size: 15px; cursor: pointer;
}
.user-drawer .panel-logout:active { opacity: .9; }
</style>

import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  { path: '/', name: 'home', component: () => import('@/views/Home.vue') },
  { path: '/login', name: 'login', component: () => import('@/views/Login.vue') },
  { path: '/privacy', name: 'privacy', component: () => import('@/views/Privacy.vue') },
  { path: '/child-protection', name: 'child-protection', component: () => import('@/views/ChildProtection.vue') },
  {
    path: '/app',
    component: () => import('@/layout/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: 'plan', name: 'plan', component: () => import('@/views/LearningPlan.vue') },
      { path: 'contract', name: 'contract', component: () => import('@/views/Contract.vue') },
      { path: 'grade', name: 'grade', component: () => import('@/views/Grade.vue') },
      { path: 'community', name: 'community', component: () => import('@/views/Community.vue') },
      { path: 'voice', name: 'voice', component: () => import('@/views/VoiceCommunity.vue') },
      { path: 'children', name: 'children', component: () => import('@/views/Children.vue') }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 有效登录态 = 有 token
function isAuthed() {
  return !!useUserStore().token
}

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !isAuthed()) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'login' && isAuthed()) {
    return { name: 'plan' }
  }
})

export default router

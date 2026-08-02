import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'home', component: () => import('@/views/Home.vue') },
  {
    path: '/app',
    component: () => import('@/layout/AppLayout.vue'),
    children: [
      { path: 'plan', name: 'plan', component: () => import('@/views/LearningPlan.vue') },
      { path: 'grade', name: 'grade', component: () => import('@/views/Grade.vue') },
      { path: 'community', name: 'community', component: () => import('@/views/Community.vue') }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

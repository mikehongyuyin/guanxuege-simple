import request from './request'
import { USE_MOCK } from '@/config'
import mock from './mock'

export function planOverview(childId) {
  if (USE_MOCK) return mock.delay(mock.PLAN_OVERVIEW)
  return request.get('/plan/overview', { params: { childId } })
}

export function planList(childId) {
  if (USE_MOCK) return mock.delay(mock.PLANS)
  return request.get('/plan/list', { params: { childId } })
}

export function createPlan(plan) {
  if (USE_MOCK) return mock.delay({ ...plan, id: Date.now() })
  return request.post('/plan', plan)
}

export function updatePlan(plan) {
  if (USE_MOCK) return mock.delay(plan)
  return request.put('/plan', plan)
}

export function deletePlan(id) {
  if (USE_MOCK) return mock.delay(true)
  return request.delete(`/plan/${id}`)
}

export function planTasks(planId) {
  if (USE_MOCK) {
    const plan = mock.PLANS.find((p) => p.id === planId)
    return mock.delay(plan ? plan.tasks : [])
  }
  return request.get('/plan/task/list', { params: { planId } })
}

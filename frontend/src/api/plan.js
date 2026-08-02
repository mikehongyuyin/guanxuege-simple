import request from './request'

export function planOverview(childId) {
  return request.get('/plan/overview', { params: { childId } })
}

export function planList(childId) {
  return request.get('/plan/list', { params: { childId } })
}

export function createPlan(plan) {
  return request.post('/plan', plan)
}

export function updatePlan(plan) {
  return request.put('/plan', plan)
}

export function deletePlan(id) {
  return request.delete(`/plan/${id}`)
}

export function planTasks(planId) {
  return request.get('/plan/task/list', { params: { planId } })
}

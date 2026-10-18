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

export function createPlanTask(task) {
  return request.post('/plan/task', task)
}

export function taskCheckin(taskId, childId, note = '') {
  return request.post(`/plan/task/${taskId}/checkin`, { childId, note })
}

export function taskCheckinStatus(taskId, childId) {
  return request.get(`/plan/task/${taskId}/checkin/status`, { params: { childId } })
}

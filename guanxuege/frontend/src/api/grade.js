import request from './request'
import { USE_MOCK } from '@/config'
import mock from './mock'

export function gradeOverview(childId) {
  if (USE_MOCK) return mock.delay(mock.GRADE_OVERVIEW)
  return request.get('/grade/overview', { params: { childId } })
}

export function gradeList(childId) {
  if (USE_MOCK) return mock.delay(mock.GRADES)
  return request.get('/grade/list', { params: { childId } })
}

export function createGrade(grade) {
  if (USE_MOCK) return mock.delay({ ...grade, id: Date.now() })
  return request.post('/grade', grade)
}

export function deleteGrade(id) {
  if (USE_MOCK) return mock.delay(true)
  return request.delete(`/grade/${id}`)
}

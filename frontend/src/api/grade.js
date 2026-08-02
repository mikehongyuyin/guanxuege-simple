import request from './request'

export function gradeOverview(childId) {
  return request.get('/grade/overview', { params: { childId } })
}

export function gradeList(childId) {
  return request.get('/grade/list', { params: { childId } })
}

export function createGrade(grade) {
  return request.post('/grade', grade)
}

export function deleteGrade(id) {
  return request.delete(`/grade/${id}`)
}

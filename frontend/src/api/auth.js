import request from './request'

export function login(phone, password) {
  return request.post('/auth/login', { phone, password })
}

export function register(dto) {
  return request.post('/auth/register', dto)
}

export function getMe() {
  return request.get('/auth/me')
}

export function getChildren() {
  return request.get('/child/list')
}

export function createChild(child) {
  return request.post('/child', child)
}

export function updateChild(child) {
  return request.put('/child', child)
}

export function deleteChild(id) {
  return request.delete(`/child/${id}`)
}

export function getFamily() {
  return request.get('/family')
}

export function createFamily(family) {
  return request.post('/family', family)
}

export function updateFamily(family) {
  return request.put('/family', family)
}

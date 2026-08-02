import request from './request'
import { USE_MOCK } from '@/config'
import mock from './mock'

export function login(phone, password) {
  if (USE_MOCK) {
    return mock.delay({ token: 'mock-token-xxx', user: mock.USER })
  }
  return request.post('/auth/login', { phone, password })
}

export function register(dto) {
  if (USE_MOCK) {
    return mock.delay({ ...mock.USER, ...dto })
  }
  return request.post('/auth/register', dto)
}

export function getMe() {
  if (USE_MOCK) return mock.delay(mock.USER)
  return request.get('/auth/me')
}

export function getChildren() {
  if (USE_MOCK) return mock.delay(mock.CHILDREN)
  return request.get('/child/list')
}

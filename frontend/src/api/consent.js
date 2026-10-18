import request from './request'

// 用户同意相关接口（含儿童个人信息专项同意）
export function recordConsent(dto) {
  return request.post('/consent', dto)
}

export function withdrawConsent(dto) {
  return request.post('/consent/withdraw', dto)
}

export function listConsent() {
  return request.get('/consent/list')
}

export function checkConsent(consentType, childId) {
  return request.get('/consent/check', { consentType, childId })
}

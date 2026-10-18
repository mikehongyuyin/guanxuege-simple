import request from './request'

export function contractList(childId = null) {
  const params = {}
  if (childId) params.childId = childId
  return request.get('/contract/list', { params })
}

export function createContract(contract) {
  return request.post('/contract', contract)
}

export function updateContract(contract) {
  return request.put('/contract', contract)
}

export function deleteContract(id) {
  return request.delete(`/contract/${id}`)
}

export function confirmContract(id) {
  return request.post(`/contract/${id}/confirm`)
}

export function markContractStatus(id, status) {
  return request.post(`/contract/${id}/status`, null, { params: { status } })
}

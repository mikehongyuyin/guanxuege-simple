import request from './request'

/**
 * 圈子列表。
 * @param community 传 parenting / voice 只取该社区的圈子；不传返回全部
 */
export function circleList(community = null) {
  const params = {}
  if (community) params.community = community
  return request.get('/circle/list', { params })
}

export function joinCircle(id) {
  return request.post(`/circle/${id}/join`)
}

export function leaveCircle(id) {
  return request.delete(`/circle/${id}/join`)
}

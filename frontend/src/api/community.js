import request from './request'

/**
 * 帖子列表。
 * @param community 社区维度：parenting 育儿 / voice 心声，用于隔离两个社区的内容
 */
export function postList(tab = 'recommend', circleId = null, community = 'parenting') {
  const params = { tab, community }
  if (circleId) params.circleId = circleId
  return request.get('/post/list', { params })
}

export function createPost(post) {
  return request.post('/post', post)
}

export function updatePost(post) {
  return request.put(`/post/${post.id}`, post)
}

export function deletePost(id) {
  return request.delete(`/post/${id}`)
}

export function likePost(id) {
  return request.post(`/post/${id}/like`)
}

export function commentList(postId) {
  return request.get('/comment/list', { params: { postId } })
}

export function createComment(comment) {
  return request.post('/comment', comment)
}

export function deleteComment(id) {
  return request.delete(`/comment/${id}`)
}

export function uploadImage(file) {
  const fd = new FormData()
  fd.append('file', file)
  // 上传耗时较长，单独放宽超时
  return request.post('/upload', fd, { timeout: 30000 })
}

/**
 * 热议话题榜。
 * @param limit 条数
 * @param community 传 parenting / voice 只统计该社区；不传统计全部
 */
export function hotTopics(limit = 10, community = null) {
  const params = { limit }
  if (community) params.community = community
  return request.get('/topic/hot', { params })
}

export function postToPlan(id, dto) {
  return request.post(`/post/${id}/to-plan`, dto)
}

/** 活跃用户榜。community 传 parenting / voice 只统计该社区 */
export function activeParents(limit = 5, community = null) {
  const params = { limit }
  if (community) params.community = community
  return request.get('/community/active-parents', { params })
}

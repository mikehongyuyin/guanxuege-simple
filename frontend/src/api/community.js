import request from './request'

export function postList(tab = 'recommend') {
  return request.get('/post/list', { params: { tab } })
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

export function hotTopics(limit = 10) {
  return request.get('/topic/hot', { params: { limit } })
}

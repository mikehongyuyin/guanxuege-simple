import request from './request'
import { USE_MOCK } from '@/config'
import mock from './mock'

export function postList(tab = 'recommend') {
  if (USE_MOCK) return mock.delay(mock.POSTS)
  return request.get('/post/list', { params: { tab } })
}

export function createPost(post) {
  if (USE_MOCK) return mock.delay({ ...post, id: Date.now(), likeCount: 0, commentCount: 0 })
  return request.post('/post', post)
}

export function likePost(id) {
  if (USE_MOCK) return mock.delay(1)
  return request.post(`/post/${id}/like`)
}

export function commentList(postId) {
  if (USE_MOCK) return mock.delay([])
  return request.get('/comment/list', { params: { postId } })
}

export function createComment(comment) {
  if (USE_MOCK) return mock.delay({ ...comment, id: Date.now() })
  return request.post('/comment', comment)
}

export function hotTopics(limit = 10) {
  if (USE_MOCK) return mock.delay(mock.TOPICS)
  return request.get('/topic/hot', { params: { limit } })
}

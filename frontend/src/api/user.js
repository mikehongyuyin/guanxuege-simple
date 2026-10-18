import request from './request'

/** 当前登录用户的资料（含联系方式） */
export function getMyProfile() {
  return request.get('/user/profile')
}

/** 更新当前登录用户资料 */
export function updateMyProfile(dto) {
  return request.put('/user/profile', dto)
}

/**
 * 查看指定用户资料。
 * 查看他人时后端不返回手机号 / 微信号 / 邮箱，前端展示为「仅本人可见」。
 */
export function getUserProfile(id) {
  return request.get(`/user/${id}`)
}

/** 昵称关键字搜索：用于 @提及选择器 */
export function searchUsers(keyword = '', limit = 10) {
  return request.get('/user/search', { params: { keyword, limit } })
}

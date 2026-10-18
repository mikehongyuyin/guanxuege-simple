<template>
  <div class="cm-page">
    <div class="cm-row">
      <!-- 左侧：帖子列表 / 帖子详情 / 发帖 共用同一块区域 -->
      <div class="feed">
        <!-- 顶部：标签页 + 发布话题（列表/详情/发帖时均保留） -->
        <div class="feed-head">
          <el-tabs v-model="tab" @tab-change="loadPosts">
            <el-tab-pane label="推荐" name="recommend" />
            <el-tab-pane label="最新" name="latest" />
            <el-tab-pane label="热议" name="hot" />
          </el-tabs>
          <el-button type="primary" class="gx-btn-primary post-btn" @click="openPost">发布话题</el-button>
        </div>

        <!-- 分层圈子（常驻） -->
        <div class="circles">
          <div class="circles-title">圈子</div>
          <div class="circles-list">
            <div
              v-for="c in circles"
              :key="c.id"
              class="circle-chip"
              :class="{ active: activeCircle === c.id, joined: c.joined }"
              :style="{ '--clr': c.color }"
              @click="selectCircle(c)"
            >
              <span class="cc-name">{{ c.name }}</span>
              <span class="cc-scope">{{ c.scope }}</span>
              <el-button
                size="small"
                :type="c.joined ? 'default' : 'primary'"
                class="cc-btn gx-btn-primary"
                @click.stop="toggleJoin(c)"
              >
                {{ c.joined ? '已加入' : '加入' }}
              </el-button>
            </div>
          </div>
        </div>

        <!-- ========== 帖子列表 ========== -->
        <template v-if="view === 'list'">
          <div v-for="p in posts" :key="p.id" class="gx-card post" @click="openDetail(p)">
            <div class="post-head">
              <el-avatar :size="34" class="pa clickable" @click.stop="openProfile(p.authorId)">
                {{ (p.authorNickname || '家').charAt(0) }}
              </el-avatar>
              <div class="post-meta">
                <span class="pa-name clickable" @click.stop="openProfile(p.authorId)">
                  {{ p.authorNickname || '匿名用户' }}
                </span>
                <span v-if="p.circleName" class="pa-circle" :style="{ color: circleColorOf(p.circleName) }">{{ p.circleName }}</span>
              </div>
            </div>
            <div class="post-title">{{ p.title }}</div>
            <!-- 正文按 @提及渲染：命中真实用户时可点击查看资料 -->
            <div class="post-excerpt">
              <MentionText :content="p.content" :mentions="p.mentions" @mention-click="openProfile" />
            </div>
            <div v-if="postImagesOf(p).length" class="post-imgs">
              <el-image
                v-for="u in postImagesOf(p)"
                :key="u"
                :src="u"
                :preview-src-list="postImagesOf(p)"
                preview-teleported
                class="post-img"
                fit="cover"
              />
            </div>
            <div class="post-foot">
              <span class="post-tag">#{{ p.tag }}</span>
              <span class="post-stat">👍 {{ p.likeCount }} · 💬 {{ p.commentCount }}</span>
              <el-button text :type="p.liked ? 'primary' : 'info'" @click.stop="like(p)">{{ p.liked ? '已赞' : '点赞' }}</el-button>
              <template v-if="isMyPost(p)">
                <el-button text type="primary" @click.stop="openEdit(p)">编辑</el-button>
                <el-button text type="danger" @click.stop="onRemovePost(p)">删除</el-button>
              </template>
            </div>
          </div>
          <el-empty v-if="posts.length === 0" :description="cfg.emptyText" />
        </template>

        <!-- ========== 帖子详情视图（只占列表区域，顶部保留） ========== -->
        <div v-else-if="view === 'detail'" class="detail-wrap">
        <div class="gx-card post-detail">
          <div class="dt-head">
            <el-button text type="primary" class="dt-back" @click="backToList">返回列表</el-button>
            <el-avatar :size="30" class="pa clickable" @click.stop="openProfile(detailPost?.authorId)">
              {{ (detailPost?.authorNickname || '家').charAt(0) }}
            </el-avatar>
            <span class="dt-author clickable" @click.stop="openProfile(detailPost?.authorId)">
              {{ detailPost?.authorNickname || '匿名用户' }}
            </span>
            <span class="dt-tag">#{{ detailPost?.tag }}</span>
            <span v-if="detailPost?.circleName" class="dt-tag" :style="{ color: circleColorOf(detailPost.circleName) }">{{ detailPost.circleName }}</span>
            <span class="dt-time">{{ formatTime(detailPost?.createTime) }}</span>
          </div>
          <div class="dt-title">{{ detailPost?.title }}</div>

          <!-- 反焦虑护栏提示 -->
          <div v-if="detailPost?.anxietyFlag === 1" class="guard-banner">
            <span class="gb-icon">☀</span>
            <span class="gb-text">{{ cfg.guardText }}</span>
          </div>

          <div class="dt-content">
            <MentionText :content="detailPost?.content" :mentions="detailPost?.mentions" @mention-click="openProfile" />
          </div>
          <div v-if="detailImages.length" class="dt-c-imgs dt-post-imgs">
            <el-image
              v-for="u in detailImages"
              :key="u"
              :src="u"
              :preview-src-list="detailImages"
              preview-teleported
              class="dt-c-img dt-post-img"
              fit="cover"
            />
          </div>
          <div class="dt-actions">
            <!-- 「转为学习计划」是育儿社区专属闭环，心声社区不提供 -->
            <el-button v-if="cfg.showToPlan" type="primary" plain class="gx-btn-primary" @click="openToPlan">转为学习计划</el-button>
            <el-button text :type="detailPost?.liked ? 'primary' : 'info'" @click.stop="like(detailPost)">👍 {{ detailPost?.liked ? '已赞' : '点赞' }}（{{ detailPost?.likeCount }}）</el-button>
          </div>

          <div class="dt-comments">
            <div class="dt-c-title">评论（{{ comments.length }}）</div>
            <div v-if="comments.length === 0" class="dt-c-empty">还没有评论，来说两句吧</div>
            <div v-for="c in comments" :key="c.id" class="dt-c-row">
              <el-avatar :size="28" class="pa clickable" @click.stop="openProfile(c.userId)">
                {{ (c.authorNickname || '家').charAt(0) }}
              </el-avatar>
              <div class="dt-c-body">
                <div class="dt-c-meta">
                  <span class="dt-c-name clickable" @click.stop="openProfile(c.userId)">
                    {{ c.authorNickname || '匿名用户' }}
                  </span>
                  <span class="dt-c-time">{{ formatTime(c.createTime) }}</span>
                  <el-button v-if="isMyComment(c)" text type="danger" size="small" class="dt-c-del" @click="removeComment(c)">删除</el-button>
                </div>
                <div class="dt-c-content">
                  <MentionText :content="c.content" :mentions="c.mentions" @mention-click="openProfile" />
                </div>
                <div v-if="commentImagesOf(c).length" class="dt-c-imgs">
                  <el-image
                    v-for="u in commentImagesOf(c)"
                    :key="u"
                    :src="u"
                    :preview-src-list="commentImagesOf(c)"
                    preview-teleported
                    class="dt-c-img"
                    fit="cover"
                  />
                </div>
              </div>
            </div>
          </div>

          <div class="dt-input">
            <div class="dt-input-main">
              <el-input v-model="commentText" type="textarea" :rows="2" maxlength="500" placeholder="写下你的评论…" />
              <div class="dt-up">
                <el-button text size="small" class="up-btn" @click="openMentionPicker('comment')">@ 提及</el-button>
                <el-upload :show-file-list="false" :http-request="doUploadImage" accept="image/*" :disabled="uploading">
                  <el-button text size="small" class="up-btn">添加图片</el-button>
                </el-upload>
                <span v-if="uploading" class="up-tip">上传中…</span>
                <div v-for="(img, i) in commentImages" :key="img" class="up-thumb">
                  <img :src="img" alt="待上传图片" />
                  <span class="up-del" @click="commentImages.splice(i, 1)">×</span>
                </div>
              </div>
            </div>
            <el-button type="primary" class="gx-btn-primary" @click="submitComment">发表评论</el-button>
          </div>
        </div>
      </div>

        <!-- ========== 发帖 / 编辑话题（内嵌于列表区域，顶部保留） ========== -->
        <div v-else-if="view === 'post'" class="detail-wrap">
          <div class="gx-card post-edit">
            <div class="pe-head">
              <el-button text type="primary" class="dt-back" @click="backToList">返回列表</el-button>
              <span class="pe-title">{{ editId ? '编辑话题' : '发布话题' }}</span>
            </div>
            <el-form :model="form" label-width="60px">
              <el-form-item label="模板">
                <el-select v-model="template" placeholder="选择模板（可选）" clearable @change="applyTemplate">
                  <el-option v-for="t in cfg.templates" :key="t.name" :label="t.name" :value="t.name" />
                </el-select>
              </el-form-item>
              <el-form-item label="标题"><el-input v-model="form.title" placeholder="一句话说清你的问题或心得" /></el-form-item>
              <el-form-item label="圈子">
                <el-select v-model="form.circleId" placeholder="选择圈子（可选）" clearable style="width: 100%">
                  <el-option v-for="c in circles" :key="c.id" :label="`${c.name}（${c.scope}）`" :value="c.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="标签">
                <el-select v-model="form.tag">
                  <el-option v-for="t in cfg.tags" :key="t" :label="t" :value="t" />
                </el-select>
              </el-form-item>
              <el-form-item label="内容">
                <el-input
                  v-model="form.content"
                  type="textarea"
                  :rows="8"
                  placeholder="支持 Ctrl+V 直接粘贴图片；输入 @ 可直接提及他人"
                  @paste="onContentPaste"
                />
                <div class="dt-up post-up">
                  <el-button text size="small" class="up-btn" @click="openMentionPicker('post')">@ 提及</el-button>
                  <span v-if="pasteTip" class="up-tip">{{ pasteTip }}</span>
                  <div v-for="(img, i) in form.images" :key="img" class="up-thumb">
                    <img :src="img" alt="帖子图片" />
                    <span class="up-del" @click="form.images.splice(i, 1)">×</span>
                  </div>
                </div>
              </el-form-item>
            </el-form>
            <div class="pe-actions">
              <el-button @click="backToList">取消</el-button>
              <el-button type="primary" class="gx-btn-primary" @click="submit">{{ editId ? '保存' : '发布' }}</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧栏（始终保留） -->
      <aside class="widgets">
        <div class="gx-card widget">
          <div class="w-tip">{{ cfg.shareTip }}</div>
          <el-button type="primary" class="gx-btn-primary w-btn" @click="openPost">发布话题</el-button>
        </div>

        <div class="gx-card widget">
          <div class="w-title">{{ cfg.activeTitle }}</div>
          <div v-for="(m, i) in activeParents" :key="m.userId" class="active-row">
            <el-avatar :size="32" :style="{ background: parentColor(i) }" class="pa clickable" @click="openProfile(m.userId)">
              {{ (m.nickname || '用').charAt(0) }}
            </el-avatar>
            <div class="active-info">
              <span class="an">{{ m.nickname || '匿名用户' }}</span>
              <span class="ar">发帖 {{ m.postCount }} · 评论 {{ m.commentCount }}</span>
            </div>
            <span class="lv">Lv.{{ m.totalCount }}</span>
          </div>
          <div v-if="activeParents.length === 0" class="ar-empty">暂无活跃用户，快来发帖吧</div>
        </div>

        <div class="gx-card widget">
          <div class="w-title">热议话题</div>
          <div v-for="(t, i) in topics" :key="t.id" class="topic-row">
            <span class="rank">{{ i + 1 }}</span>
            <span class="t-name">{{ t.name }}</span>
            <span class="t-count">{{ t.discussCount }} 讨论</span>
          </div>
        </div>
      </aside>
    </div>

    <!-- 帖子一键转计划弹窗（仅育儿社区） -->
    <el-dialog v-model="toPlanDialog" title="将帖子转为学习计划" width="480px">
      <el-form :model="toPlanForm" label-width="80px">
        <el-form-item label="选择孩子" required>
          <el-select v-model="toPlanForm.childId" placeholder="选择孩子" style="width: 100%">
            <el-option v-for="c in children" :key="c.id" :label="`${c.name}（${c.grade}）`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划标题"><el-input v-model="toPlanForm.title" /></el-form-item>
        <el-form-item label="任务列表">
          <el-input
            v-model="toPlanForm.taskTitlesText"
            type="textarea"
            :rows="4"
            placeholder="每行一个任务，例如：&#10;每天专注阅读 20 分钟&#10;记录一周执行感受"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="toPlanDialog = false">取消</el-button>
        <el-button type="primary" class="gx-btn-primary" @click="submitToPlan">生成计划</el-button>
      </template>
    </el-dialog>

    <!-- @ 提及选择器 -->
    <el-dialog v-model="mentionDialog" title="提及谁？" width="420px">
      <el-input v-model="mentionKeyword" placeholder="输入昵称搜索" @input="loadMentionUsers" />
      <div class="mp-list">
        <div
          v-for="u in mentionUsers"
          :key="u.id"
          class="mp-row"
          @click="pickMention(u)"
        >
          <el-avatar :size="28" class="pa">{{ (u.displayName || u.nickname || '用').charAt(0) }}</el-avatar>
          <span class="mp-name">{{ u.displayName || u.nickname }}</span>
          <span v-if="u.communityNickname" class="mp-sub">@{{ u.communityNickname }}</span>
        </div>
        <div v-if="mentionUsers.length === 0" class="mp-empty">没有匹配的用户</div>
      </div>
    </el-dialog>

    <!-- 用户资料弹窗 -->
    <UserProfileDialog v-model="profileVisible" :user-id="profileUserId" />
  </div>
</template>

<script setup>
import { ref, inject, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import MentionText from '@/components/MentionText.vue'
import UserProfileDialog from '@/components/UserProfileDialog.vue'
import {
  postList, createPost, updatePost, deletePost, likePost, hotTopics,
  commentList, createComment, deleteComment, uploadImage, postToPlan, activeParents as fetchActiveParents
} from '@/api/community'
import { circleList, joinCircle, leaveCircle } from '@/api/circle'
import { searchUsers } from '@/api/user'

const props = defineProps({
  /** 社区维度：parenting 育儿社区 / voice 心声社区 */
  community: { type: String, required: true }
})

/**
 * 两个社区共用一套交互，差异全部收敛在这份配置里：
 * 标签、模板、右侧文案、是否支持转计划、反焦虑提示文案。
 */
const COMMUNITY_CONFIG = {
  parenting: {
    tags: ['习惯养成', '成绩交流', '双减政策'],
    defaultTag: '习惯养成',
    templates: [
      { name: '求助问题模板', content: '【孩子基本情况】\n【最近遇到的问题】\n【已经尝试过的办法】\n【希望获得什么建议】' },
      { name: '习惯养成模板', content: '【想养成的习惯】\n【目前的做法】\n【遇到的阻力】\n【目标与奖励】' },
      { name: '成绩提升模板', content: '【学科与年级】\n【当前水平/目标】\n【已做过的尝试】\n【最近一次考试情况】' }
    ],
    shareTip: '有什么育儿心得想分享？',
    activeTitle: '活跃家长',
    emptyText: '暂无帖子',
    showToPlan: true,
    guardText: '这个帖子的话题可能容易引发焦虑。放轻松——孩子的成长有自己的节奏，先看具体的小事，再找方法。'
  },
  voice: {
    tags: ['职场困惑', '副业探索', '创业复盘', '生活随笔'],
    defaultTag: '职场困惑',
    templates: [
      { name: '职场困境模板', content: '【当前处境】\n【已经尝试过的办法】\n【最纠结的点】\n【希望听到什么】' },
      { name: '副业起步模板', content: '【主业与时间情况】\n【想做的方向】\n【已经投入的成本】\n【目前卡在哪】' },
      { name: '复盘模板', content: '【做了什么】\n【结果如何】\n【做对了什么】\n【下次会怎么改】' }
    ],
    shareTip: '最近有什么想说的？',
    activeTitle: '活跃用户',
    emptyText: '还没有人开口，你可以先说',
    showToPlan: false,
    guardText: '这个话题读起来有点紧绷。慢慢说——把事情拆小一点，往往更容易找到出口。'
  }
}
const cfg = computed(() => COMMUNITY_CONFIG[props.community] || COMMUNITY_CONFIG.parenting)

const router = useRouter()
const userStore = useUserStore()
const myId = userStore.user?.id
const children = inject('children', ref([]))
const currentChild = inject('currentChild', ref(null))
function isMyPost(p) {
  return !!myId && p.authorId === myId
}

const view = ref('list') // list: 帖子列表 / detail: 帖子详情 / post: 发帖
const tab = ref('recommend')
const posts = ref([])
const topics = ref([])

// ===== 分层圈子（按社区维度拉取）=====
const circles = ref([])
const activeCircle = ref(null)
async function loadCircles() {
  circles.value = await circleList(props.community)
}
function selectCircle(c) {
  activeCircle.value = activeCircle.value === c.id ? null : c.id
  loadPosts()
}
async function toggleJoin(c) {
  if (c.joined) {
    await leaveCircle(c.id)
    c.joined = false
    c.memberCount = Math.max(0, (c.memberCount || 0) - 1)
    ElMessage.success(`已退出「${c.name}」`)
  } else {
    await joinCircle(c.id)
    c.joined = true
    c.memberCount = (c.memberCount || 0) + 1
    ElMessage.success(`已加入「${c.name}」，欢迎！`)
  }
}
function circleColorOf(name) {
  const c = circles.value.find((x) => x.name === name)
  return c ? c.color : '#81B29A'
}

// ===== 模板 =====
const template = ref('')
function applyTemplate(name) {
  const t = cfg.value.templates.find((x) => x.name === name)
  if (t && !form.value.content.trim()) {
    form.value.content = t.content
  }
  template.value = ''
}

// ===== 活跃用户（真实排行数据） =====
const parentColors = ['#205C49', '#8A6440', '#B5392E', '#4A6F8A', '#9B8A6F']
const activeParents = ref([])
function parentColor(i) {
  return parentColors[i % parentColors.length]
}
async function loadActiveParents() {
  // 按社区过滤：否则心声社区会显示育儿社区的活跃榜
  activeParents.value = await fetchActiveParents(5, props.community)
}

async function loadPosts() {
  posts.value = await postList(tab.value, activeCircle.value, props.community)
  view.value = 'list'
  detailPost.value = null
  comments.value = []
}
async function init() {
  await loadCircles()
  await loadPosts()
  // 话题榜同样按社区过滤
  topics.value = await hotTopics(5, props.community)
  await loadActiveParents()
}
onMounted(init)

// ===== 用户资料 =====
const profileVisible = ref(false)
const profileUserId = ref(null)
function openProfile(userId) {
  if (!userId) return
  profileUserId.value = userId
  profileVisible.value = true
}

// ===== @ 提及 =====
const mentionDialog = ref(false)
const mentionKeyword = ref('')
const mentionUsers = ref([])
let mentionTarget = 'post'
async function loadMentionUsers() {
  mentionUsers.value = await searchUsers(mentionKeyword.value.trim(), 10)
}
async function openMentionPicker(target) {
  mentionTarget = target
  mentionKeyword.value = ''
  mentionDialog.value = true
  try {
    mentionUsers.value = await searchUsers('', 10)
  } catch (e) {
    mentionUsers.value = []
  }
}
function pickMention(u) {
  const name = u.displayName || u.nickname
  if (!name) return
  const text = `@${name} `
  if (mentionTarget === 'post') {
    form.value.content = (form.value.content || '') + text
  } else {
    commentText.value = (commentText.value || '') + text
  }
  mentionDialog.value = false
}

/**
 * 点赞：幂等 toggle。已赞则取消（-1），未赞则点赞（+1）。
 * 先做乐观更新保证手感，再用服务端返回的最新计数校正；
 * 失败时回滚到本地操作前的状态。
 */
async function like(p) {
  if (!p) return
  const wasLiked = !!p.liked
  const before = p.likeCount || 0
  p.liked = !wasLiked
  p.likeCount = Math.max(0, before + (p.liked ? 1 : -1))
  try {
    const count = await likePost(p.id)
    if (typeof count === 'number') p.likeCount = count
  } catch (e) {
    p.liked = wasLiked
    p.likeCount = before
  }
}

const editId = ref(null)
const form = ref({ title: '', tag: cfg.value.defaultTag, content: '', circleId: null, images: [] })
const pasteTip = ref('')
function postImagesOf(p) {
  if (!p || !p.images) return []
  try {
    const arr = JSON.parse(p.images)
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
}
function openPost() {
  view.value = 'post'
  editId.value = null
  form.value = { title: '', tag: cfg.value.defaultTag, content: '', circleId: null, images: [] }
  pasteTip.value = ''
}
function openEdit(p) {
  view.value = 'post'
  editId.value = p.id
  form.value = { title: p.title, tag: p.tag || cfg.value.defaultTag, content: p.content, circleId: p.circleId || null, images: postImagesOf(p) }
  pasteTip.value = ''
}
// 内容区粘贴图片：读取剪贴板图片并上传
async function onContentPaste(e) {
  const items = e.clipboardData?.items || []
  for (const it of items) {
    if (it.type && it.type.startsWith('image/')) {
      const file = it.getAsFile()
      if (!file) return
      e.preventDefault()
      pasteTip.value = '图片上传中…'
      try {
        const url = await uploadImage(file)
        form.value.images.push(url)
        pasteTip.value = ''
        ElMessage.success('图片已粘贴')
      } catch {
        pasteTip.value = '图片上传失败，请重试'
      }
      return
    }
  }
}
async function submit() {
  if (!form.value.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  const payload = {
    ...form.value,
    // 归属社区由页面决定，避免前端传错导致帖子跑到另一个社区
    community: props.community,
    images: form.value.images.length ? JSON.stringify(form.value.images) : null
  }
  let created = null
  if (editId.value) {
    created = await updatePost({ id: editId.value, ...payload })
  } else {
    created = await createPost(payload)
  }
  view.value = 'list'
  if (created?.warning) {
    // 反焦虑护栏：温和提醒
    ElMessageBox.alert(created.warning, '一个小提醒', {
      confirmButtonText: '知道了',
      type: 'info'
    })
  } else {
    ElMessage.success(editId.value ? '已更新' : '发布成功')
  }
  await loadPosts()
}
async function onRemovePost(p) {
  try {
    await ElMessageBox.confirm(`确定删除帖子「${p.title}」吗？删除后不可恢复。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deletePost(p.id)
  ElMessage.success('已删除')
  await loadPosts()
}

// ===== 帖子详情视图 + 评论 =====
const detailPost = ref(null)
const comments = ref([])
const commentText = ref('')
const commentImages = ref([])
const uploading = ref(false)
const detailImages = computed(() => (detailPost.value ? postImagesOf(detailPost.value) : []))

function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}
function isMyComment(c) {
  return !!myId && c.userId === myId
}
function commentImagesOf(c) {
  if (!c.images) return []
  try {
    const arr = JSON.parse(c.images)
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
}

async function openDetail(p) {
  view.value = 'detail'
  detailPost.value = p
  commentText.value = ''
  commentImages.value = []
  comments.value = await commentList(p.id)
}

function backToList() {
  view.value = 'list'
  detailPost.value = null
  comments.value = []
  commentText.value = ''
  commentImages.value = []
}

// ===== 帖子一键转计划（仅育儿社区） =====
const toPlanDialog = ref(false)
const toPlanForm = ref({ childId: null, title: '', taskTitlesText: '' })
function openToPlan() {
  toPlanForm.value = {
    childId: currentChild.value,
    title: `「${detailPost.value.title}」践行计划`,
    taskTitlesText: '按帖子方法坚持一周\n记录每周执行感受'
  }
  toPlanDialog.value = true
}
async function submitToPlan() {
  if (!toPlanForm.value.childId) {
    ElMessage.warning('请选择孩子')
    return
  }
  const taskTitles = toPlanForm.value.taskTitlesText
    .split('\n')
    .map((s) => s.trim())
    .filter(Boolean)
  await postToPlan(detailPost.value.id, {
    childId: toPlanForm.value.childId,
    title: toPlanForm.value.title.trim(),
    taskTitles
  })
  toPlanDialog.value = false
  ElMessageBox.alert('已为你的孩子生成学习计划，可以到「学习计划」中查看和打卡。', '转换成功', {
    confirmButtonText: '去看看',
    callback: () => router.push('/app/plan')
  })
}

async function doUploadImage(option) {
  uploading.value = true
  try {
    const url = await uploadImage(option.file)
    commentImages.value.push(url)
  } finally {
    uploading.value = false
  }
}

async function submitComment() {
  const text = commentText.value.trim()
  if (!text) {
    ElMessage.warning('请输入评论内容')
    return
  }
  const created = await createComment({
    postId: detailPost.value.id,
    content: text,
    images: commentImages.value.length ? JSON.stringify(commentImages.value) : null
  })
  comments.value.push(created)
  commentText.value = ''
  commentImages.value = []
  detailPost.value.commentCount = (detailPost.value.commentCount || 0) + 1
  if (created.anxietyFlag === 1) {
    ElMessage.info('嗯，这个话题容易引发焦虑，试着把问题说得具体一点，会更容易得到帮助。')
  }
}

async function removeComment(c) {
  await deleteComment(c.id)
  comments.value = comments.value.filter((x) => x.id !== c.id)
  detailPost.value.commentCount = Math.max(0, (detailPost.value.commentCount || 0) - 1)
  ElMessage.success('评论已删除')
}
</script>

<style scoped>
.cm-row { display: grid; grid-template-columns: 1fr 320px; gap: 20px; align-items: start; }
.feed { min-width: 0; }
.detail-wrap { min-width: 0; }
.feed-head { display: flex; align-items: center; justify-content: space-between; background: var(--gx-warm-white); border: 1px solid var(--gx-divider); border-radius: var(--gx-radius); padding: 4px 16px; margin-bottom: 16px; }
.post-btn { margin: 10px 0; }

/* 圈子 */
.circles { background: var(--gx-warm-white); border: 1px solid var(--gx-divider); border-radius: var(--gx-radius); padding: 14px 16px; margin-bottom: 14px; }
.circles-title { font-family: var(--gx-font-serif); font-size: 15px; font-weight: 700; margin-bottom: 10px; }
.circles-list { display: flex; flex-wrap: wrap; gap: 10px; }
.circle-chip {
  display: flex; align-items: center; gap: 8px; padding: 8px 12px; border: 1px solid var(--gx-divider);
  border-radius: 10px; cursor: pointer; transition: .2s; background: #fff;
}
.circle-chip:hover { border-color: var(--clr, #81B29A); }
.circle-chip.active { border-color: var(--clr, #81B29A); box-shadow: 0 0 0 1px var(--clr, #81B29A); }
.circle-chip.joined::before { content: '✓'; color: var(--clr, #81B29A); font-weight: 700; }
.cc-name { font-weight: 600; font-size: 14px; }
.cc-scope { color: var(--gx-sub); font-size: 12px; }
.cc-btn { margin-left: 4px; }

.post { padding: 18px; margin-bottom: 14px; cursor: pointer; }
.post-head { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.pa { background: var(--gx-celadon); color: #fff; font-weight: 700; }
.clickable { cursor: pointer; }
.pa.clickable:hover { opacity: .85; }
.pa-name { font-size: 14px; color: var(--gx-sub); }
.pa-name.clickable:hover { color: var(--gx-vermillion); }
.pa-circle { font-size: 12px; padding: 1px 8px; border-radius: 8px; background: rgba(0,0,0,.04); }
.post-title { font-family: var(--gx-font-serif); font-size: 17px; font-weight: 700; margin-bottom: 8px; }
.post-excerpt { color: var(--gx-sub); font-size: 14px; line-height: 1.7; margin-bottom: 12px; white-space: pre-wrap; }
.post-imgs { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }
.post-img { width: 96px; height: 96px; border-radius: 8px; cursor: zoom-in; }
.post-up { margin-top: 8px; }
.post-foot { display: flex; align-items: center; gap: 14px; }
.post-tag { color: var(--gx-vermillion); font-size: 13px; }
.post-stat { color: var(--gx-sub); font-size: 13px; }

.widget { padding: 16px; margin-bottom: 16px; }
.w-tip { color: var(--gx-sub); font-size: 14px; margin-bottom: 12px; }
.w-btn { width: 100%; }
.w-title { font-family: var(--gx-font-serif); font-size: 16px; font-weight: 700; margin-bottom: 14px; }
.active-row { display: flex; align-items: center; gap: 10px; padding: 8px 0; }
.active-info { flex: 1; display: flex; flex-direction: column; }
.an { font-size: 14px; font-weight: 600; }
.ar { font-size: 12px; color: var(--gx-sub); }
.lv { font-size: 12px; color: var(--gx-ochre); }
.ar-empty { color: var(--gx-sub); font-size: 13px; text-align: center; padding: 14px 0; }
.topic-row { display: flex; align-items: center; gap: 10px; padding: 8px 0; font-size: 14px; }
.rank { color: var(--gx-vermillion); font-weight: 700; font-family: var(--gx-font-serif); width: 18px; }
.t-name { flex: 1; color: var(--gx-ink); }
.t-count { font-size: 12px; color: var(--gx-sub); }

/* 帖子详情视图 */
.post-detail { padding: 22px; border-color: var(--gx-vermillion); }
.post-edit { padding: 22px; border-color: var(--gx-celadon); }
.pe-head { display: flex; align-items: center; gap: 10px; margin-bottom: 14px; }
.pe-title { font-family: var(--gx-font-serif); font-size: 18px; font-weight: 700; }
.pe-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 8px; }
.dt-head { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.dt-back { margin-right: 6px; color: var(--gx-vermillion); }
.dt-author { font-size: 14px; color: var(--gx-sub); }
.dt-author.clickable:hover { color: var(--gx-vermillion); }
.dt-tag { color: var(--gx-vermillion); font-size: 13px; }
.dt-time { margin-left: auto; color: var(--gx-sub); font-size: 12px; }
.dt-title { font-family: var(--gx-font-serif); font-size: 20px; font-weight: 700; margin-bottom: 10px; }
.guard-banner {
  display: flex; align-items: flex-start; gap: 8px; background: #FFF7E6; color: #8A6440;
  border: 1px solid #F0E0C0; border-radius: 8px; padding: 10px 12px; margin-bottom: 12px; font-size: 13px; line-height: 1.6;
}
.gb-icon { color: #E8A33D; font-weight: 700; }
.dt-content { color: var(--gx-ink); font-size: 14px; line-height: 1.8; margin-bottom: 10px; white-space: pre-wrap; }
.dt-actions { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; }
.dt-comments { border-top: 1px solid var(--gx-divider); padding-top: 14px; max-height: 380px; overflow-y: auto; }
.dt-c-title { font-weight: 700; margin-bottom: 10px; }
.dt-c-empty { color: var(--gx-sub); font-size: 13px; padding: 10px 0; }
.dt-c-row { display: flex; gap: 10px; padding: 10px 0; border-bottom: 1px dashed var(--gx-divider); }
.dt-c-body { flex: 1; }
.dt-c-meta { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.dt-c-name { font-size: 13px; font-weight: 600; }
.dt-c-name.clickable:hover { color: var(--gx-vermillion); }
.dt-c-time { font-size: 12px; color: var(--gx-sub); }
.dt-c-del { margin-left: auto; }
.dt-c-content { font-size: 14px; color: var(--gx-ink); line-height: 1.6; white-space: pre-wrap; }
.dt-c-imgs { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 8px; }
.dt-c-img { width: 64px; height: 64px; border-radius: 6px; cursor: zoom-in; }
.dt-post-imgs { margin-bottom: 12px; }
.dt-post-img { width: 120px; height: 120px; }
.dt-input { display: flex; gap: 10px; margin-top: 14px; align-items: flex-end; }
.dt-input-main { flex: 1; display: flex; flex-direction: column; gap: 6px; }
.dt-up { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; }
.up-btn { color: var(--gx-vermillion); }
.up-tip { font-size: 12px; color: var(--gx-sub); }
.up-thumb { position: relative; width: 48px; height: 48px; }
.up-thumb img { width: 100%; height: 100%; object-fit: cover; border-radius: 6px; }
.up-del {
  position: absolute; top: -6px; right: -6px; width: 16px; height: 16px; line-height: 14px;
  text-align: center; background: rgba(0, 0, 0, .55); color: #fff; border-radius: 50%;
  font-size: 12px; cursor: pointer;
}

/* @ 提及选择器 */
.mp-list { margin-top: 12px; max-height: 300px; overflow-y: auto; }
.mp-row { display: flex; align-items: center; gap: 10px; padding: 8px 6px; border-radius: 8px; cursor: pointer; }
.mp-row:hover { background: var(--gx-muted); }
.mp-name { font-size: 14px; font-weight: 600; }
.mp-sub { font-size: 12px; color: var(--gx-sub); }
.mp-empty { text-align: center; color: var(--gx-sub); font-size: 13px; padding: 20px 0; }

/* ===== 移动端适配（<768px） ===== */
@media (max-width: 767px) {
  /* 右侧栏移至帖子列表下方，整页单列 */
  .cm-row { grid-template-columns: 1fr; }
  .feed-head { flex-wrap: wrap; padding: 4px 10px; }
  .post { padding: 14px 12px; }
  .post-foot { flex-wrap: wrap; gap: 8px; }
  .post-img { width: 80px; height: 80px; }
  .circles { padding: 12px; }
  .post-detail, .post-edit { padding: 16px 12px; }
  .dt-head { flex-wrap: wrap; row-gap: 6px; }
  .dt-time { margin-left: 0; }
  .dt-title { font-size: 18px; }
  .dt-actions { flex-wrap: wrap; }
  /* 评论输入区：按钮换行占满 */
  .dt-input { flex-direction: column; align-items: stretch; }
  .dt-input .el-button { width: 100%; margin-left: 0; }
  .dt-post-img { width: 100px; height: 100px; }
}
</style>

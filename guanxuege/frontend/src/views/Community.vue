<template>
  <div class="cm-page">
    <div class="cm-row">
      <!-- 信息流 -->
      <div class="feed">
        <div class="feed-head">
          <el-tabs v-model="tab" @tab-change="loadPosts">
            <el-tab-pane label="推荐" name="recommend" />
            <el-tab-pane label="最新" name="latest" />
            <el-tab-pane label="热议" name="hot" />
          </el-tabs>
          <el-button type="primary" class="gx-btn-primary post-btn" @click="openPost">发布话题</el-button>
        </div>

        <div v-for="p in posts" :key="p.id" class="gx-card post">
          <div class="post-head">
            <el-avatar :size="34" class="pa">{{ (p.authorNickname || '家').charAt(0) }}</el-avatar>
            <div class="post-meta">
              <span class="pa-name">{{ p.authorNickname || '匿名家长' }}</span>
            </div>
          </div>
          <div class="post-title">{{ p.title }}</div>
          <div class="post-excerpt">{{ p.content }}</div>
          <div class="post-foot">
            <span class="post-tag">#{{ p.tag }}</span>
            <span class="post-stat">👍 {{ p.likeCount }} · 💬 {{ p.commentCount }}</span>
            <el-button text type="primary" @click="like(p)">点赞</el-button>
          </div>
        </div>
        <el-empty v-if="posts.length === 0" description="暂无帖子" />
      </div>

      <!-- 右侧栏 -->
      <aside class="widgets">
        <div class="gx-card widget">
          <div class="w-tip">有什么育儿心得想分享？</div>
          <el-button type="primary" class="gx-btn-primary w-btn" @click="openPost">发布话题</el-button>
        </div>

        <div class="gx-card widget">
          <div class="w-title">活跃家长</div>
          <div v-for="m in activeParents" :key="m.name" class="active-row">
            <el-avatar :size="32" :style="{ background: m.color }" class="pa">{{ m.name.charAt(0) }}</el-avatar>
            <div class="active-info"><span class="an">{{ m.name }}</span><span class="ar">{{ m.role }}</span></div>
            <span class="lv">{{ m.lv }}</span>
          </div>
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

    <el-dialog v-model="dialog" title="发布话题" width="480px">
      <el-form :model="form" label-width="60px">
        <el-form-item label="标题"><el-input v-model="form.title" placeholder="一句话说清你的问题或心得" /></el-form-item>
        <el-form-item label="标签">
          <el-select v-model="form.tag"><el-option label="习惯养成" value="习惯养成" /><el-option label="成绩交流" value="成绩交流" /><el-option label="双减政策" value="双减政策" /></el-select>
        </el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" class="gx-btn-primary" @click="submit">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { postList, createPost, likePost, hotTopics } from '@/api/community'

const tab = ref('recommend')
const posts = ref([])
const topics = ref([])

const activeParents = [
  { name: '王女士', role: '三年级学生家长', lv: 'Lv.8', color: '#205C49' },
  { name: '张先生', role: '六年级学生家长', lv: 'Lv.7', color: '#8A6440' },
  { name: '刘妈妈', role: '一年级学生家长', lv: 'Lv.6', color: '#B5392E' }
]

async function loadPosts() {
  posts.value = await postList(tab.value)
}
async function init() {
  await loadPosts()
  topics.value = await hotTopics(5)
}
onMounted(init)

async function like(p) {
  await likePost(p.id)
  p.likeCount = (p.likeCount || 0) + 1
}

const dialog = ref(false)
const form = ref({ title: '', tag: '习惯养成', content: '' })
function openPost() { dialog.value = true }
async function submit() {
  await createPost(form.value)
  dialog.value = false
  await loadPosts()
}
</script>

<style scoped>
.cm-row { display: grid; grid-template-columns: 1fr 320px; gap: 20px; align-items: start; }
.feed-head { display: flex; align-items: center; justify-content: space-between; background: var(--gx-warm-white); border: 1px solid var(--gx-divider); border-radius: var(--gx-radius); padding: 4px 16px; margin-bottom: 16px; }
.post-btn { margin: 10px 0; }
.post { padding: 18px; margin-bottom: 14px; }
.post-head { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.pa { background: var(--gx-celadon); color: #fff; font-weight: 700; }
.pa-name { font-size: 14px; color: var(--gx-sub); }
.post-title { font-family: var(--gx-font-serif); font-size: 17px; font-weight: 700; margin-bottom: 8px; }
.post-excerpt { color: var(--gx-sub); font-size: 14px; line-height: 1.7; margin-bottom: 12px; }
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
.topic-row { display: flex; align-items: center; gap: 10px; padding: 8px 0; font-size: 14px; }
.rank { color: var(--gx-vermillion); font-weight: 700; font-family: var(--gx-font-serif); width: 18px; }
.t-name { flex: 1; color: var(--gx-ink); }
.t-count { font-size: 12px; color: var(--gx-sub); }
</style>

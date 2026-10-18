<template>
  <el-dialog
    v-model="visible"
    :title="isSelf ? '我的资料' : '用户资料'"
    width="460px"
    class="profile-dialog"
  >
    <div v-if="loading" class="pf-loading">加载中…</div>

    <template v-else-if="profile">
      <!-- ===== 查看态 ===== -->
      <div v-if="!editing" class="pf-view">
        <div class="pf-head">
          <el-avatar :size="56" class="pf-avatar">{{ initial }}</el-avatar>
          <div class="pf-head-info">
            <div class="pf-name">{{ profile.displayName || '未设置昵称' }}</div>
            <div v-if="profile.communityNickname" class="pf-sub">
              社区昵称：{{ profile.communityNickname }}
            </div>
            <div v-else class="pf-sub">未设置社区昵称，社区内展示账号昵称</div>
          </div>
        </div>

        <div v-if="profile.signature" class="pf-sign">{{ profile.signature }}</div>
        <div v-else-if="isSelf" class="pf-sign pf-sign-empty">还没有签名，写一句自我介绍吧</div>

        <div class="pf-stats">
          <div class="pf-stat"><span class="pf-num">{{ profile.postCount ?? 0 }}</span><span class="pf-label">发帖</span></div>
          <div class="pf-stat"><span class="pf-num">{{ profile.commentCount ?? 0 }}</span><span class="pf-label">评论</span></div>
          <div class="pf-stat"><span class="pf-num">{{ joinDays }}</span><span class="pf-label">加入天数</span></div>
        </div>

        <div class="pf-contacts">
          <div class="pf-row">
            <span class="pf-k">微信号</span>
            <span class="pf-v">{{ isSelf ? (profile.wechat || '未填写') : '仅本人可见' }}</span>
          </div>
          <div class="pf-row">
            <span class="pf-k">邮箱</span>
            <span class="pf-v">{{ isSelf ? (profile.email || '未填写') : '仅本人可见' }}</span>
          </div>
          <div v-if="isSelf" class="pf-row">
            <span class="pf-k">手机号</span>
            <span class="pf-v">{{ profile.phone || '-' }}</span>
          </div>
        </div>

        <div class="pf-tip">
          联系方式仅本人可见。其他用户查看你的资料时，这些字段不会返回。
        </div>
      </div>

      <!-- ===== 编辑态（仅本人） ===== -->
      <el-form v-else :model="form" label-width="84px" class="pf-form">
        <el-form-item label="账号昵称">
          <el-input v-model="form.nickname" maxlength="50" show-word-limit placeholder="登录后在站内展示的名称" />
        </el-form-item>
        <el-form-item label="社区昵称">
          <el-input v-model="form.communityNickname" maxlength="30" show-word-limit placeholder="留空则使用账号昵称" />
          <div class="pf-hint">设置后，社区内的帖子与评论将用这个昵称展示</div>
        </el-form-item>
        <el-form-item label="微信号">
          <el-input v-model="form.wechat" maxlength="64" placeholder="选填，仅本人可见" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" maxlength="64" placeholder="选填，仅本人可见" />
        </el-form-item>
        <el-form-item label="签名">
          <el-input
            v-model="form.signature"
            type="textarea"
            :rows="2"
            maxlength="120"
            show-word-limit
            placeholder="写一句自我介绍"
          />
        </el-form-item>
      </el-form>
    </template>

    <template #footer>
      <template v-if="isSelf">
        <el-button v-if="!editing" @click="visible = false">关闭</el-button>
        <el-button v-if="!editing" type="primary" class="gx-btn-primary" @click="startEdit">编辑资料</el-button>
        <template v-else>
          <el-button @click="editing = false">取消</el-button>
          <el-button type="primary" class="gx-btn-primary" :loading="saving" @click="save">保存</el-button>
        </template>
      </template>
      <el-button v-else @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserProfile, updateMyProfile } from '@/api/user'
import { useUserStore } from '@/store/user'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  // 目标用户 id；为空表示查看自己
  userId: { type: [Number, String], default: null }
})
const emit = defineEmits(['update:modelValue'])

const userStore = useUserStore()
const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const loading = ref(false)
const saving = ref(false)
const editing = ref(false)
const profile = ref(null)

const isSelf = computed(() => {
  const me = userStore.user?.id
  if (!me) return false
  return !props.userId || String(props.userId) === String(me)
})
const initial = computed(() => (profile.value?.displayName || '用').charAt(0))
const joinDays = computed(() => {
  const t = profile.value?.createTime
  if (!t) return 0
  const start = new Date(String(t).replace(' ', 'T'))
  if (Number.isNaN(start.getTime())) return 0
  return Math.max(1, Math.floor((Date.now() - start.getTime()) / 86400000))
})

const form = ref({ nickname: '', communityNickname: '', wechat: '', email: '', signature: '' })

async function load() {
  loading.value = true
  editing.value = false
  try {
    profile.value = await getUserProfile(props.userId || userStore.user?.id)
  } catch (e) {
    profile.value = null
  } finally {
    loading.value = false
  }
}

watch(
  () => [props.modelValue, props.userId],
  ([open]) => {
    if (open) load()
    else profile.value = null
  }
)

function startEdit() {
  const p = profile.value || {}
  form.value = {
    nickname: p.nickname || '',
    communityNickname: p.communityNickname || '',
    wechat: p.wechat || '',
    email: p.email || '',
    signature: p.signature || ''
  }
  editing.value = true
}

async function save() {
  saving.value = true
  try {
    const updated = await updateMyProfile({ ...form.value })
    profile.value = updated
    editing.value = false
    // 同步顶栏展示名（昵称可能刚被改掉）
    userStore.setAuth(userStore.token, { ...userStore.user, ...updated })
    ElMessage.success('资料已保存')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.pf-loading { text-align: center; color: var(--gx-sub); padding: 24px 0; }
.pf-head { display: flex; align-items: center; gap: 14px; margin-bottom: 12px; }
.pf-avatar { background: var(--gx-celadon); color: #fff; font-weight: 700; font-size: 22px; }
.pf-name { font-family: var(--gx-font-serif); font-size: 18px; font-weight: 700; }
.pf-sub { font-size: 12.5px; color: var(--gx-sub); margin-top: 2px; }
.pf-sign {
  background: var(--gx-paper); border-radius: 8px; padding: 10px 12px;
  font-size: 13.5px; color: var(--gx-ink); line-height: 1.6; margin-bottom: 14px;
}
.pf-sign-empty { color: var(--gx-sub); }
.pf-stats { display: flex; gap: 28px; padding: 12px 0; border-top: 1px dashed var(--gx-divider); border-bottom: 1px dashed var(--gx-divider); margin-bottom: 12px; }
.pf-stat { display: flex; flex-direction: column; align-items: center; }
.pf-num { font-family: var(--gx-font-serif); font-size: 20px; font-weight: 700; color: var(--gx-vermillion); }
.pf-label { font-size: 12px; color: var(--gx-sub); }
.pf-row { display: flex; align-items: center; padding: 7px 0; font-size: 14px; }
.pf-k { width: 84px; color: var(--gx-sub); }
.pf-v { color: var(--gx-ink); }
.pf-tip { margin-top: 10px; font-size: 12px; color: var(--gx-sub); line-height: 1.6; }
.pf-hint { font-size: 12px; color: var(--gx-sub); margin-top: 4px; line-height: 1.5; }
.pf-form { padding-top: 6px; }
</style>

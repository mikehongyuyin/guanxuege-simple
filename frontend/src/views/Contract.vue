<template>
  <div class="contract-page">
    <div class="ct-head">
      <h3>家庭契约</h3>
      <el-button type="primary" class="gx-btn-primary" @click="openCreate">+ 新建契约</el-button>
    </div>

    <div class="ct-tip">
      <span class="ct-tip-ico">✍</span>
      和孩子郑重地约定一次：孩子承诺做到什么、家长承诺做到什么，达成有奖励、违约有约定。让规则成为双向的尊重。
    </div>

    <div v-if="contracts.length" class="ct-grid">
      <div v-for="c in contracts" :key="c.id" class="gx-card ct-card">
        <div class="ct-top">
          <span class="ct-title">{{ c.title }}</span>
          <el-tag size="small" :type="statusType(c.status)">{{ statusText(c.status) }}</el-tag>
        </div>
        <div class="ct-child">{{ childName(c.childId) }} · {{ c.startDate || '-' }} ~ {{ c.endDate || '-' }}</div>
        <div class="ct-promise">
          <div class="ct-p"><span class="ct-p-label">👦 孩子的承诺</span><span class="ct-p-text">{{ c.childPromise || '未填写' }}</span></div>
          <div class="ct-p"><span class="ct-p-label">👨 家长的承诺</span><span class="ct-p-text">{{ c.parentPromise || '未填写' }}</span></div>
        </div>
        <div class="ct-reward">
          <span v-if="c.reward" class="ct-rw">🏅 达成奖励：{{ c.reward }}</span>
          <span v-if="c.penalty" class="ct-rw ct-pt">⚠ 违约约定：{{ c.penalty }}</span>
        </div>
        <div class="ct-confirm">
          <el-tag v-if="c.childConfirm === 1" size="small" type="success">孩子已确认</el-tag>
          <el-tag v-else size="small" type="info">等待孩子确认</el-tag>
        </div>
        <div class="ct-ops">
          <el-button v-if="c.status === 0" size="small" text type="primary" @click="confirm(c)">孩子确认</el-button>
          <el-button v-if="c.status === 0" size="small" text type="success" @click="mark(c, 1)">达成</el-button>
          <el-button v-if="c.status === 0" size="small" text type="warning" @click="mark(c, 2)">违约</el-button>
          <el-button size="small" text type="danger" @click="remove(c)">删除</el-button>
        </div>
      </div>
    </div>
    <el-empty v-else description="还没有家庭契约，和孩子来一次郑重约定吧" />

    <el-dialog v-model="dialog" :title="'新建家庭契约'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="选择孩子" required>
          <el-select v-model="form.childId" placeholder="选择孩子" style="width: 100%">
            <el-option v-for="c in children" :key="c.id" :label="`${c.name}（${c.grade}）`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="契约标题" required>
          <el-input v-model="form.title" placeholder="如：早睡契约" />
        </el-form-item>
        <el-form-item label="孩子的承诺">
          <el-input v-model="form.childPromise" type="textarea" :rows="2" placeholder="孩子承诺做到…（如：每晚 9 点半前上床睡觉）" />
        </el-form-item>
        <el-form-item label="家长的承诺">
          <el-input v-model="form.parentPromise" type="textarea" :rows="2" placeholder="家长承诺做到…（如：睡前陪读 15 分钟，不催不吼）" />
        </el-form-item>
        <el-form-item label="达成奖励">
          <el-input v-model="form.reward" placeholder="如：周末去动物园 / 集满 7 天兑换一个小愿望" />
        </el-form-item>
        <el-form-item label="违约约定">
          <el-input v-model="form.penalty" placeholder="如：少看一天动画片" />
        </el-form-item>
        <el-form-item label="起止日期">
          <el-date-picker v-model="form.dates" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始" end-placeholder="结束" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" class="gx-btn-primary" @click="submit">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, inject, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { contractList, createContract, confirmContract, markContractStatus, deleteContract } from '@/api/contract'

const children = inject('children', ref([]))
const currentChild = inject('currentChild', ref(null))
const contracts = ref([])

function statusText(s) { return ['进行中', '已完成', '已违约'][s] || '' }
function statusType(s) { return ['primary', 'success', 'danger'][s] || 'info' }
function childName(id) {
  const c = children.value.find((x) => x.id === id)
  return c ? c.name : '孩子'
}

async function load() {
  contracts.value = await contractList(currentChild.value)
}
onMounted(load)

const dialog = ref(false)
const form = ref({ childId: null, title: '', childPromise: '', parentPromise: '', reward: '', penalty: '', dates: null })
function openCreate() {
  form.value = { childId: currentChild.value, title: '', childPromise: '', parentPromise: '', reward: '', penalty: '', dates: null }
  dialog.value = true
}
async function submit() {
  if (!form.value.childId) {
    ElMessage.warning('请选择孩子')
    return
  }
  if (!form.value.title.trim()) {
    ElMessage.warning('请填写契约标题')
    return
  }
  const payload = { ...form.value }
  if (payload.dates && payload.dates.length === 2) {
    payload.startDate = payload.dates[0]
    payload.endDate = payload.dates[1]
  }
  delete payload.dates
  await createContract(payload)
  dialog.value = false
  ElMessage.success('契约已创建，记得和孩子一起确认')
  await load()
}

async function confirm(c) {
  await confirmContract(c.id)
  c.childConfirm = 1
  ElMessage.success(`「${childName(c.childId)}」已确认契约`)
}
async function mark(c, status) {
  const tip = status === 1 ? '确认契约已达成？' : '确认契约违约？'
  try {
    await ElMessageBox.confirm(tip, '状态变更', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await markContractStatus(c.id, status)
  c.status = status
  ElMessage.success(status === 1 ? '恭喜！契约达成 🎉' : '已标记违约')
}
async function remove(c) {
  try {
    await ElMessageBox.confirm(`确定删除契约「${c.title}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deleteContract(c.id)
  contracts.value = contracts.value.filter((x) => x.id !== c.id)
  ElMessage.success('已删除')
}
</script>

<style scoped>
.ct-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.ct-head h3 { font-family: var(--gx-font-serif); font-size: 20px; margin: 0; }
.ct-tip {
  display: flex; align-items: flex-start; gap: 8px; background: var(--gx-warm-white);
  border: 1px solid var(--gx-divider); border-radius: var(--gx-radius); padding: 14px 16px;
  color: var(--gx-sub); font-size: 13px; line-height: 1.7; margin-bottom: 18px;
}
.ct-tip-ico { color: var(--gx-vermillion); font-weight: 700; }

.ct-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 16px; }
.ct-card { padding: 18px; }
.ct-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px; }
.ct-title { font-family: var(--gx-font-serif); font-size: 16px; font-weight: 700; }
.ct-child { color: var(--gx-ochre); font-size: 12px; margin-bottom: 10px; }
.ct-promise { border-top: 1px dashed var(--gx-divider); padding-top: 10px; display: flex; flex-direction: column; gap: 8px; margin-bottom: 10px; }
.ct-p { display: flex; gap: 10px; font-size: 13px; line-height: 1.6; }
.ct-p-label { flex-shrink: 0; color: var(--gx-vermillion); font-weight: 600; }
.ct-p-text { color: var(--gx-ink); }
.ct-reward { display: flex; flex-direction: column; gap: 4px; font-size: 12px; color: var(--gx-sub); margin-bottom: 10px; }
.ct-rw.ct-pt { color: var(--gx-ochre); }
.ct-confirm { margin-bottom: 8px; }
.ct-ops { border-top: 1px dashed var(--gx-divider); padding-top: 8px; display: flex; flex-wrap: wrap; gap: 2px; }

/* ===== 移动端适配（<768px） ===== */
@media (max-width: 767px) {
  .ct-head h3 { font-size: 18px; }
  .ct-grid { grid-template-columns: 1fr; }
  .ct-card { padding: 14px 12px; }
  .ct-p { flex-direction: column; gap: 2px; }
}
</style>

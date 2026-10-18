<template>
  <div class="plan-page">
    <!-- 概览卡片 -->
    <div class="ov-cards">
      <div class="ov-card"><div class="ov-num vermillion">{{ overview.todoCount }}</div><div class="ov-label">待开始</div></div>
      <div class="ov-card"><div class="ov-num celadon">{{ overview.doingCount }}</div><div class="ov-label">进行中</div></div>
      <div class="ov-card"><div class="ov-num ochre">{{ overview.doneCount }}</div><div class="ov-label">已完成</div></div>
      <div class="ov-card"><div class="ov-num ink">{{ overview.overallProgress }}%</div><div class="ov-label">整体进度</div></div>
    </div>

    <!-- 今日打卡汇总 -->
    <div class="today-bar" :class="{ done: todayPendingCount === 0 }" @click="openToday">
      <span class="tb-icon">☀</span>
      <span class="tb-text">今日打卡</span>
      <span v-if="todayPendingCount > 0" class="tb-num">还有 {{ todayPendingCount }} 项任务待打卡</span>
      <span v-else class="tb-num">今天的任务都打完卡啦，太棒了！</span>
      <el-button v-if="todayPendingCount > 0" type="primary" size="small" class="gx-btn-primary" @click.stop="openToday">去打卡</el-button>
      <el-button v-else type="success" size="small" plain @click.stop="openToday">查看</el-button>
    </div>

    <div class="board-head">
      <h3>计划看板</h3>
      <el-button type="primary" class="gx-btn-primary" @click="openCreate">+ 新建计划</el-button>
    </div>

    <!-- 看板：三列 -->
    <div class="board">
      <div v-for="col in columns" :key="col.status" class="board-col">
        <div class="col-title" :class="col.cls">{{ col.label }}（{{ plansByStatus(col.status).length }}）</div>
        <div v-for="p in plansByStatus(col.status)" :key="p.id" class="plan-card gx-card">
          <div class="pc-head">
            <span class="pc-title">{{ p.title }}</span>
            <el-tag size="small" effect="plain">{{ p.planType === 'WEEK' ? '周计划' : p.planType === 'DAY' ? '日计划' : '月计划' }}</el-tag>
          </div>
          <div class="pc-desc">{{ p.description }}</div>
          <div class="pc-meta">{{ p.startDate }} ~ {{ p.endDate }}</div>
          <div class="pc-progress">
            <el-progress :percentage="p.progress" :stroke-width="6" :color="col.color" />
          </div>
          <div class="pc-ops">
            <el-button v-if="p.status === 0" type="primary" size="small" class="gx-btn-primary" @click="startPlan(p)">开始计划</el-button>
            <el-button v-if="p.status === 1" type="success" size="small" @click="finishPlan(p)">完成计划</el-button>
          </div>
          <div v-if="p.tasks && p.tasks.length" class="pc-tasks">
            <div v-for="t in p.tasks" :key="t.id" class="task-row">
              <span v-if="t.subject" class="subject" :class="subjectCls(t.subject)">{{ t.subject }}</span>
              <span class="task-title">{{ t.title }}</span>
              <span v-if="t.checkin && t.checkin.streak > 0" class="task-streak">🔥 连续 {{ t.checkin.streak }} 天</span>
              <span class="task-status" :class="statusCls(t.status)">{{ taskStatusText(t.status) }}</span>
              <el-button
                v-if="p.status !== 2"
                size="small"
                :type="t.checkin && t.checkin.checkedToday ? 'success' : 'primary'"
                class="gx-btn-primary"
                :disabled="t.checkin && t.checkin.checkedToday"
                @click="openCheckin(t, p)"
              >
                {{ t.checkin && t.checkin.checkedToday ? '今日已打卡' : '打卡' }}
              </el-button>
            </div>
          </div>
        </div>
        <div v-if="plansByStatus(col.status).length === 0" class="empty">暂无计划</div>
      </div>
    </div>

    <!-- 新建计划弹窗 -->
    <el-dialog v-model="dialog" title="新建学习计划" width="460px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="选择孩子" required>
          <el-select v-model="form.childId" placeholder="选择孩子" style="width: 100%">
            <el-option v-for="c in children" :key="c.id" :label="`${c.name}（${c.grade}）`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划标题"><el-input v-model="form.title" placeholder="如：本周语文复习" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="form.description" type="textarea" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.planType"><el-option label="日计划" value="DAY" /><el-option label="周计划" value="WEEK" /><el-option label="月计划" value="MONTH" /></el-select>
        </el-form-item>
        <el-form-item label="任务列表">
          <el-input
            v-model="form.tasksText"
            type="textarea"
            :rows="4"
            placeholder="每行一个任务，例如：&#10;每天专注阅读 20 分钟&#10;抄写 10 个生字"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" class="gx-btn-primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 打卡弹窗 -->
    <el-dialog v-model="checkinDialog" :title="`打卡：${checkinTask?.title || ''}`" width="420px">
      <el-input v-model="checkinNote" type="textarea" :rows="3" maxlength="200" placeholder="记录一下今天完成的情况（可选）" />
      <template #footer>
        <el-button @click="checkinDialog = false">取消</el-button>
        <el-button type="primary" class="gx-btn-primary" @click="doCheckin">完成打卡</el-button>
      </template>
    </el-dialog>

    <!-- 今日打卡弹窗 -->
    <el-dialog v-model="todayDialog" title="今日打卡" width="520px">
      <div v-if="todayPending.length === 0" class="today-done">今天所有任务都已打卡，太棒了！</div>
      <div v-else class="today-list">
        <div v-for="item in todayPending" :key="item.task.id" class="today-row">
          <div class="today-info">
            <span class="today-plan">{{ item.planTitle }}</span>
            <span class="today-task">{{ item.task.title }}</span>
          </div>
          <el-button type="primary" size="small" class="gx-btn-primary" @click="doTodayCheckin(item)">打卡</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, inject, watch, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { planOverview, planList, createPlan, createPlanTask, updatePlan, planTasks, taskCheckin, taskCheckinStatus } from '@/api/plan'

const currentChild = inject('currentChild', ref(null))
const children = inject('children', ref([]))
const overview = ref({ todoCount: 0, doingCount: 0, doneCount: 0, taskTotal: 0, taskDone: 0, overallProgress: 0 })
const plans = ref([])

const columns = [
  { status: 0, label: '待开始', cls: 'c-todo', color: '#8A6440' },
  { status: 1, label: '进行中', cls: 'c-doing', color: '#205C49' },
  { status: 2, label: '已完成', cls: 'c-done', color: '#B5392E' }
]

function plansByStatus(s) {
  return plans.value.filter((p) => p.status === s)
}
function subjectCls(s) {
  return { 语文: 's-yw', 数学: 's-sx', 英语: 's-yy', 科学: 's-kx' }[s] || 's-other'
}
function statusCls(s) { return ['st-todo', 'st-doing', 'st-done'][s] || '' }
function taskStatusText(s) { return ['待开始', '进行中', '已完成'][s] || '' }

async function load() {
  if (!currentChild.value) return
  overview.value = await planOverview(currentChild.value)
  const list = await planList(currentChild.value)
  // 逐计划加载任务与打卡状态
  for (const p of list) {
    try {
      const tasks = await planTasks(p.id)
      for (const t of tasks) {
        try {
          t.checkin = await taskCheckinStatus(t.id, currentChild.value)
        } catch (e) {
          t.checkin = null
        }
      }
      p.tasks = tasks
    } catch (e) {
      p.tasks = []
    }
  }
  plans.value = list
}
watch(currentChild, load)
onMounted(load)

const dialog = ref(false)
const form = ref({ childId: null, title: '', description: '', planType: 'WEEK', tasksText: '' })
function openCreate() {
  form.value = { childId: currentChild.value, title: '', description: '', planType: 'WEEK', tasksText: '' }
  dialog.value = true
}
async function submit() {
  if (!form.value.childId) {
    ElMessage.warning('请先选择孩子')
    return
  }
  const plan = await createPlan({ ...form.value, status: 0, progress: 0 })
  // 解析任务列表，逐行创建任务
  const lines = (form.value.tasksText || '').split('\n').map((s) => s.trim()).filter(Boolean)
  for (const t of lines) {
    await createPlanTask({ planId: plan.id, title: t, status: 0, progress: 0 })
  }
  dialog.value = false
  const target = children.value.find((c) => c.id === form.value.childId)
  if (target && target.id !== currentChild.value) {
    ElMessage.success(`已为「${target.name}」创建计划，可在顶部切换孩子查看`)
  } else if (lines.length > 0) {
    ElMessage.success(`计划已创建，共 ${lines.length} 项任务，快去打卡吧`)
  }
  await load()
}

async function startPlan(p) {
  await updatePlan({ ...p, status: 1 })
  ElMessage.success('计划已开始，记得每天打卡')
  await load()
}
async function finishPlan(p) {
  await updatePlan({ ...p, status: 2, progress: 100 })
  ElMessage.success('计划已完成')
  await load()
}

// ===== 任务打卡 =====
const checkinDialog = ref(false)
const checkinTask = ref(null)
const checkinNote = ref('')
async function openCheckin(t, p) {
  // 待开始计划：点击打卡时自动开始
  if (p && p.status === 0) {
    await updatePlan({ ...p, status: 1 })
    p.status = 1
    ElMessage.success('计划已自动开始，今天起每天打卡')
  }
  checkinTask.value = t
  checkinNote.value = ''
  checkinDialog.value = true
}
async function doCheckin() {
  await taskCheckin(checkinTask.value.id, currentChild.value, checkinNote.value.trim())
  checkinDialog.value = false
  ElMessage.success('打卡成功，坚持就是胜利！')
  await load()
}

// ===== 今日打卡汇总 =====
const todayDialog = ref(false)
const todayPending = computed(() => {
  const arr = []
  for (const p of plans.value) {
    if (p.status === 2 || !p.tasks) continue
    for (const t of p.tasks) {
      if (t.checkin && t.checkin.checkedToday) continue
      arr.push({ planTitle: p.title, task: t })
    }
  }
  return arr
})
const todayPendingCount = computed(() => todayPending.value.length)
function openToday() {
  todayDialog.value = true
}
async function doTodayCheckin(item) {
  await taskCheckin(item.task.id, currentChild.value, '')
  ElMessage.success(`「${item.task.title}」打卡成功`)
  await load()
}
</script>

<style scoped>
.ov-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
.ov-card { background: var(--gx-warm-white); border: 1px solid var(--gx-divider); border-radius: var(--gx-radius); padding: 20px; text-align: center; }
.ov-num { font-family: var(--gx-font-serif); font-size: 32px; font-weight: 700; }
.ov-num.vermillion { color: var(--gx-vermillion); } .ov-num.celadon { color: var(--gx-celadon); }
.ov-num.ochre { color: var(--gx-ochre); } .ov-num.ink { color: var(--gx-ink); }
.ov-label { color: var(--gx-sub); font-size: 13px; margin-top: 6px; }

.board-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.board-head h3 { font-family: var(--gx-font-serif); font-size: 20px; margin: 0; }
.board { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; align-items: start; }
.col-title { font-family: var(--gx-font-serif); font-size: 16px; font-weight: 700; padding: 10px 14px; border-radius: 8px 8px 0 0; color: #fff; }
.col-title.c-todo { background: var(--gx-ochre); } .col-title.c-doing { background: var(--gx-celadon); } .col-title.c-done { background: var(--gx-vermillion); }
.plan-card { border-radius: 0 0 12px 12px; padding: 16px; margin-bottom: 14px; }
.pc-head { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.pc-title { font-weight: 700; font-size: 15px; }
.pc-desc { color: var(--gx-sub); font-size: 13px; margin: 8px 0; line-height: 1.6; }
.pc-meta { color: var(--gx-ochre); font-size: 12px; margin-bottom: 10px; }
.pc-progress { margin-bottom: 10px; }
.pc-tasks { border-top: 1px dashed var(--gx-divider); padding-top: 10px; }
.task-row { display: flex; align-items: center; gap: 8px; padding: 6px 0; font-size: 13px; flex-wrap: wrap; }
.subject { padding: 2px 8px; border-radius: 6px; font-size: 12px; color: #fff; }
.s-yw { background: var(--gx-vermillion); } .s-sx { background: var(--gx-celadon); } .s-yy { background: var(--gx-ochre); } .s-kx { background: #4a6f8a; } .s-other { background: #9b8a6f; }
.task-title { flex: 1; color: var(--gx-ink); }
.task-streak { font-size: 12px; color: #D97706; }
.task-status { font-size: 12px; color: var(--gx-sub); }
.task-status.st-doing { color: var(--gx-celadon); } .task-status.st-done { color: var(--gx-vermillion); }
.empty { color: var(--gx-sub); font-size: 13px; text-align: center; padding: 20px; }

.today-bar { display: flex; align-items: center; gap: 12px; background: var(--gx-warm-white); border: 1px solid var(--gx-divider); border-radius: var(--gx-radius); padding: 14px 18px; margin-bottom: 16px; cursor: pointer; }
.today-bar.done { border-color: #B7D7C8; }
.tb-icon { font-size: 20px; }
.tb-text { font-weight: 700; font-size: 15px; }
.tb-num { flex: 1; color: var(--gx-ochre); font-size: 14px; }
.today-bar.done .tb-num { color: var(--gx-celadon); }
.today-done { text-align: center; color: var(--gx-celadon); padding: 24px 0; font-size: 14px; }
.today-list { max-height: 46vh; overflow-y: auto; }
.today-row { display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 10px 4px; border-bottom: 1px dashed var(--gx-divider); }
.today-row:last-child { border-bottom: none; }
.today-info { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.today-plan { font-size: 12px; color: var(--gx-sub); }
.today-task { font-size: 14px; color: var(--gx-ink); }

/* ===== 移动端适配（<768px） ===== */
@media (max-width: 767px) {
  .ov-cards { grid-template-columns: repeat(2, 1fr); gap: 12px; margin-bottom: 16px; }
  .ov-card { padding: 14px 10px; }
  .ov-num { font-size: 26px; }
  .board { grid-template-columns: 1fr; gap: 14px; }
  .board-head h3 { font-size: 18px; }
  .plan-card { padding: 14px 12px; }
  .task-row { gap: 6px; }
  .today-bar { flex-wrap: wrap; gap: 8px; padding: 12px 14px; }
  .tb-num { flex-basis: 100%; }
  .today-bar .el-button { margin-left: auto; }
}
</style>

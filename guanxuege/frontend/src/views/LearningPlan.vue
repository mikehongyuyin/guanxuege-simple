<template>
  <div class="plan-page">
    <!-- 概览卡片 -->
    <div class="ov-cards">
      <div class="ov-card"><div class="ov-num vermillion">{{ overview.todoCount }}</div><div class="ov-label">待开始</div></div>
      <div class="ov-card"><div class="ov-num celadon">{{ overview.doingCount }}</div><div class="ov-label">进行中</div></div>
      <div class="ov-card"><div class="ov-num ochre">{{ overview.doneCount }}</div><div class="ov-label">已完成</div></div>
      <div class="ov-card"><div class="ov-num ink">{{ overview.overallProgress }}%</div><div class="ov-label">整体进度</div></div>
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
          <div class="pc-tasks">
            <div v-for="t in p.tasks" :key="t.id" class="task-row">
              <span class="subject" :class="subjectCls(t.subject)">{{ t.subject }}</span>
              <span class="task-title">{{ t.title }}</span>
              <span class="task-status" :class="statusCls(t.status)">{{ taskStatusText(t.status) }}</span>
            </div>
          </div>
        </div>
        <div v-if="plansByStatus(col.status).length === 0" class="empty">暂无计划</div>
      </div>
    </div>

    <!-- 新建计划弹窗 -->
    <el-dialog v-model="dialog" title="新建学习计划" width="460px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="计划标题"><el-input v-model="form.title" placeholder="如：本周语文复习" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="form.description" type="textarea" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.planType"><el-option label="日计划" value="DAY" /><el-option label="周计划" value="WEEK" /><el-option label="月计划" value="MONTH" /></el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" class="gx-btn-primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, inject, watch, onMounted } from 'vue'
import { planOverview, planList, createPlan } from '@/api/plan'

const currentChild = inject('currentChild', ref(null))
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
  plans.value = await planList(currentChild.value)
}
watch(currentChild, load)
onMounted(load)

const dialog = ref(false)
const form = ref({ title: '', description: '', planType: 'WEEK' })
function openCreate() { dialog.value = true }
async function submit() {
  await createPlan({ ...form.value, childId: currentChild.value, status: 0, progress: 0 })
  dialog.value = false
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
.task-row { display: flex; align-items: center; gap: 8px; padding: 6px 0; font-size: 13px; }
.subject { padding: 2px 8px; border-radius: 6px; font-size: 12px; color: #fff; }
.s-yw { background: var(--gx-vermillion); } .s-sx { background: var(--gx-celadon); } .s-yy { background: var(--gx-ochre); } .s-kx { background: #4a6f8a; } .s-other { background: #9b8a6f; }
.task-title { flex: 1; color: var(--gx-ink); }
.task-status { font-size: 12px; color: var(--gx-sub); }
.task-status.st-doing { color: var(--gx-celadon); } .task-status.st-done { color: var(--gx-vermillion); }
.empty { color: var(--gx-sub); font-size: 13px; text-align: center; padding: 20px; }
</style>

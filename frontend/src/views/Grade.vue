<template>
  <div class="grade-page">
    <!-- 概览卡片 -->
    <div class="ov-cards">
      <div class="ov-card"><div class="ov-num ink">{{ overview.overallAverage }}</div><div class="ov-label">总平均分</div></div>
      <div v-for="s in overview.subjectStats || []" :key="s.subject" class="ov-card">
        <div class="ov-num" :style="{ color: subjectColor(s.subject) }">{{ s.average }}</div>
        <div class="ov-label">{{ s.subject }}平均</div>
      </div>
    </div>

    <!-- 图表卡 -->
    <div class="gx-card chart-card">
      <h3>各科得分率</h3>
      <div class="bars">
        <div v-for="s in overview.subjectStats || []" :key="s.subject" class="bar-col">
          <div class="bar-wrap"><div class="bar" :style="{ height: s.rate + '%', background: subjectColor(s.subject) }"></div></div>
          <div class="bar-rate">{{ s.rate }}%</div>
          <div class="bar-label">{{ s.subject }}</div>
        </div>
      </div>
    </div>

    <!-- 科目明细卡 -->
    <div class="subject-cards">
      <div v-for="s in overview.subjectStats || []" :key="s.subject" class="gx-card sc">
        <div class="sc-head"><span class="sc-name">{{ s.subject }}</span><span class="sc-avg">{{ s.average }}<small>/{{ s.fullScore }}</small></span></div>
        <el-progress :percentage="s.rate" :stroke-width="8" :color="subjectColor(s.subject)" />
        <div class="sc-foot">共 {{ s.count }} 次考试</div>
      </div>
    </div>

    <!-- 成绩明细表 -->
    <div class="gx-card table-card">
      <div class="table-head">
        <h3>成绩明细</h3>
        <el-button type="primary" class="gx-btn-primary" @click="openAdd">+ 添加成绩</el-button>
      </div>
      <el-table :data="grades" style="width: 100%">
        <el-table-column prop="subject" label="科目" />
        <el-table-column prop="examName" label="考试" />
        <el-table-column label="得分">
          <template #default="{ row }">{{ row.score }} / {{ row.fullScore }}</template>
        </el-table-column>
        <el-table-column label="得分率">
          <template #default="{ row }">{{ Math.round((row.score / row.fullScore) * 100) }}%</template>
        </el-table-column>
        <el-table-column prop="examDate" label="日期" />
        <el-table-column prop="rankInClass" label="班排名" />
        <el-table-column label="操作" width="90">
          <template #default="{ row }">
            <el-button text type="danger" size="small" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 添加成绩弹窗 -->
    <el-dialog v-model="dialog" title="添加成绩" width="460px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="选择孩子" required>
          <el-select v-model="form.childId" placeholder="选择孩子" style="width: 100%">
            <el-option v-for="c in children" :key="c.id" :label="`${c.name}（${c.grade}）`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="科目" required>
          <el-select v-model="form.subject" placeholder="选择科目" style="width: 100%">
            <el-option v-for="s in subjects" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试名称">
          <el-input v-model="form.examName" placeholder="如：期中、期末" maxlength="50" />
        </el-form-item>
        <el-form-item label="得分" required>
          <el-input-number v-model="form.score" :min="0" :max="999" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="满分">
          <el-input-number v-model="form.fullScore" :min="1" :max="999" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="考试日期">
          <el-date-picker v-model="form.examDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="班排名">
          <el-input-number v-model="form.rankInClass" :min="1" :max="999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="评语">
          <el-input v-model="form.comment" type="textarea" :rows="2" maxlength="200" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { gradeOverview, gradeList, createGrade, deleteGrade } from '@/api/grade'

const currentChild = inject('currentChild', ref(null))
const children = inject('children', ref([]))
const overview = ref({ overallAverage: 0, subjectStats: [] })
const grades = ref([])

const subjects = ['语文', '数学', '英语', '科学']
const colorMap = { 语文: '#B5392E', 数学: '#205C49', 英语: '#8A6440', 科学: '#4a6f8a' }
function subjectColor(s) { return colorMap[s] || '#9b8a6f' }

async function load() {
  if (!currentChild.value) return
  overview.value = await gradeOverview(currentChild.value)
  grades.value = await gradeList(currentChild.value)
}
watch(currentChild, load)
onMounted(load)

const dialog = ref(false)
const form = ref({ childId: null, subject: '语文', examName: '', score: null, fullScore: 100, examDate: '', rankInClass: null, comment: '' })
function openAdd() {
  form.value = { childId: currentChild.value, subject: '语文', examName: '', score: null, fullScore: 100, examDate: '', rankInClass: null, comment: '' }
  dialog.value = true
}
async function submit() {
  if (!form.value.childId) {
    ElMessage.warning('请先选择孩子')
    return
  }
  if (form.value.score === null || form.value.score === undefined) {
    ElMessage.warning('请输入得分')
    return
  }
  await createGrade(form.value)
  dialog.value = false
  const target = children.value.find((c) => c.id === form.value.childId)
  if (target && target.id !== currentChild.value) {
    ElMessage.success(`已为「${target.name}」录入成绩，可在顶部切换孩子查看`)
  }
  await load()
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除这次「${row.subject}·${row.examName}」成绩吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deleteGrade(row.id)
  ElMessage.success('已删除')
  await load()
}
</script>

<style scoped>
.ov-cards { display: grid; grid-template-columns: repeat(5, 1fr); gap: 16px; margin-bottom: 20px; }
.ov-card { background: var(--gx-warm-white); border: 1px solid var(--gx-divider); border-radius: var(--gx-radius); padding: 18px; text-align: center; }
.ov-num { font-family: var(--gx-font-serif); font-size: 28px; font-weight: 700; color: var(--gx-ink); }
.ov-label { color: var(--gx-sub); font-size: 13px; margin-top: 6px; }

.chart-card { padding: 22px; margin-bottom: 20px; }
.chart-card h3 { font-family: var(--gx-font-serif); font-size: 18px; margin: 0 0 18px; }
.bars { display: flex; gap: 28px; align-items: flex-end; height: 220px; padding: 0 10px; }
.bar-col { flex: 1; display: flex; flex-direction: column; align-items: center; height: 100%; justify-content: flex-end; }
.bar-wrap { flex: 1; width: 46px; display: flex; align-items: flex-end; background: var(--gx-muted); border-radius: 8px 8px 0 0; overflow: hidden; }
.bar { width: 100%; border-radius: 8px 8px 0 0; transition: height .4s; }
.bar-rate { font-size: 13px; color: var(--gx-sub); margin: 8px 0 4px; }
.bar-label { font-size: 14px; font-family: var(--gx-font-serif); }

.subject-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px; }
.sc { padding: 18px; }
.sc-head { display: flex; align-items: baseline; justify-content: space-between; margin-bottom: 12px; }
.sc-name { font-family: var(--gx-font-serif); font-size: 16px; font-weight: 700; }
.sc-avg { font-size: 22px; font-weight: 700; color: var(--gx-ink); }
.sc-avg small { font-size: 13px; color: var(--gx-sub); font-weight: 400; }
.sc-foot { color: var(--gx-sub); font-size: 12px; margin-top: 10px; }

.table-card { padding: 22px; }
.table-card h3 { font-family: var(--gx-font-serif); font-size: 18px; margin: 0; }
.table-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
</style>

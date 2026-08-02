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
      <h3>成绩明细</h3>
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
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, inject, watch, onMounted } from 'vue'
import { gradeOverview, gradeList } from '@/api/grade'

const currentChild = inject('currentChild', ref(null))
const overview = ref({ overallAverage: 0, subjectStats: [] })
const grades = ref([])

const colorMap = { 语文: '#B5392E', 数学: '#205C49', 英语: '#8A6440', 科学: '#4a6f8a' }
function subjectColor(s) { return colorMap[s] || '#9b8a6f' }

async function load() {
  if (!currentChild.value) return
  overview.value = await gradeOverview(currentChild.value)
  grades.value = await gradeList(currentChild.value)
}
watch(currentChild, load)
onMounted(load)
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
.table-card h3 { font-family: var(--gx-font-serif); font-size: 18px; margin: 0 0 16px; }
</style>

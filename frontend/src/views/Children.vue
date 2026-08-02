<template>
  <div class="children-page">
    <div class="ch-head">
      <h3>孩子档案</h3>
      <el-button type="primary" class="gx-btn-primary" @click="openAdd">+ 添加孩子</el-button>
    </div>

    <div v-if="children.length" class="ch-grid">
      <div v-for="c in children" :key="c.id" class="gx-card ch-card">
        <div class="ch-top">
          <el-avatar :size="44" class="ch-avatar">{{ (c.name || '宝').charAt(0) }}</el-avatar>
          <div class="ch-info">
            <div class="ch-name">{{ c.name }}</div>
            <el-tag size="small" effect="plain">{{ c.grade || '未设置年级' }}</el-tag>
          </div>
        </div>
        <div class="ch-school">{{ c.school || '未填写学校' }}</div>
        <div class="ch-ops">
          <el-button text type="primary" size="small" @click="openEdit(c)">编辑</el-button>
          <el-button text type="danger" size="small" @click="onDelete(c)">删除</el-button>
        </div>
      </div>
    </div>
    <el-empty v-else description="还没有孩子档案，点击右上角添加" />

    <el-dialog v-model="dialog" :title="editing ? '编辑孩子' : '添加孩子'" width="440px">
      <el-form :model="form" label-width="70px">
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" placeholder="请输入孩子姓名" maxlength="20" />
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="form.grade" placeholder="选择年级" style="width: 100%">
            <el-option v-for="g in grades" :key="g" :label="g" :value="g" />
          </el-select>
        </el-form-item>
        <el-form-item label="学校">
          <el-input v-model="form.school" placeholder="请输入学校名称" maxlength="50" />
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
import { ref, inject, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getChildren, createChild, updateChild, deleteChild } from '@/api/auth'

const children = inject('children', ref([]))
const grades = ['一年级', '二年级', '三年级', '四年级', '五年级', '六年级']

const dialog = ref(false)
const editing = ref(false)
const form = ref({ id: null, name: '', grade: '', school: '' })

async function ensureLoaded() {
  if (!children.value.length) {
    try {
      children.value = await getChildren()
    } catch (e) {
      /* 加载失败时保持空列表 */
    }
  }
}
onMounted(ensureLoaded)

function openAdd() {
  editing.value = false
  form.value = { id: null, name: '', grade: '', school: '' }
  dialog.value = true
}

function openEdit(c) {
  editing.value = true
  form.value = { ...c }
  dialog.value = true
}

async function submit() {
  if (!form.value.name.trim()) {
    ElMessage.warning('请输入孩子姓名')
    return
  }
  try {
    if (editing.value) {
      const updated = await updateChild(form.value)
      const i = children.value.findIndex((x) => x.id === updated.id)
      if (i >= 0) children.value.splice(i, 1, updated)
      ElMessage.success('已保存修改')
    } else {
      const created = await createChild(form.value)
      children.value.push(created)
      ElMessage.success('添加成功')
    }
    dialog.value = false
  } catch (e) {
    /* 具体错误已由 request 拦截器统一提示 */
  }
}

async function onDelete(c) {
  try {
    await ElMessageBox.confirm(`确定删除「${c.name}」吗？其学习计划、成绩等数据将一并隐藏。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deleteChild(c.id)
  children.value = children.value.filter((x) => x.id !== c.id)
  ElMessage.success('已删除')
}
</script>

<style scoped>
.ch-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 18px; }
.ch-head h3 { font-family: var(--gx-font-serif); font-size: 20px; margin: 0; }

.ch-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 16px; }
.ch-card { padding: 18px; }
.ch-top { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.ch-avatar { background: var(--gx-celadon); color: #fff; font-weight: 700; font-size: 18px; }
.ch-info { display: flex; flex-direction: column; gap: 6px; }
.ch-name { font-family: var(--gx-font-serif); font-size: 17px; font-weight: 700; color: var(--gx-ink); }
.ch-school { color: var(--gx-sub); font-size: 13px; margin-bottom: 12px; }
.ch-ops { border-top: 1px dashed var(--gx-divider); padding-top: 10px; display: flex; gap: 4px; }
</style>

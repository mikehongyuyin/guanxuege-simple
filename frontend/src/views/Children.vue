<template>
  <div class="children-page">
    <!-- 家庭档案 -->
    <div class="gx-card fam-card">
      <div class="fam-head">
        <h3>家庭档案</h3>
        <el-button type="primary" class="gx-btn-primary" @click="saveFamily">{{ family ? '保存家庭档案' : '创建家庭档案' }}</el-button>
      </div>
      <div class="fam-body">
        <el-form :inline="true" :model="familyForm" label-width="90px">
          <el-form-item label="家庭昵称">
            <el-input v-model="familyForm.familyName" placeholder="如：小满一家" style="width: 200px" />
          </el-form-item>
          <el-form-item label="家庭类型">
            <el-select v-model="familyForm.familyType" style="width: 200px">
              <el-option v-for="t in familyTypes" :key="t" :label="t" :value="t" />
            </el-select>
          </el-form-item>
          <el-form-item label="家风宣言">
            <el-input v-model="familyForm.motto" placeholder="一句话写下你们的家风" style="width: 340px" />
          </el-form-item>
        </el-form>
      </div>
    </div>

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
        <div class="ch-tags">
          <span v-if="c.gender" class="ch-tag">{{ c.gender === 1 ? '👦 男孩' : '👧 女孩' }}</span>
          <span v-if="c.birthday" class="ch-tag">🎂 {{ c.birthday }}</span>
        </div>
        <div v-if="c.personality" class="ch-line">性格：{{ c.personality }}</div>
        <div v-if="c.interests" class="ch-line">兴趣：{{ c.interests }}</div>
        <div class="ch-ops">
          <el-button text type="primary" size="small" @click="openEdit(c)">编辑</el-button>
          <el-button text type="danger" size="small" @click="onDelete(c)">删除</el-button>
        </div>
      </div>
    </div>
    <el-empty v-else description="还没有孩子档案，点击右上角添加" />

    <el-dialog v-model="dialog" :title="editing ? '编辑孩子' : '添加孩子'" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" placeholder="请输入孩子姓名" maxlength="20" />
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="form.grade" placeholder="选择年级" style="width: 100%">
            <el-option v-for="g in grades" :key="g" :label="g" :value="g" />
          </el-select>
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :label="1">男孩</el-radio>
            <el-radio :label="2">女孩</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期">
          <el-date-picker v-model="form.birthday" type="date" value-format="YYYY-MM-DD" placeholder="选择出生日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="学校">
          <el-input v-model="form.school" placeholder="选填，如：实验小学" maxlength="50" />
        </el-form-item>
        <el-form-item label="性格特点">
          <el-input v-model="form.personality" placeholder="如：好动、好奇心强（逗号分隔）" maxlength="100" />
        </el-form-item>
        <el-form-item label="兴趣特长">
          <el-input v-model="form.interests" placeholder="如：画画、游泳（逗号分隔）" maxlength="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" class="gx-btn-primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 监护人声明 + 儿童信息专项同意（添加孩子前强制） -->
    <el-dialog
      v-model="consentDialog"
      title="监护人声明与儿童信息同意"
      width="480px"
      :close-on-click-modal="false"
      :show-close="false"
    >
      <p class="consent-tip">
        为保障儿童个人信息安全，添加孩子前请确认您为孩子的监护人，并阅读专项规则。我们仅收集必要信息，学校等字段均可不填。
      </p>
      <el-form label-width="90px">
        <el-form-item label="监护关系" required>
          <el-select v-model="consentForm.guardianRelation" placeholder="请选择您与孩子的关系" style="width: 100%">
            <el-option label="父母" value="父母" />
            <el-option label="祖父母" value="祖父母" />
            <el-option label="其他监护人" value="其他监护人" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="consentForm.agreed">
            <span class="agree-text">我已阅读并同意
              <router-link to="/child-protection" target="_blank" class="lnk">《儿童个人信息保护规则》</router-link>
            </span>
          </el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="consentDialog = false">取消</el-button>
        <el-button
          type="primary"
          class="gx-btn-primary"
          :disabled="!(consentForm.guardianRelation && consentForm.agreed)"
          @click="confirmConsent"
        >同意并继续</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, inject, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getChildren, createChild, updateChild, deleteChild, getFamily, createFamily, updateFamily } from '@/api/auth'

const children = inject('children', ref([]))
const grades = ['一年级', '二年级', '三年级', '四年级', '五年级', '六年级']
const familyTypes = ['核心家庭', '二胎家庭', '单亲家庭', '隔代养育', '其他']

// ===== 家庭档案 =====
const family = ref(null)
const familyForm = ref({ familyName: '', familyType: '核心家庭', motto: '' })
async function loadFamily() {
  const f = await getFamily()
  if (f) {
    family.value = f
    familyForm.value = { familyName: f.familyName || '', familyType: f.familyType || '核心家庭', motto: f.motto || '' }
  }
}
async function saveFamily() {
  try {
    if (family.value) {
      family.value = await updateFamily({ ...familyForm.value })
    } else {
      family.value = await createFamily(familyForm.value)
    }
    ElMessage.success('家庭档案已保存')
  } catch (e) {
    ElMessage.error(e?.message || '家庭档案保存失败，请稍后重试')
  }
}

const dialog = ref(false)
const editing = ref(false)
const form = ref({ id: null, name: '', grade: '', school: '', birthday: null, gender: null, personality: '', interests: '' })

// ===== 监护人同意（添加孩子前强制） =====
const consentDialog = ref(false)
const consentForm = ref({ guardianRelation: '', agreed: false })
const pendingConsent = ref(null)

async function ensureLoaded() {
  if (!children.value.length) {
    try {
      children.value = await getChildren()
    } catch (e) {
      /* 加载失败时保持空列表 */
    }
  }
}
onMounted(async () => {
  await ensureLoaded()
  try {
    await loadFamily()
  } catch (e) {
    /* 家庭档案未创建时忽略 */
  }
})

function openAdd() {
  // 先弹出监护人同意，确认后再打开填写表单
  consentForm.value = { guardianRelation: '', agreed: false }
  pendingConsent.value = null
  consentDialog.value = true
}

function confirmConsent() {
  if (!consentForm.value.guardianRelation || !consentForm.value.agreed) return
  pendingConsent.value = {
    guardianRelation: consentForm.value.guardianRelation,
    consentVersion: '1.0',
    consentAgreed: true
  }
  consentDialog.value = false
  editing.value = false
  form.value = { id: null, name: '', grade: '', school: '', birthday: null, gender: null, personality: '', interests: '' }
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
      const created = await createChild({
        name: form.value.name,
        grade: form.value.grade,
        school: form.value.school,
        birthday: form.value.birthday,
        gender: form.value.gender,
        personality: form.value.personality,
        interests: form.value.interests,
        guardianRelation: pendingConsent.value?.guardianRelation || '父母',
        consentVersion: '1.0',
        consentAgreed: true
      })
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
.fam-card { padding: 18px; margin-bottom: 20px; }
.fam-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.fam-head h3 { font-family: var(--gx-font-serif); font-size: 18px; margin: 0; }
.fam-body :deep(.el-form-item) { margin-bottom: 10px; }

.ch-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 18px; }
.ch-head h3 { font-family: var(--gx-font-serif); font-size: 20px; margin: 0; }

.ch-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 16px; }
.ch-card { padding: 18px; }
.ch-top { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.ch-avatar { background: var(--gx-celadon); color: #fff; font-weight: 700; font-size: 18px; }
.ch-info { display: flex; flex-direction: column; gap: 6px; }
.ch-name { font-family: var(--gx-font-serif); font-size: 17px; font-weight: 700; color: var(--gx-ink); }
.ch-school { color: var(--gx-sub); font-size: 13px; margin-bottom: 8px; }
.ch-tags { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 8px; }
.ch-tag { font-size: 12px; color: var(--gx-sub); background: rgba(0,0,0,.04); padding: 2px 8px; border-radius: 8px; }
.ch-line { font-size: 13px; color: var(--gx-sub); line-height: 1.6; margin-bottom: 4px; }
.ch-ops { border-top: 1px dashed var(--gx-divider); padding-top: 10px; display: flex; gap: 4px; }

/* ===== 移动端适配（<768px） ===== */
@media (max-width: 767px) {
  .fam-card { padding: 14px 12px; }
  .fam-head { flex-wrap: wrap; gap: 8px; }
  /* 内联表单改为整行堆叠，输入框占满（覆盖内联 200/340px 宽度） */
  .fam-body :deep(.el-form-item) { display: flex; width: 100%; margin-right: 0; }
  .fam-body :deep(.el-input), .fam-body :deep(.el-select) { width: 100% !important; }
  .ch-head h3 { font-size: 18px; }
  .ch-grid { grid-template-columns: 1fr; }
  .ch-card { padding: 14px 12px; }
}
</style>

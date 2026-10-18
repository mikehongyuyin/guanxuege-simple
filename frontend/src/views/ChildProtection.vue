<template>
  <div class="doc-page">
    <div class="doc-card">
      <h1>儿童个人信息保护规则</h1>
      <p class="doc-meta">版本：1.0 ｜ 生效日期：2026-09-15 ｜ 依据《个人信息保护法》《未成年人保护法》《儿童个人信息网络保护规定》</p>

      <h2>一、规则的适用</h2>
      <p>当您在本平台<b>添加或绑定孩子档案</b>时，即表示您作为孩子的监护人，已阅读并同意本规则。本规则是《隐私政策》关于儿童信息的专项说明，二者不一致时以本规则为准。</p>

      <h2>二、监护人身份与关系声明</h2>
      <p>在绑定孩子前，请您声明与孩子的监护关系（父母 / 祖父母 / 其他监护人），并确认您有权代表被监护人作出同意。我们仅依据该专项同意处理儿童信息。</p>

      <h2>三、我们收集的儿童信息</h2>
      <ul>
        <li>姓名、年级（提供服务所必需）；</li>
        <li>学校、出生日期、性别、性格与兴趣标签（<b>选填</b>，您可留空，不影响核心功能）。</li>
      </ul>
      <p>我们承诺遵循<b>最小必要</b>原则，不收集与育儿管理无关的未成年人信息。</p>

      <h2>四、儿童信息的使用与展示</h2>
      <p>儿童信息仅用于：为您生成学习计划与成绩分析、在您本人账户内展示、以及在您主动发布社区内容时按您的选择脱敏展示。我们<b>不会</b>将可识别的儿童个人信息用于广告推送或对外提供。</p>

      <h2>五、您的控制权</h2>
      <ul>
        <li><b>撤回同意</b>：您可随时撤回对儿童信息的专项同意，撤回后我们将停止处理并隐藏该孩子数据；</li>
        <li><b>删除</b>：您可删除孩子档案，其关联的学习计划、成绩、契约等数据将一并隐藏；</li>
        <li><b>更正</b>：您可随时修改孩子档案内容。</li>
      </ul>

      <h2>六、保存期限</h2>
      <p>我们仅在为您及您的孩子提供服务的必要期限内保存儿童信息；您撤回同意或删除后，我们将在合理期限内停止处理并删除或匿名化相关数据。</p>

      <p class="doc-contact">儿童信息保护联系：privacy@guanxuege.cn</p>
      <el-button class="doc-back" @click="goBack">返回</el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
function goBack() {
  if (window.history.length > 1) router.back()
  else router.push('/')
}
</script>

<style scoped>
.doc-page { min-height: 100vh; background: var(--gx-paper); padding: 32px 16px; }
.doc-card { max-width: 760px; margin: 0 auto; background: var(--gx-warm-white);
  border: 1px solid var(--gx-divider); border-radius: 16px; padding: 36px 40px; }
.doc-card h1 { font-family: var(--gx-font-serif); font-size: 26px; margin: 0 0 6px; }
.doc-meta { color: var(--gx-sub); font-size: 13px; margin: 0 0 20px; line-height: 1.6; }
.doc-card h2 { font-family: var(--gx-font-serif); font-size: 18px; margin: 26px 0 8px; color: var(--gx-ink); }
.doc-card p, .doc-card li { color: #3a352d; font-size: 14.5px; line-height: 1.85; }
.doc-card ul { padding-left: 22px; }
.doc-contact { margin-top: 22px; color: var(--gx-ochre); font-size: 13px; }
.doc-back { margin-top: 8px; }
@media (max-width: 767px) { .doc-card { padding: 24px 18px; } }
</style>

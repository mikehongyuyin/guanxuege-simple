<template>
  <div class="doc-page">
    <div class="doc-card">
      <h1>隐私政策</h1>
      <p class="doc-meta">版本：1.0 ｜ 生效日期：2026-09-15 ｜ 运营方：观学阁</p>

      <h2>一、我们如何收集和使用信息</h2>
      <p>观学阁（以下简称"我们"）仅收集为您及您的孩子提供育儿管理、学习规划与社区交流所必需的信息：</p>
      <ul>
        <li><b>家长账户信息</b>：手机号、登录密码（加密存储）、昵称、头像。</li>
        <li><b>孩子档案信息</b>：姓名、年级、学校（选填）、出生日期（选填）、性别（选填）、性格与兴趣标签。我们遵循<b>最小必要</b>原则，学校等字段均可不填写。</li>
        <li><b>学习管理数据</b>：您创建的学习计划、任务打卡、成绩记录、家庭契约，仅用于向您本人展示与分析。</li>
        <li><b>社区内容</b>：您主动发布的帖子、评论，用于社区展示与互动。</li>
      </ul>

      <h2>二、关于儿童个人信息</h2>
      <p>我们深知儿童个人信息保护的重要性。在您<b>添加/绑定孩子</b>时，我们会单独向您（监护人）说明《儿童个人信息保护规则》并取得您的<b>专项同意</b>。我们不会收集与提供服务无关的未成年人信息，也不会向第三方出售或对外提供儿童个人信息。</p>

      <h2>三、信息的存储与保护</h2>
      <p>您的信息存储于中国大陆境内的服务器，采用传输加密与访问控制等措施保护安全。我们仅在为您提供服务所必需的时间内保留信息。</p>

      <h2>四、您的权利</h2>
      <ul>
        <li>您可随时查阅、更正自己的账户与孩子档案信息；</li>
        <li>您可<b>撤回</b>对儿童信息的专项同意，撤回后我们将停止处理并隐藏该孩子的相关数据；</li>
        <li>您可<b>删除</b>孩子档案，删除后其学习计划、成绩等关联数据将一并隐藏；</li>
        <li>您可注销账户。如需行使上述权利，请在「我的—设置」中操作，或联系我们。</li>
      </ul>

      <h2>五、第三方与对外提供</h2>
      <p>除为完成您明确授权的事项（如登录态校验）或法律法规要求外，我们不会向第三方提供您的个人信息。社区内容在您发布时即视为您同意在平台内公开展示。</p>

      <h2>六、政策的更新</h2>
      <p>我们可能适时修订本政策。重大变更将以显著方式通知您，并在您再次使用前重新征得同意。</p>

      <p class="doc-contact">联系我们：privacy@guanxuege.cn</p>
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
.doc-meta { color: var(--gx-sub); font-size: 13px; margin: 0 0 20px; }
.doc-card h2 { font-family: var(--gx-font-serif); font-size: 18px; margin: 26px 0 8px; color: var(--gx-ink); }
.doc-card p, .doc-card li { color: #3a352d; font-size: 14.5px; line-height: 1.85; }
.doc-card ul { padding-left: 22px; }
.doc-contact { margin-top: 22px; color: var(--gx-ochre); font-size: 13px; }
.doc-back { margin-top: 8px; }
@media (max-width: 767px) { .doc-card { padding: 24px 18px; } }
</style>

<template>
  <!--
    正文渲染：把 @昵称 渲染成可点击的高亮文本。
    刻意不用 v-html —— 正文是用户内容，拼接 HTML 会带来 XSS 风险；
    这里切成片段后用模板渲染，等价于纯文本输出。
  -->
  <span class="mention-text">
    <template v-for="(seg, i) in segments" :key="i">
      <span
        v-if="seg.type === 'mention'"
        class="mt-at"
        @click.stop="onClick(seg.userId)"
      >{{ seg.value }}</span>
      <span v-else class="mt-plain">{{ seg.value }}</span>
    </template>
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  content: { type: String, default: '' },
  // [{ userId, nickname }]
  mentions: { type: Array, default: () => [] }
})
const emit = defineEmits(['mention-click'])

function escapeRegExp(s) {
  return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

const segments = computed(() => {
  const text = props.content || ''
  const list = (props.mentions || []).filter((m) => m && m.nickname)
  if (!list.length) return [{ type: 'text', value: text }]

  // 长昵称优先匹配，避免「小明」抢先匹配掉「小明同学」
  const names = list.map((m) => m.nickname).sort((a, b) => b.length - a.length)
  const pattern = new RegExp('@(' + names.map(escapeRegExp).join('|') + ')', 'g')

  const out = []
  let last = 0
  let m
  while ((m = pattern.exec(text)) !== null) {
    if (m.index > last) out.push({ type: 'text', value: text.slice(last, m.index) })
    const hit = list.find((x) => x.nickname === m[1])
    out.push({ type: 'mention', value: '@' + m[1], userId: hit ? hit.userId : null })
    last = m.index + m[0].length
  }
  if (last < text.length) out.push({ type: 'text', value: text.slice(last) })
  return out
})

function onClick(userId) {
  if (userId) emit('mention-click', userId)
}
</script>

<style scoped>
.mention-text { white-space: pre-wrap; }
.mt-at {
  color: var(--gx-vermillion);
  cursor: pointer;
  font-weight: 600;
}
.mt-at:hover { text-decoration: underline; }
</style>

<template>
  <div class="home">
    <!-- 顶栏 -->
    <header class="h-header">
      <div class="h-brand"><span class="seal">观</span><span class="bn">观学阁</span></div>
      <nav class="h-nav">
        <a href="#features">功能</a>
        <a href="#how">如何使用</a>
        <a href="#voice">家长口碑</a>
        <router-link to="/app/community">社区</router-link>
        <router-link to="/login">登录</router-link>
      </nav>
      <router-link to="/login" class="h-cta gx-btn-primary">免费开始</router-link>
    </header>

    <!-- Hero -->
    <section class="h-hero">
      <div class="h-hero-text">
        <span class="h-badge">古法育人 · 现代陪伴</span>
        <h1>用心陪伴<br />孩子的每一步成长</h1>
        <p>观学阁，为学龄儿童家长打造的学习计划、成绩管理与育儿经验社区，让家庭教育有迹可循。</p>
        <div class="h-hero-btns">
          <router-link to="/login" class="gx-btn-primary big">免费开始</router-link>
          <a href="#features" class="ghost big">了解更多</a>
        </div>
        <div class="h-trust">已陪伴 <b>12,000+</b> 个家庭 · 覆盖小学全年级</div>
      </div>
      <div class="h-hero-art">
        <div class="ink-card">
          <div class="ink-row"><span class="dot c1"></span>本周计划完成率 <b>86%</b></div>
          <div class="ink-row"><span class="dot c2"></span>语文平均 <b>92</b> · 数学 <b>95</b></div>
          <div class="ink-row"><span class="dot c3"></span>社区热帖 <b>512</b> 讨论</div>
        </div>
      </div>
    </section>

    <!-- 功能 -->
    <section id="features" class="h-section">
      <h2 class="h-h2">专注家长真正需要的三件事</h2>
      <div class="h-cards">
        <router-link to="/app/plan" class="h-card-link">
          <div class="h-card">
            <div class="h-ico" style="background:var(--gx-vermillion)">✎</div>
            <h3>学习计划管理</h3>
            <p>按日 / 周 / 月规划孩子的学习任务，科目标签 + 进度条，一目了然。</p>
            <span class="h-go">进入学习计划 →</span>
          </div>
        </router-link>
        <router-link to="/app/grade" class="h-card-link">
          <div class="h-card">
            <div class="h-ico" style="background:var(--gx-celadon)">✓</div>
            <h3>成绩管理</h3>
            <p>记录每次考试，自动生成各科得分率与趋势，薄弱科目早发现。</p>
            <span class="h-go">进入成绩管理 →</span>
          </div>
        </router-link>
        <router-link to="/app/community" class="h-card-link">
          <div class="h-card">
            <div class="h-ico" style="background:var(--gx-ochre)">❝</div>
            <h3>育儿社区</h3>
            <p>与万千家长交流育儿管理经验，热门话题、真实口碑，少走弯路。</p>
            <span class="h-go">进入育儿社区 →</span>
          </div>
        </router-link>
      </div>
    </section>

    <!-- 如何使用 -->
    <section id="how" class="h-section alt">
      <h2 class="h-h2">三步开启科学陪伴</h2>
      <div class="h-steps">
        <div class="h-step"><span class="num">一</span><h4>建立孩子档案</h4><p>录入孩子年级与学校，绑定学习计划与成绩。</p></div>
        <div class="h-step"><span class="num">二</span><h4>规划与记录</h4><p>制定学习计划、登记考试成绩，系统自动汇总。</p></div>
        <div class="h-step"><span class="num">三</span><h4>交流成长</h4><p>在社区分享经验、参与话题，陪伴不孤单。</p></div>
      </div>
    </section>

    <!-- 口碑 -->
    <section id="voice" class="h-section">
      <h2 class="h-h2">家长们的真实声音</h2>
      <div class="h-stats">
        <div><b>12,000+</b><span>注册家庭</span></div>
        <div><b>86%</b><span>计划完成率</span></div>
        <div><b>4.8</b><span>家长评分</span></div>
        <div><b>512</b><span>社区热帖</span></div>
      </div>
      <div class="h-quotes">
        <div class="h-quote">「计划一目了然，孩子写作业不再拖拉。」 — 林女士</div>
        <div class="h-quote">「成绩趋势帮我及时发现数学下滑。」 — 陈先生</div>
        <div class="h-quote">「社区里学到了很多实用的育儿方法。」 — 王妈妈</div>
      </div>
    </section>

    <!-- 结尾 CTA -->
    <section class="h-cta-band">
      <h2>从今天起，更懂孩子的学习</h2>
      <router-link to="/login" class="gx-btn-primary big">免费开始</router-link>
    </section>

    <!-- 页脚 -->
    <footer class="h-footer">
      <div class="f-brand"><span class="seal sm">观</span> 观学阁 GuanXueGe</div>
      <div class="f-cols">
        <div><b>产品</b><span>学习计划</span><span>成绩管理</span><span>育儿社区</span></div>
        <div><b>关于</b><span>品牌理念</span><span>家长公约</span></div>
        <div><b>联系</b><span>客服</span><span>合作</span></div>
      </div>
      <div class="f-bottom">版权所有 2026 观学阁 GuanXueGe · 用心陪伴每一个家庭</div>
    </footer>
  </div>
</template>

<script setup></script>

<style scoped>
.home { background: var(--gx-paper); }
.h-header { display: flex; align-items: center; gap: 24px; padding: 18px 48px; position: sticky; top: 0; background: rgba(246,241,231,.92); backdrop-filter: blur(6px); z-index: 20; }
.h-brand { display: flex; align-items: center; gap: 10px; }
.seal { width: 36px; height: 36px; border-radius: 8px; background: var(--gx-vermillion); color: #fff; display: grid; place-items: center; font-family: var(--gx-font-serif); font-size: 20px; font-weight: 700; }
.seal.sm { width: 26px; height: 26px; font-size: 15px; }
.bn { font-family: var(--gx-font-serif); font-size: 22px; font-weight: 700; }
.h-nav { display: flex; gap: 24px; margin-left: auto; color: var(--gx-sub); font-size: 15px; }
.h-nav a:hover { color: var(--gx-vermillion); }
.h-cta { padding: 9px 22px; border-radius: 8px; font-size: 15px; }

.h-hero { display: flex; gap: 40px; align-items: center; padding: 64px 48px; max-width: 1200px; margin: 0 auto; }
.h-badge { display: inline-block; padding: 5px 12px; border: 1px solid var(--gx-vermillion); color: var(--gx-vermillion); border-radius: 999px; font-size: 13px; margin-bottom: 18px; }
.h-hero-text h1 { font-family: var(--gx-font-serif); font-size: 46px; line-height: 1.25; margin: 0 0 18px; }
.h-hero-text p { color: var(--gx-sub); font-size: 16px; line-height: 1.8; max-width: 520px; }
.h-hero-btns { display: flex; gap: 14px; margin: 26px 0 18px; }
.big { padding: 12px 28px; border-radius: 8px; font-size: 16px; display: inline-block; }
.ghost { border: 1px solid var(--gx-divider); color: var(--gx-ink); background: var(--gx-warm-white); }
.h-trust { color: var(--gx-ochre); font-size: 14px; }
.h-trust b { color: var(--gx-vermillion); }
.h-hero-art { flex: 1; display: grid; place-items: center; }
.ink-card { width: 340px; background: var(--gx-warm-white); border: 1px solid var(--gx-divider); border-radius: 16px; padding: 26px; box-shadow: 0 18px 40px rgba(43,38,32,.08); }
.ink-row { display: flex; align-items: center; gap: 10px; padding: 12px 0; border-bottom: 1px dashed var(--gx-divider); font-size: 15px; color: var(--gx-sub); }
.ink-row b { margin-left: auto; color: var(--gx-ink); font-family: var(--gx-font-serif); }
.dot { width: 12px; height: 12px; border-radius: 50%; }
.c1 { background: var(--gx-vermillion); } .c2 { background: var(--gx-celadon); } .c3 { background: var(--gx-ochre); }

.h-section { padding: 60px 48px; max-width: 1200px; margin: 0 auto; }
.h-section.alt { background: var(--gx-muted); max-width: none; }
.h-section.alt > * { max-width: 1200px; margin-left: auto; margin-right: auto; }
.h-h2 { font-family: var(--gx-font-serif); font-size: 32px; text-align: center; margin: 0 0 36px; }
.h-cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 22px; }
.h-card-link { display: block; text-decoration: none; color: inherit; }
.h-card-link:hover .h-card { transform: translateY(-4px); box-shadow: 0 14px 30px rgba(43,38,32,.1); }
.h-card { background: var(--gx-warm-white); border: 1px solid var(--gx-divider); border-radius: var(--gx-radius); padding: 28px; transition: transform .2s, box-shadow .2s; }
.h-ico { width: 46px; height: 46px; border-radius: 12px; color: #fff; display: grid; place-items: center; font-size: 22px; margin-bottom: 16px; }
.h-card h3 { font-family: var(--gx-font-serif); font-size: 20px; margin: 0 0 10px; }
.h-card p { color: var(--gx-sub); line-height: 1.7; font-size: 14px; }
.h-go { display: inline-block; margin-top: 14px; color: var(--gx-vermillion); font-size: 14px; }
.h-steps { display: grid; grid-template-columns: repeat(3, 1fr); gap: 22px; }
.h-step { text-align: center; }
.h-step .num { display: inline-grid; place-items: center; width: 48px; height: 48px; border-radius: 50%; background: var(--gx-vermillion); color: #fff; font-family: var(--gx-font-serif); font-size: 22px; margin-bottom: 14px; }
.h-step h4 { font-family: var(--gx-font-serif); font-size: 18px; margin: 0 0 8px; }
.h-step p { color: var(--gx-sub); font-size: 14px; }

.h-stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 18px; margin-bottom: 30px; }
.h-stats div { text-align: center; background: var(--gx-warm-white); border: 1px solid var(--gx-divider); border-radius: var(--gx-radius); padding: 22px; }
.h-stats b { display: block; font-family: var(--gx-font-serif); font-size: 30px; color: var(--gx-vermillion); }
.h-stats span { color: var(--gx-sub); font-size: 13px; }
.h-quotes { display: grid; grid-template-columns: repeat(3, 1fr); gap: 18px; }
.h-quote { background: var(--gx-warm-white); border-left: 4px solid var(--gx-celadon); border-radius: 8px; padding: 18px; color: var(--gx-sub); font-size: 14px; line-height: 1.7; }

.h-cta-band { text-align: center; padding: 70px 48px; background: var(--gx-vermillion); color: #fff; }
.h-cta-band h2 { font-family: var(--gx-font-serif); font-size: 30px; margin: 0 0 24px; }
.h-cta-band .gx-btn-primary { background: #fff; color: var(--gx-vermillion); border-color: #fff; }

.h-footer { padding: 48px; background: var(--gx-ink); color: #efe7d8; }
.f-brand { font-family: var(--gx-font-serif); font-size: 20px; margin-bottom: 24px; }
.f-cols { display: flex; gap: 60px; margin-bottom: 24px; }
.f-cols b { display: block; margin-bottom: 10px; color: #fff; }
.f-cols span { display: block; font-size: 13px; color: #cbbfa9; margin-bottom: 6px; }
.f-bottom { border-top: 1px solid #4a4239; padding-top: 18px; font-size: 13px; color: #cbbfa9; }
</style>

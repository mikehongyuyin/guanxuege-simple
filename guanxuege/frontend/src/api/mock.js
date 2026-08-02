// 本地 mock 数据（USE_MOCK=true 时启用），与后端 R{code,data,msg} 结构保持一致
const delay = (data) => new Promise((resolve) => setTimeout(() => resolve(data), 200))

const USER = { id: 1, nickname: '王妈妈', phone: '13800000000', avatar: '', role: 1, status: 1 }

const CHILDREN = [
  { id: 1, parentId: 1, name: '小明', grade: '二年级', school: '阳光小学', avatar: '' },
  { id: 2, parentId: 1, name: '小雅', grade: '五年级', school: '晨光小学', avatar: '' }
]

const PLAN_OVERVIEW = {
  todoCount: 2, doingCount: 3, doneCount: 1,
  taskTotal: 20, taskDone: 8, overallProgress: 40
}

const PLANS = [
  {
    id: 1, childId: 1, title: '本周语文复习计划', description: '巩固课文背诵与阅读理解',
    planType: 'WEEK', status: 1, startDate: '2026-08-03', endDate: '2026-08-09', progress: 60,
    tasks: [
      { id: 11, planId: 1, title: '背诵古诗三首', subject: '语文', status: 2, dueDate: '2026-08-04', progress: 100 },
      { id: 12, planId: 1, title: '阅读理解练习 2 篇', subject: '语文', status: 1, dueDate: '2026-08-06', progress: 50 },
      { id: 13, planId: 1, title: '生字听写', subject: '语文', status: 0, dueDate: '2026-08-08', progress: 0 }
    ]
  },
  {
    id: 2, childId: 1, title: '数学每日一练', description: '口算 + 应用题',
    planType: 'DAY', status: 1, startDate: '2026-08-01', endDate: '2026-08-31', progress: 30,
    tasks: [
      { id: 21, planId: 2, title: '口算 50 题', subject: '数学', status: 1, dueDate: '2026-08-04', progress: 40 },
      { id: 22, planId: 2, title: '应用题 3 道', subject: '数学', status: 0, dueDate: '2026-08-05', progress: 0 }
    ]
  },
  {
    id: 3, childId: 1, title: '英语绘本阅读', description: '每周 2 本绘本',
    planType: 'WEEK', status: 2, startDate: '2026-07-27', endDate: '2026-08-02', progress: 100,
    tasks: [
      { id: 31, planId: 3, title: '绘本 A', subject: '英语', status: 2, dueDate: '2026-07-29', progress: 100 },
      { id: 32, planId: 3, title: '绘本 B', subject: '英语', status: 2, dueDate: '2026-08-01', progress: 100 }
    ]
  }
]

const GRADE_OVERVIEW = {
  overallAverage: 91.25,
  subjectStats: [
    { subject: '语文', average: 92, fullScore: 100, rate: 92, count: 3 },
    { subject: '数学', average: 95, fullScore: 100, rate: 95, count: 3 },
    { subject: '英语', average: 88, fullScore: 100, rate: 88, count: 3 },
    { subject: '科学', average: 90, fullScore: 100, rate: 90, count: 2 }
  ]
}

const GRADES = [
  { id: 1, childId: 1, subject: '语文', examName: '期中', score: 94, fullScore: 100, examDate: '2026-04-20', rankInClass: 5 },
  { id: 2, childId: 1, subject: '数学', examName: '期中', score: 97, fullScore: 100, examDate: '2026-04-20', rankInClass: 2 },
  { id: 3, childId: 1, subject: '英语', examName: '期中', score: 86, fullScore: 100, examDate: '2026-04-20', rankInClass: 12 },
  { id: 4, childId: 1, subject: '语文', examName: '期末', score: 90, fullScore: 100, examDate: '2026-07-01', rankInClass: 8 },
  { id: 5, childId: 1, subject: '数学', examName: '期末', score: 93, fullScore: 100, examDate: '2026-07-01', rankInClass: 4 }
]

const POSTS = [
  { id: 1, authorId: 2, title: '孩子写作业总是拖拉，大家有什么好办法？', tag: '习惯养成',
    content: '试了番茄钟，效果还不错，分享给同样困扰的家长。', viewCount: 120, likeCount: 12, commentCount: 8,
    authorNickname: '林女士', authorAvatar: '' },
  { id: 2, authorId: 3, title: '五年级数学突然下滑，从哪开始补？', tag: '成绩交流',
    content: '单元测试从 95 掉到 78，先抓计算还是应用题？', viewCount: 230, likeCount: 24, commentCount: 15,
    authorNickname: '陈先生', authorAvatar: '' },
  { id: 3, authorId: 1, title: '双减之后，周末到底该怎么安排？', tag: '双减政策',
    content: '放弃了学科补习，反而更纠结时间怎么填。', viewCount: 310, likeCount: 31, commentCount: 19,
    authorNickname: '王妈妈', authorAvatar: '' },
  { id: 4, authorId: 4, title: '孩子沉迷手机，该怎么立规矩？', tag: '习惯养成',
    content: '试过没收、讲过道理，效果都不持久。', viewCount: 410, likeCount: 45, commentCount: 27,
    authorNickname: '赵女士', authorAvatar: '' }
]

const TOPICS = [
  { id: 1, name: '如何培养孩子的专注力', discussCount: 512 },
  { id: 2, name: '双减之后周末该怎么安排', discussCount: 430 },
  { id: 3, name: '五年级数学突然下滑怎么办', discussCount: 387 },
  { id: 4, name: '亲子阅读书单推荐', discussCount: 295 },
  { id: 5, name: '二胎家庭的平衡之道', discussCount: 241 }
]

export default {
  delay,
  USER, CHILDREN, PLAN_OVERVIEW, PLANS, GRADE_OVERVIEW, GRADES, POSTS, TOPICS
}

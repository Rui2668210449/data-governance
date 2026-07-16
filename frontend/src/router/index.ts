import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import AppLayout from '@/components/layout/AppLayout.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/pages/Login.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/',
    component: AppLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/pages/Dashboard.vue'),
        meta: { title: '工作台首页' },
      },
      // 元数据管理
      {
        path: 'metadata/overview',
        name: 'metadata-overview',
        component: () => import('@/pages/metadata/Overview.vue'),
        meta: { title: '资产全景图' },
      },
      {
        path: 'metadata/datasource',
        name: 'metadata-datasource',
        component: () => import('@/pages/metadata/Datasource.vue'),
        meta: { title: '数据源管理' },
      },
      {
        path: 'metadata/table/:id',
        name: 'metadata-table-detail',
        component: () => import('@/pages/metadata/TableDetail.vue'),
        meta: { title: '表详情' },
      },
      {
        path: 'metadata/lineage',
        name: 'metadata-lineage',
        component: () => import('@/pages/metadata/Lineage.vue'),
        meta: { title: '血缘图谱' },
      },
      // 数据标准
      {
        path: 'standard/tree',
        name: 'standard-tree',
        component: () => import('@/pages/standard/StandardTree.vue'),
        meta: { title: '标准树' },
      },
      {
        path: 'standard/mapping',
        name: 'standard-mapping',
        component: () => import('@/pages/standard/Mapping.vue'),
        meta: { title: '标准映射' },
      },
      {
        path: 'standard/audit',
        name: 'standard-audit',
        component: () => import('@/pages/standard/Audit.vue'),
        meta: { title: '落标稽核' },
      },
      // 数据质量
      {
        path: 'quality/rule',
        name: 'quality-rule',
        component: () => import('@/pages/quality/Rule.vue'),
        meta: { title: '质量规则' },
      },
      {
        path: 'quality/dashboard',
        name: 'quality-dashboard',
        component: () => import('@/pages/quality/Dashboard.vue'),
        meta: { title: '质量看板' },
      },
      {
        path: 'quality/ticket',
        name: 'quality-ticket',
        component: () => import('@/pages/quality/Ticket.vue'),
        meta: { title: '质量工单' },
      },
      // 数据安全
      {
        path: 'security/classify',
        name: 'security-classify',
        component: () => import('@/pages/security/Classify.vue'),
        meta: { title: '分级标签' },
      },
      {
        path: 'security/sensitive-map',
        name: 'security-sensitive-map',
        component: () => import('@/pages/security/SensitiveMap.vue'),
        meta: { title: '敏感数据地图' },
      },
      {
        path: 'security/mask',
        name: 'security-mask',
        component: () => import('@/pages/security/Mask.vue'),
        meta: { title: '脱敏策略' },
      },
      {
        path: 'security/audit',
        name: 'security-audit',
        component: () => import('@/pages/security/Audit.vue'),
        meta: { title: '审计日志' },
      },
      // 资产目录
      {
        path: 'catalog/search',
        name: 'catalog-search',
        component: () => import('@/pages/catalog/Search.vue'),
        meta: { title: '资产检索' },
      },
      {
        path: 'catalog/asset/:id',
        name: 'catalog-asset-detail',
        component: () => import('@/pages/catalog/AssetDetail.vue'),
        meta: { title: '资产详情' },
      },
      {
        path: 'catalog/tag',
        name: 'catalog-tag',
        component: () => import('@/pages/catalog/Tag.vue'),
        meta: { title: '标签管理' },
      },
      {
        path: 'catalog/workspace',
        name: 'catalog-workspace',
        component: () => import('@/pages/catalog/Workspace.vue'),
        meta: { title: '个人工作台' },
      },
      // 生命周期
      {
        path: 'lifecycle/policy',
        name: 'lifecycle-policy',
        component: () => import('@/pages/lifecycle/Policy.vue'),
        meta: { title: '生命周期策略' },
      },
      {
        path: 'lifecycle/dashboard',
        name: 'lifecycle-dashboard',
        component: () => import('@/pages/lifecycle/Dashboard.vue'),
        meta: { title: '生命周期仪表盘' },
      },
      // 工作流
      {
        path: 'workflow/board',
        name: 'workflow-board',
        component: () => import('@/pages/workflow/Board.vue'),
        meta: { title: '治理看板' },
      },
      {
        path: 'workflow/todo',
        name: 'workflow-todo',
        component: () => import('@/pages/workflow/Todo.vue'),
        meta: { title: '我的待办' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  document.title = `${to.meta.title || '数据治理平台'} - 数据治理平台`
  const token = localStorage.getItem('gov_token')
  if (!token && to.path !== '/login') {
    next('/login')
  } else {
    next()
  }
})

export default router

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { getQualityTickets, updateTicketStatus } from '@/api'
import type { QualityTicket } from '@/types'

const loading = ref(false)
const tickets = ref<QualityTicket[]>([])

const statusFilter = ref('ALL')
const keyword = ref('')

const statusTabs = [
  { label: '全部', value: 'ALL' },
  { label: '待处理', value: 'OPEN' },
  { label: '处理中', value: 'PROCESSING' },
  { label: '已解决', value: 'RESOLVED' },
  { label: '已关闭', value: 'CLOSED' },
]

const statusTagMap: Record<string, { text: string; theme: 'danger' | 'warning' | 'success' | 'default' }> = {
  OPEN: { text: '待处理', theme: 'danger' },
  PROCESSING: { text: '处理中', theme: 'warning' },
  RESOLVED: { text: '已解决', theme: 'success' },
  CLOSED: { text: '已关闭', theme: 'default' },
}

const priorityTagMap: Record<string, { text: string; theme: 'danger' | 'warning' | 'primary' }> = {
  HIGH: { text: '高', theme: 'danger' },
  MEDIUM: { text: '中', theme: 'warning' },
  LOW: { text: '低', theme: 'primary' },
}

// 状态流转: OPEN -> PROCESSING -> RESOLVED -> CLOSED
const nextStatusMap: Record<string, string> = {
  OPEN: 'PROCESSING',
  PROCESSING: 'RESOLVED',
  RESOLVED: 'CLOSED',
}

const nextActionTextMap: Record<string, string> = {
  OPEN: '开始处理',
  PROCESSING: '标记解决',
  RESOLVED: '关闭工单',
}

const statusCounts = computed(() => ({
  OPEN: tickets.value.filter((t) => t.status === 'OPEN').length,
  PROCESSING: tickets.value.filter((t) => t.status === 'PROCESSING').length,
  RESOLVED: tickets.value.filter((t) => t.status === 'RESOLVED').length,
  CLOSED: tickets.value.filter((t) => t.status === 'CLOSED').length,
}))

const filteredTickets = computed(() => {
  return tickets.value.filter((t) => {
    const sOk = statusFilter.value === 'ALL' || t.status === statusFilter.value
    const kwOk = !keyword.value || t.title.includes(keyword.value) || t.ruleName.includes(keyword.value) || t.assignee.includes(keyword.value)
    return sOk && kwOk
  })
})

const columns = [
  { colKey: 'title', title: '工单标题', minWidth: 220, ellipsis: true },
  { colKey: 'ruleName', title: '关联规则', width: 160, ellipsis: true },
  { colKey: 'status', title: '状态', width: 110, cell: 'status' },
  { colKey: 'priority', title: '优先级', width: 90, cell: 'priority' },
  { colKey: 'assignee', title: '负责人', width: 100 },
  { colKey: 'createTime', title: '创建时间', width: 170 },
  { colKey: 'op', title: '流转操作', width: 120, cell: 'op', fixed: 'right' as const },
]

async function loadData() {
  loading.value = true
  try {
    tickets.value = await getQualityTickets()
  } finally {
    loading.value = false
  }
}

async function handleAdvance(row: QualityTicket) {
  const next = nextStatusMap[row.status]
  if (!next) return
  await updateTicketStatus(row.id, next)
  row.status = next as QualityTicket['status']
  row.updateTime = new Date().toLocaleString('zh-CN').replace(/\//g, '-')
  MessagePlugin.success(`工单已流转为「${statusTagMap[next].text}」`)
}

// ============ 抽屉详情 ============
const drawerVisible = ref(false)
const currentTicket = ref<QualityTicket | null>(null)

interface Comment {
  id: number
  user: string
  time: string
  content: string
}

const commentMap: Record<number, Comment[]> = {
  1: [
    { id: 1, user: '系统', time: '2026-07-15 09:00:00', content: '规则「phone唯一性校验」检测到异常，自动生成工单' },
    { id: 2, user: '张三', time: '2026-07-15 10:30:00', content: '已确认是数据同步任务重复执行导致，正在排查同步逻辑' },
    { id: 3, user: '张三', time: '2026-07-15 14:00:00', content: '已修复同步任务，对235条重复数据执行去重清洗' },
  ],
  2: [
    { id: 4, user: '系统', time: '2026-07-16 08:00:00', content: '规则「日志时效性检查」触发，昨日分区未产出' },
    { id: 5, user: '王五', time: '2026-07-16 08:30:00', content: '上游ETL任务失败，正在联系数据工程团队处理' },
  ],
  3: [
    { id: 6, user: '系统', time: '2026-07-10 10:00:00', content: '规则「手机号格式校验」触发，120条记录格式异常' },
    { id: 7, user: '李四', time: '2026-07-12 16:00:00', content: '已通过正则清洗修复，验证通过' },
  ],
  4: [
    { id: 8, user: '系统', time: '2026-07-05 11:00:00', content: '规则「订单金额准确性」触发，检测到负数金额' },
    { id: 9, user: '李四', time: '2026-07-06 09:00:00', content: '确认是退款订单，属正常业务，关闭工单' },
  ],
}

const currentComments = computed<Comment[]>(() => {
  if (!currentTicket.value) return []
  return commentMap[currentTicket.value.id] || []
})

const newComment = ref('')

function openDetail(row: QualityTicket) {
  currentTicket.value = row
  newComment.value = ''
  drawerVisible.value = true
}

function addComment() {
  if (!currentTicket.value || !newComment.value.trim()) {
    MessagePlugin.warning('请输入评论内容')
    return
  }
  const list = commentMap[currentTicket.value.id] || (commentMap[currentTicket.value.id] = [])
  list.push({
    id: Date.now(),
    user: '当前用户',
    time: new Date().toLocaleString('zh-CN').replace(/\//g, '-'),
    content: newComment.value.trim(),
  })
  newComment.value = ''
  MessagePlugin.success('评论已添加')
}

onMounted(loadData)
</script>

<template>
  <div class="page-container" v-loading="loading">
    <!-- 状态统计卡片 -->
    <div class="stat-grid">
      <div class="stat-card" style="--c: #e34d59">
        <div class="stat-icon">
          <t-icon name="error-circle" size="22" />
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ statusCounts.OPEN }}</div>
          <div class="stat-label">待处理</div>
        </div>
      </div>
      <div class="stat-card" style="--c: #ed7b2f">
        <div class="stat-icon">
          <t-icon name="loading" size="22" />
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ statusCounts.PROCESSING }}</div>
          <div class="stat-label">处理中</div>
        </div>
      </div>
      <div class="stat-card" style="--c: #00a870">
        <div class="stat-icon">
          <t-icon name="check-circle" size="22" />
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ statusCounts.RESOLVED }}</div>
          <div class="stat-label">已解决</div>
        </div>
      </div>
      <div class="stat-card" style="--c: #8c8c8c">
        <div class="stat-icon">
          <t-icon name="check-double" size="22" />
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ statusCounts.CLOSED }}</div>
          <div class="stat-label">已关闭</div>
        </div>
      </div>
    </div>

    <!-- 工单列表 -->
    <div class="page-card">
      <div class="flex-between" style="margin-bottom: 16px">
        <h3 class="page-title" style="margin: 0">质量工单</h3>
        <t-input
          v-model="keyword"
          placeholder="搜索标题 / 规则 / 负责人"
          clearable
          style="width: 260px"
        >
          <template #prefix-icon><t-icon name="search" /></template>
        </t-input>
      </div>

      <t-tabs v-model="statusFilter" style="margin-bottom: 12px">
        <t-tab-panel
          v-for="tab in statusTabs"
          :key="tab.value"
          :value="tab.value"
          :label="tab.label"
        />
      </t-tabs>

      <t-table
        :data="filteredTickets"
        row-key="id"
        :columns="columns"
        size="small"
        :pagination="{ show: false }"
        bordered
        @row-click="openDetail"
      >
        <template #status="{ row }">
          <t-tag :theme="statusTagMap[row.status].theme" variant="light" size="small">
            {{ statusTagMap[row.status].text }}
          </t-tag>
        </template>
        <template #priority="{ row }">
          <t-tag :theme="priorityTagMap[row.priority].theme" variant="light" size="small">
            {{ priorityTagMap[row.priority].text }}
          </t-tag>
        </template>
        <template #op="{ row }">
          <t-button
            v-if="nextStatusMap[row.status]"
            theme="primary"
            variant="outline"
            size="small"
            @click.stop="handleAdvance(row)"
          >
            {{ nextActionTextMap[row.status] }}
          </t-button>
          <span v-else class="text-secondary" style="font-size: 12px">—</span>
        </template>
      </t-table>
    </div>

    <!-- 工单详情抽屉 -->
    <t-drawer
      v-model:visible="drawerVisible"
      header="工单详情"
      size="480px"
      :close-on-overlay-click="true"
    >
      <template v-if="currentTicket">
        <t-descriptions :column="1" bordered>
          <t-descriptions-item label="工单标题">{{ currentTicket.title }}</t-descriptions-item>
          <t-descriptions-item label="关联规则">{{ currentTicket.ruleName }}</t-descriptions-item>
          <t-descriptions-item label="状态">
            <t-tag :theme="statusTagMap[currentTicket.status].theme" variant="light" size="small">
              {{ statusTagMap[currentTicket.status].text }}
            </t-tag>
          </t-descriptions-item>
          <t-descriptions-item label="优先级">
            <t-tag :theme="priorityTagMap[currentTicket.priority].theme" variant="light" size="small">
              {{ priorityTagMap[currentTicket.priority].text }}
            </t-tag>
          </t-descriptions-item>
          <t-descriptions-item label="负责人">{{ currentTicket.assignee }}</t-descriptions-item>
          <t-descriptions-item label="创建时间">{{ currentTicket.createTime }}</t-descriptions-item>
          <t-descriptions-item label="更新时间">{{ currentTicket.updateTime }}</t-descriptions-item>
          <t-descriptions-item label="问题描述">
            <div class="desc-text">{{ currentTicket.description }}</div>
          </t-descriptions-item>
        </t-descriptions>

        <div v-if="nextStatusMap[currentTicket.status]" style="margin-top: 16px">
          <t-button theme="primary" block @click="handleAdvance(currentTicket)">
            {{ nextActionTextMap[currentTicket.status] }}
          </t-button>
        </div>

        <!-- 评论区 -->
        <div class="comment-section">
          <div class="comment-header">
            <t-icon name="chat" />
            <span>处理记录 ({{ currentComments.length }})</span>
          </div>
          <div class="comment-list">
            <div v-for="c in currentComments" :key="c.id" class="comment-item">
              <div class="comment-avatar">{{ c.user.charAt(0) }}</div>
              <div class="comment-body">
                <div class="comment-meta">
                  <span class="comment-user">{{ c.user }}</span>
                  <span class="comment-time">{{ c.time }}</span>
                </div>
                <div class="comment-content">{{ c.content }}</div>
              </div>
            </div>
          </div>
          <div class="comment-input">
            <t-textarea
              v-model="newComment"
              placeholder="添加处理记录..."
              :autosize="{ minRows: 3, maxRows: 6 }"
            />
            <t-button theme="primary" size="small" style="margin-top: 8px" @click="addComment">
              发表评论
            </t-button>
          </div>
        </div>
      </template>
    </t-drawer>
  </div>
</template>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}
.stat-card {
  background: #fff;
  border-radius: 6px;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  border-left: 3px solid var(--c);
}
.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: color-mix(in srgb, var(--c) 12%, transparent);
  color: var(--c);
  flex-shrink: 0;
}
.stat-value {
  font-size: 26px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.2;
}
.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 2px;
}
.desc-text {
  line-height: 1.6;
  color: var(--text-secondary);
}
.comment-section {
  margin-top: 24px;
}
.comment-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}
.comment-item {
  display: flex;
  gap: 10px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 6px;
}
.comment-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--brand-color);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}
.comment-body {
  flex: 1;
  min-width: 0;
}
.comment-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.comment-user {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}
.comment-time {
  font-size: 12px;
  color: var(--text-secondary);
}
.comment-content {
  font-size: 13px;
  color: var(--text-primary);
  line-height: 1.6;
  word-break: break-word;
}
.comment-input {
  border-top: 1px solid var(--border-color);
  padding-top: 12px;
}
</style>

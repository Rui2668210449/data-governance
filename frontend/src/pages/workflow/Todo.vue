<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { getMyTodo, getWorkflowTasks, updateTaskStatus } from '@/api'
import type { WorkflowTask } from '@/types'

const loading = ref(false)
const activeTab = ref<'todo' | 'done'>('todo')
const todoTasks = ref<WorkflowTask[]>([])
const allTasks = ref<WorkflowTask[]>([])

const typeMeta: Record<string, { text: string; color: string }> = {
  QUALITY: { text: '质量', color: '#00a870' },
  SECURITY: { text: '安全', color: '#e34d59' },
  STANDARD: { text: '标准', color: '#7265e6' },
  APPROVAL: { text: '审批', color: '#0052d9' },
}

const priorityMeta: Record<string, { text: string; theme: 'danger' | 'warning' | 'primary' }> = {
  HIGH: { text: '高', theme: 'danger' },
  MEDIUM: { text: '中', theme: 'warning' },
  LOW: { text: '低', theme: 'primary' },
}

const statusMeta: Record<string, { text: string; color: string }> = {
  TODO: { text: '待办', color: '#0052d9' },
  DOING: { text: '进行中', color: '#ed7b2f' },
  DONE: { text: '已完成', color: '#00a870' },
}

const currentList = computed(() =>
  activeTab.value === 'todo' ? todoTasks.value : allTasks.value.filter((t) => t.status === 'DONE')
)

const today = new Date('2026-07-16').getTime()
function isOverdue(task: WorkflowTask) {
  const due = new Date(task.dueDate).getTime()
  return due < today && task.status !== 'DONE'
}

const columns = [
  { colKey: 'title', title: '任务标题', minWidth: 220, ellipsis: true },
  { colKey: 'type', title: '类型', width: 90, cell: 'type' },
  { colKey: 'priority', title: '优先级', width: 90, cell: 'priority' },
  { colKey: 'assignee', title: '负责人', width: 120 },
  { colKey: 'dueDate', title: '截止日期', width: 140, cell: 'dueDate' },
  { colKey: 'status', title: '状态', width: 100, cell: 'status' },
  { colKey: 'op', title: '操作', width: 140, cell: 'op', fixed: 'right' as const },
]

async function loadData() {
  loading.value = true
  try {
    const [todo, all] = await Promise.all([getMyTodo(), getWorkflowTasks()])
    todoTasks.value = todo
    allTasks.value = all
  } finally {
    loading.value = false
  }
}

// ============ 任务详情抽屉 ============
const drawerVisible = ref(false)
const currentTask = ref<WorkflowTask | null>(null)
const submitting = ref(false)

// 评论列表（模拟）
interface Comment {
  id: number
  user: string
  content: string
  time: string
}
const comments = ref<Comment[]>([])

function openHandle(task: WorkflowTask) {
  currentTask.value = { ...task }
  // 生成与任务相关的模拟评论
  comments.value = [
    {
      id: 1,
      user: task.assignee,
      content: `已查看任务，开始处理：${task.description}`,
      time: task.createTime,
    },
    {
      id: 2,
      user: '系统',
      content: `任务创建于 ${task.createTime}，截止日期 ${task.dueDate}`,
      time: task.createTime,
    },
  ]
  drawerVisible.value = true
}

// 状态流转：TODO -> DOING -> DONE
function nextStatus(status: string): string | null {
  if (status === 'TODO') return 'DOING'
  if (status === 'DOING') return 'DONE'
  return null
}

async function handleAdvance() {
  if (!currentTask.value) return
  const next = nextStatus(currentTask.value.status)
  if (!next) {
    MessagePlugin.info('任务已完成，无法继续流转')
    return
  }
  submitting.value = true
  try {
    await updateTaskStatus(currentTask.value.id, next)
    // 更新本地
    currentTask.value.status = next as WorkflowTask['status']
    // 同步两个列表
    const t1 = todoTasks.value.find((t) => t.id === currentTask.value!.id)
    if (t1) t1.status = next as WorkflowTask['status']
    const t2 = allTasks.value.find((t) => t.id === currentTask.value!.id)
    if (t2) t2.status = next as WorkflowTask['status']
    // 若已 DONE，从待办移除
    if (next === 'DONE') {
      todoTasks.value = todoTasks.value.filter((t) => t.id !== currentTask.value!.id)
    }
    comments.value.push({
      id: comments.value.length + 1,
      user: '当前用户',
      content: `任务状态已流转至「${statusMeta[next].text}」`,
      time: new Date().toLocaleString('zh-CN'),
    })
    MessagePlugin.success(`任务已流转至「${statusMeta[next].text}」`)
    if (next === 'DONE') {
      drawerVisible.value = false
    }
  } finally {
    submitting.value = false
  }
}

const newComment = ref('')
function handleAddComment() {
  if (!newComment.value.trim()) return
  comments.value.push({
    id: comments.value.length + 1,
    user: '当前用户',
    content: newComment.value,
    time: new Date().toLocaleString('zh-CN'),
  })
  newComment.value = ''
  MessagePlugin.success('评论已添加')
}

onMounted(loadData)
</script>

<template>
  <div class="page-container" v-loading="loading">
    <div class="page-card">
      <div class="flex-between" style="margin-bottom: 16px">
        <h3 class="page-title" style="margin: 0">我的待办</h3>
        <div class="flex gap-12 flex-center">
          <t-tag theme="warning" variant="light">
            待办 {{ todoTasks.length }}
          </t-tag>
          <t-tag theme="success" variant="light">
            已办 {{ allTasks.filter(t => t.status === 'DONE').length }}
          </t-tag>
        </div>
      </div>

      <t-tabs v-model="activeTab">
        <t-tab-panel value="todo" label="待办" />
        <t-tab-panel value="done" label="已办" />
      </t-tabs>

      <t-table
        :data="currentList"
        row-key="id"
        :columns="columns"
        size="small"
        :pagination="{ pageSize: 10 }"
        bordered
        style="margin-top: 8px"
      >
        <template #type="{ row }">
          <t-tag size="small" :color="typeMeta[row.type].color" variant="light">
            {{ typeMeta[row.type].text }}
          </t-tag>
        </template>
        <template #priority="{ row }">
          <t-tag size="small" :theme="priorityMeta[row.priority].theme" variant="light">
            {{ priorityMeta[row.priority].text }}
          </t-tag>
        </template>
        <template #dueDate="{ row }">
          <span :class="{ 'overdue-text': isOverdue(row) }">
            {{ row.dueDate }}
            <t-icon v-if="isOverdue(row)" name="error-triangle" size="12" />
          </span>
        </template>
        <template #status="{ row }">
          <t-tag size="small" :color="statusMeta[row.status].color" variant="light">
            {{ statusMeta[row.status].text }}
          </t-tag>
        </template>
        <template #op="{ row }">
          <t-button
            v-if="row.status !== 'DONE'"
            theme="primary"
            variant="text"
            size="small"
            @click="openHandle(row)"
          >
            处理
          </t-button>
          <t-button
            theme="default"
            variant="text"
            size="small"
            @click="openHandle(row)"
          >
            查看
          </t-button>
        </template>
      </t-table>
    </div>

    <!-- 任务详情抽屉 -->
    <t-drawer
      v-model:visible="drawerVisible"
      header="任务详情"
      size="560px"
      :footer="false"
    >
      <div v-if="currentTask" class="drawer-content">
        <!-- 任务基本信息 -->
        <div class="detail-section">
          <div class="detail-title">{{ currentTask.title }}</div>
          <div class="detail-tags">
            <t-tag size="small" :color="typeMeta[currentTask.type].color" variant="light">
              {{ typeMeta[currentTask.type].text }}
            </t-tag>
            <t-tag size="small" :theme="priorityMeta[currentTask.priority].theme" variant="light">
              {{ priorityMeta[currentTask.priority].text }}优先级
            </t-tag>
            <t-tag size="small" :color="statusMeta[currentTask.status].color" variant="light">
              {{ statusMeta[currentTask.status].text }}
            </t-tag>
            <t-tag
              v-if="isOverdue(currentTask)"
              size="small"
              theme="danger"
              variant="light"
            >
              已逾期
            </t-tag>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-label">任务描述</div>
          <div class="section-content">{{ currentTask.description }}</div>
        </div>

        <div class="detail-meta">
          <div class="meta-row">
            <span class="meta-label">负责人</span>
            <span class="meta-value">
              <t-icon name="user-circle" size="14" />
              {{ currentTask.assignee }}
            </span>
          </div>
          <div class="meta-row">
            <span class="meta-label">截止日期</span>
            <span class="meta-value" :class="{ 'overdue-text': isOverdue(currentTask) }">
              <t-icon name="time" size="14" />
              {{ currentTask.dueDate }}
            </span>
          </div>
          <div class="meta-row">
            <span class="meta-label">创建时间</span>
            <span class="meta-value">{{ currentTask.createTime }}</span>
          </div>
        </div>

        <!-- 状态流转 -->
        <div class="detail-section" v-if="currentTask.status !== 'DONE'">
          <div class="section-label">状态流转</div>
          <t-button theme="primary" :loading="submitting" @click="handleAdvance" block>
            <template #icon><t-icon name="arrow-right" /></template>
            推进到「{{ nextStatus(currentTask.status) ? statusMeta[nextStatus(currentTask.status)!].text : '结束' }}」
          </t-button>
        </div>

        <!-- 评论区 -->
        <div class="detail-section">
          <div class="section-label">评论 ({{ comments.length }})</div>
          <div class="comment-list">
            <div v-for="c in comments" :key="c.id" class="comment-item">
              <div class="comment-avatar">
                <t-icon name="user-circle" size="20" />
              </div>
              <div class="comment-body">
                <div class="comment-head">
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
              placeholder="添加评论..."
              :autosize="{ minRows: 2, maxRows: 4 }"
            />
            <t-button
              theme="primary"
              size="small"
              style="margin-top: 8px"
              @click="handleAddComment"
            >
              发表
            </t-button>
          </div>
        </div>
      </div>
    </t-drawer>
  </div>
</template>

<style scoped>
.page-title {
  margin: 0;
}
.overdue-text {
  color: var(--danger);
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.drawer-content {
  padding: 4px 0 24px;
}
.detail-section {
  margin-bottom: 20px;
}
.detail-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
  margin-bottom: 10px;
}
.detail-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.section-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}
.section-content {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  padding: 10px 12px;
  background: #fafbfc;
  border-radius: 4px;
  border: 1px solid var(--border-color);
}
.detail-meta {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 12px;
  background: #fafbfc;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  margin-bottom: 20px;
}
.meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}
.meta-label {
  color: var(--text-secondary);
}
.meta-value {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--text-primary);
  font-weight: 500;
}
.comment-list {
  max-height: 240px;
  overflow-y: auto;
  margin-bottom: 12px;
}
.comment-item {
  display: flex;
  gap: 8px;
  padding: 10px 0;
  border-bottom: 1px solid var(--border-color);
}
.comment-item:last-child {
  border-bottom: none;
}
.comment-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  flex-shrink: 0;
}
.comment-body {
  flex: 1;
  min-width: 0;
}
.comment-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}
.comment-user {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}
.comment-time {
  font-size: 11px;
  color: var(--text-secondary);
}
.comment-content {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}
.comment-input {
  margin-top: 8px;
}
</style>

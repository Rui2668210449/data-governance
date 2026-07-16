<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { getWorkflowTasks, updateTaskStatus } from '@/api'
import type { WorkflowTask } from '@/types'

const loading = ref(false)
const tasks = ref<WorkflowTask[]>([])

const columns = [
  { key: 'TODO', title: '待办', color: '#0052d9', icon: 'file-paste' },
  { key: 'DOING', title: '进行中', color: '#ed7b2f', icon: 'time' },
  { key: 'DONE', title: '已完成', color: '#00a870', icon: 'check-circle' },
]

const typeMeta: Record<string, { text: string; color: string }> = {
  QUALITY: { text: '质量', color: '#00a870' },
  SECURITY: { text: '安全', color: '#e34d59' },
  STANDARD: { text: '标准', color: '#7265e6' },
  APPROVAL: { text: '审批', color: '#0052d9' },
}

const priorityMeta: Record<string, { text: string; color: string }> = {
  HIGH: { text: '高', color: '#e34d59' },
  MEDIUM: { text: '中', color: '#ed7b2f' },
  LOW: { text: '低', color: '#0052d9' },
}

const stats = computed(() => {
  const map: Record<string, number> = { TODO: 0, DOING: 0, DONE: 0 }
  tasks.value.forEach((t) => {
    map[t.status] = (map[t.status] || 0) + 1
  })
  return map
})

function getColTasks(status: string) {
  return tasks.value.filter((t) => t.status === status)
}

async function loadData() {
  loading.value = true
  try {
    tasks.value = await getWorkflowTasks()
  } finally {
    loading.value = false
  }
}

// ============ 原生 HTML5 拖拽 ============
const draggingId = ref<number | undefined>(undefined)

function onDragStart(e: DragEvent, task: WorkflowTask) {
  draggingId.value = task.id
  if (e.dataTransfer) {
    e.dataTransfer.effectAllowed = 'move'
    e.dataTransfer.setData('text/taskId', String(task.id))
  }
}

function onDragOver(e: DragEvent) {
  e.preventDefault()
  if (e.dataTransfer) e.dataTransfer.dropEffect = 'move'
}

async function onDrop(e: DragEvent, targetStatus: string) {
  e.preventDefault()
  const taskId = Number(e.dataTransfer?.getData('text/taskId'))
  draggingId.value = undefined
  if (!taskId) return
  const task = tasks.value.find((t) => t.id === taskId)
  if (!task) return
  if (task.status === targetStatus) return
  const oldStatus = task.status
  task.status = targetStatus as WorkflowTask['status']
  try {
    await updateTaskStatus(taskId, targetStatus)
    MessagePlugin.success(`任务已流转至「${columns.find((c) => c.key === targetStatus)?.title}」`)
  } catch {
    // 失败回滚
    task.status = oldStatus
    MessagePlugin.error('任务流转失败，已回滚')
  }
}

function onDragEnd() {
  draggingId.value = undefined
}

function isOverdue(task: WorkflowTask) {
  const today = new Date('2026-07-16').getTime()
  const due = new Date(task.dueDate).getTime()
  return due < today && task.status !== 'DONE'
}

onMounted(loadData)
</script>

<template>
  <div class="page-container" v-loading="loading">
    <!-- 顶部统计 -->
    <div class="page-card">
      <div class="flex-between" style="margin-bottom: 4px">
        <h3 class="page-title" style="margin: 0">治理看板</h3>
        <div class="flex gap-12 flex-center">
          <div
            v-for="col in columns"
            :key="col.key"
            class="stat-chip"
            :style="{ background: col.color + '15', color: col.color }"
          >
            <t-icon :name="col.icon" size="14" />
            <span>{{ col.title }}</span>
            <span class="stat-chip-num">{{ stats[col.key] }}</span>
          </div>
        </div>
      </div>
      <div class="hint-text">
        <t-icon name="info-circle" size="12" />
        提示：拖拽任务卡片到其他列即可流转状态
      </div>
    </div>

    <!-- 看板视图 -->
    <div class="board">
      <div
        v-for="col in columns"
        :key="col.key"
        class="board-col"
        :style="{ borderTopColor: col.color }"
        @dragover="onDragOver"
        @drop="onDrop($event, col.key)"
      >
        <div class="board-col-head">
          <div class="flex gap-8 flex-center">
            <span class="col-dot" :style="{ background: col.color }"></span>
            <span class="col-title">{{ col.title }}</span>
            <span class="col-count">{{ getColTasks(col.key).length }}</span>
          </div>
          <t-icon :name="col.icon" :style="{ color: col.color }" />
        </div>

        <div class="board-col-body">
          <div
            v-for="task in getColTasks(col.key)"
            :key="task.id"
            class="task-card"
            :class="{ dragging: draggingId === task.id }"
            draggable="true"
            @dragstart="onDragStart($event, task)"
            @dragend="onDragEnd"
          >
            <!-- 优先级色条 -->
            <div
              class="priority-bar"
              :style="{ background: priorityMeta[task.priority].color }"
            ></div>
            <div class="task-content">
              <div class="task-title">{{ task.title }}</div>
              <div class="task-desc">{{ task.description }}</div>
              <div class="task-meta">
                <t-tag
                  size="small"
                  :color="typeMeta[task.type].color"
                  variant="light"
                >
                  {{ typeMeta[task.type].text }}
                </t-tag>
                <t-tag
                  size="small"
                  :color="priorityMeta[task.priority].color"
                  variant="outline"
                >
                  {{ priorityMeta[task.priority].text }}优先级
                </t-tag>
              </div>
              <div class="task-foot">
                <div class="assignee">
                  <t-icon name="user-circle" size="14" />
                  <span>{{ task.assignee }}</span>
                </div>
                <div
                  class="due-date"
                  :class="{ overdue: isOverdue(task) }"
                >
                  <t-icon name="time" size="14" />
                  <span>{{ task.dueDate }}</span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="getColTasks(col.key).length === 0" class="col-empty">
            <t-icon name="folder-open" size="28" style="color: #d9d9d9" />
            <div>暂无任务</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-title {
  margin: 0;
}
.hint-text {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  font-size: 12px;
  color: var(--text-secondary);
}
.stat-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 14px;
  font-size: 13px;
  font-weight: 500;
}
.stat-chip-num {
  font-weight: 600;
}
.board {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.board-col {
  background: #fff;
  border-radius: 8px;
  border-top: 3px solid var(--brand-color);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  min-height: 520px;
}
.board-col-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid var(--border-color);
}
.col-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.col-title {
  font-size: 14px;
  font-weight: 600;
}
.col-count {
  display: inline-block;
  padding: 0 8px;
  height: 18px;
  line-height: 18px;
  background: #f0f2f5;
  border-radius: 9px;
  font-size: 12px;
  color: var(--text-secondary);
}
.board-col-body {
  flex: 1;
  padding: 10px;
  overflow-y: auto;
}
.task-card {
  background: #fff;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  margin-bottom: 10px;
  display: flex;
  cursor: grab;
  transition: all 0.15s;
  overflow: hidden;
}
.task-card:hover {
  border-color: var(--brand-color);
  box-shadow: 0 4px 10px rgba(0, 82, 217, 0.1);
}
.task-card:active {
  cursor: grabbing;
}
.task-card.dragging {
  opacity: 0.5;
  transform: rotate(2deg);
}
.priority-bar {
  width: 3px;
  flex-shrink: 0;
}
.task-content {
  flex: 1;
  padding: 10px 12px;
  min-width: 0;
}
.task-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  line-height: 1.4;
  margin-bottom: 4px;
}
.task-desc {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.4;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.task-meta {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}
.task-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 8px;
  border-top: 1px dashed var(--border-color);
  font-size: 12px;
  color: var(--text-secondary);
}
.assignee {
  display: flex;
  align-items: center;
  gap: 4px;
}
.due-date {
  display: flex;
  align-items: center;
  gap: 4px;
}
.due-date.overdue {
  color: var(--danger);
  font-weight: 500;
}
.col-empty {
  text-align: center;
  padding: 40px 0;
  color: var(--text-secondary);
  font-size: 13px;
}
.col-empty div {
  margin-top: 6px;
}
</style>

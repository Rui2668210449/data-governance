<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { getDatasources, saveDatasource, deleteDatasource } from '@/api'
import type { Datasource } from '@/types'

interface DatasourceForm {
  id?: number
  name?: string
  type?: 'MYSQL' | 'HIVE' | 'POSTGRESQL' | 'REST'
  jdbcUrl?: string
  username?: string
  password?: string
  status?: 'ACTIVE' | 'INACTIVE'
}

const list = ref<Datasource[]>([])
const loading = ref(true)
const keyword = ref('')

// 弹窗
const dialogVisible = ref(false)
const dialogMode = ref<'add' | 'edit'>('add')
const submitting = ref(false)
const formRef = ref()
const formData = ref<DatasourceForm>({
  name: '',
  type: 'MYSQL',
  jdbcUrl: '',
  username: '',
  password: '',
  status: 'ACTIVE',
})

const formRules = {
  name: [{ required: true, message: '请输入数据源名称', type: 'error' as const }],
  type: [{ required: true, message: '请选择类型', type: 'error' as const }],
  jdbcUrl: [{ required: true, message: '请输入JDBC地址', type: 'error' as const }],
  username: [{ required: true, message: '请输入用户名', type: 'error' as const }],
}

// 删除确认
const deleteVisible = ref(false)
const deleteTarget = ref<Datasource | null>(null)

const typeOptions = [
  { label: 'MySQL', value: 'MYSQL' },
  { label: 'Hive', value: 'HIVE' },
  { label: 'PostgreSQL', value: 'POSTGRESQL' },
  { label: 'REST API', value: 'REST' },
]

const typeColorMap: Record<string, string> = {
  MYSQL: '#0052d9',
  HIVE: '#ed7b2f',
  POSTGRESQL: '#00a870',
  REST: '#7265e6',
}

const filteredList = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return list.value
  return list.value.filter(
    (d) =>
      d.name.toLowerCase().includes(kw) ||
      d.type.toLowerCase().includes(kw) ||
      d.jdbcUrl.toLowerCase().includes(kw)
  )
})

async function loadData() {
  loading.value = true
  try {
    list.value = await getDatasources()
  } finally {
    loading.value = false
  }
}

function openAdd() {
  dialogMode.value = 'add'
  formData.value = {
    name: '',
    type: 'MYSQL',
    jdbcUrl: '',
    username: '',
    password: '',
    status: 'ACTIVE',
  }
  dialogVisible.value = true
}

function openEdit(row: Datasource) {
  dialogMode.value = 'edit'
  formData.value = { ...row }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate?.()
  if (valid !== true) return
  submitting.value = true
  try {
    await saveDatasource(formData.value)
    MessagePlugin.success(dialogMode.value === 'add' ? '数据源已创建' : '数据源已更新')
    dialogVisible.value = false
    await loadData()
  } catch (e) {
    MessagePlugin.error('保存失败')
  } finally {
    submitting.value = false
  }
}

function handleCollect(row: Datasource) {
  MessagePlugin.success(`采集任务已触发：${row.name}`)
}

function openDelete(row: Datasource) {
  deleteTarget.value = row
  deleteVisible.value = true
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  try {
    await deleteDatasource(deleteTarget.value.id)
    MessagePlugin.success('数据源已删除')
    deleteVisible.value = false
    await loadData()
  } catch (e) {
    MessagePlugin.error('删除失败')
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page-container">
    <!-- 工具栏 -->
    <div class="page-card">
      <div class="flex-between">
        <div class="page-title" style="margin: 0">数据源管理</div>
        <div class="flex gap-12">
          <t-input
            v-model="keyword"
            placeholder="搜索名称/类型/地址"
            clearable
            style="width: 260px"
          >
            <template #prefix-icon>
              <t-icon name="search" />
            </template>
          </t-input>
          <t-button theme="primary" @click="openAdd">
            <template #icon><t-icon name="add" /></template>
            新增数据源
          </t-button>
        </div>
      </div>
    </div>

    <!-- 数据源列表 -->
    <div class="page-card">
      <t-table
        :data="filteredList"
        row-key="id"
        :loading="loading"
        :columns="[
          { colKey: 'name', title: '名称', ellipsis: true },
          { colKey: 'type', title: '类型', width: 130, cell: 'type' },
          { colKey: 'jdbcUrl', title: 'JDBC地址', ellipsis: true },
          { colKey: 'status', title: '状态', width: 100, cell: 'status' },
          { colKey: 'tableCount', title: '表数量', width: 100 },
          { colKey: 'lastCollectTime', title: '最后采集时间', width: 180 },
          { colKey: 'op', title: '操作', width: 220, fixed: 'right', cell: 'op' },
        ]"
        size="small"
        :pagination="{ show: false }"
      >
        <template #type="{ row }">
          <t-tag size="small" variant="light" :color="typeColorMap[row.type]">
            {{ row.type }}
          </t-tag>
        </template>
        <template #status="{ row }">
          <t-tag
            size="small"
            variant="light"
            :theme="row.status === 'ACTIVE' ? 'success' : 'default'"
          >
            {{ row.status === 'ACTIVE' ? '启用' : '停用' }}
          </t-tag>
        </template>
        <template #op="{ row }">
          <t-space size="small">
            <t-button theme="primary" variant="text" size="small" @click="handleCollect(row)">
              采集
            </t-button>
            <t-button theme="primary" variant="text" size="small" @click="openEdit(row)">
              编辑
            </t-button>
            <t-button theme="danger" variant="text" size="small" @click="openDelete(row)">
              删除
            </t-button>
          </t-space>
        </template>
      </t-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <t-dialog
      v-model:visible="dialogVisible"
      :header="dialogMode === 'add' ? '新增数据源' : '编辑数据源'"
      :confirm-btn="{ content: '保存', loading: submitting }"
      cancel-btn="取消"
      width="520px"
      @confirm="handleSubmit"
    >
      <t-form
        ref="formRef"
        :data="formData"
        :rules="formRules"
        label-align="right"
        label-width="100px"
        style="margin-top: 12px"
      >
        <t-form-item label="名称" name="name">
          <t-input v-model="formData.name" placeholder="请输入数据源名称" />
        </t-form-item>
        <t-form-item label="类型" name="type">
          <t-select v-model="formData.type" :options="typeOptions" />
        </t-form-item>
        <t-form-item label="JDBC地址" name="jdbcUrl">
          <t-input v-model="formData.jdbcUrl" placeholder="如 jdbc:mysql://host:port/db" />
        </t-form-item>
        <t-form-item label="用户名" name="username">
          <t-input v-model="formData.username" placeholder="请输入用户名" />
        </t-form-item>
        <t-form-item label="密码" name="password">
          <t-input v-model="formData.password" type="password" placeholder="请输入密码" />
        </t-form-item>
      </t-form>
    </t-dialog>

    <!-- 删除确认 -->
    <t-dialog
      v-model:visible="deleteVisible"
      header="删除确认"
      theme="danger"
      :confirm-btn="{ content: '确定删除', theme: 'danger' }"
      cancel-btn="取消"
      @confirm="confirmDelete"
    >
      <div style="padding: 8px 0">
        确定要删除数据源 <b>{{ deleteTarget?.name }}</b> 吗？此操作不可恢复。
      </div>
    </t-dialog>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}
</style>

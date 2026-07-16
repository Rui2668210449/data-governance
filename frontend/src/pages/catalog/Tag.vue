<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getAssetTags } from '@/api'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import type { AssetTag } from '@/types'

const tags = ref<AssetTag[]>([])
const selectedCategory = ref('全部')
const dialogVisible = ref(false)
const editMode = ref(false)
const editId = ref<number | null>(null)

const form = ref({ name: '', category: '', color: '#0052d9' })
const categories = ref(['重要等级', '业务域', '数仓分层', '资产类型'])
const colorOptions = ['#0052d9', '#00a870', '#ed7b2f', '#e34d59', '#7265e6', '#8c8c8c', '#00a8e5']

async function load() {
  tags.value = await getAssetTags()
}

const groupTree = computed(() => {
  const map: Record<string, AssetTag[]> = {}
  tags.value.forEach((t) => {
    if (!map[t.category]) map[t.category] = []
    map[t.category].push(t)
  })
  return Object.entries(map).map(([category, items]) => ({
    label: `${category} (${items.length})`,
    value: category,
    children: items.map((i) => ({ label: i.name, value: i.id.toString() })),
  }))
})

const filteredTags = computed(() => {
  if (selectedCategory.value === '全部') return tags.value
  return tags.value.filter((t) => t.category === selectedCategory.value)
})

function openCreate() {
  editMode.value = false
  editId.value = null
  form.value = { name: '', category: categories.value[0], color: '#0052d9' }
  dialogVisible.value = true
}

function openEdit(tag: AssetTag) {
  editMode.value = true
  editId.value = tag.id
  form.value = { name: tag.name, category: tag.category, color: tag.color }
  dialogVisible.value = true
}

function onSubmit() {
  if (!form.value.name) {
    MessagePlugin.error('请输入标签名称')
    return
  }
  if (editMode.value && editId.value !== null) {
    const idx = tags.value.findIndex((t) => t.id === editId.value)
    if (idx >= 0) {
      tags.value[idx] = { ...tags.value[idx], ...form.value }
    }
  } else {
    tags.value.push({ id: Date.now(), ...form.value })
  }
  dialogVisible.value = false
  MessagePlugin.success(editMode.value ? '修改成功' : '创建成功')
}

function onDelete(tag: AssetTag) {
  const d = DialogPlugin.confirm({
    header: '确认删除',
    body: `确定删除标签「${tag.name}」吗？`,
    theme: 'danger',
    onConfirm: () => {
      tags.value = tags.value.filter((t) => t.id !== tag.id)
      d.destroy()
      MessagePlugin.success('删除成功')
    },
  })
}

function onSelect(category: string) {
  selectedCategory.value = category
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <div class="flex-between page-card" style="margin-bottom:16px">
      <h2 class="page-title" style="margin:0">标签体系管理</h2>
      <t-button theme="primary" @click="openCreate">
        <template #icon><t-icon name="add" /></template>
        新建标签
      </t-button>
    </div>

    <div class="tag-layout">
      <aside class="tag-tree">
        <div class="page-card" style="margin-bottom:0">
          <div style="font-weight:600;margin-bottom:12px">标签类目</div>
          <div class="category-item" :class="{ active: selectedCategory === '全部' }" @click="onSelect('全部')">
            <t-icon name="folder" size="14" /> 全部 ({{ tags.length }})
          </div>
          <div v-for="item in groupTree" :key="item.value" class="category-group">
            <div class="category-item" :class="{ active: selectedCategory === item.value }" @click="onSelect(item.value)">
              <t-icon name="folder" size="14" /> {{ item.label }}
            </div>
          </div>
        </div>
      </aside>

      <div class="tag-grid">
        <div v-for="tag in filteredTags" :key="tag.id" class="tag-card">
          <div class="tag-dot" :style="{ background: tag.color }"></div>
          <div class="tag-name">{{ tag.name }}</div>
          <div class="tag-category">{{ tag.category }}</div>
          <div class="tag-actions">
            <t-button theme="default" variant="text" size="small" @click="openEdit(tag)">
              <template #icon><t-icon name="edit" /></template>
            </t-button>
            <t-button theme="danger" variant="text" size="small" @click="onDelete(tag)">
              <template #icon><t-icon name="delete" /></template>
            </t-button>
          </div>
        </div>
      </div>
    </div>

    <t-dialog v-model:visible="dialogVisible" :header="editMode ? '编辑标签' : '新建标签'">
      <t-form :data="form" label-width="80px">
        <t-form-item label="标签名称" name="name">
          <t-input v-model="form.name" placeholder="请输入标签名称" />
        </t-form-item>
        <t-form-item label="所属类目" name="category">
          <t-select v-model="form.category" :options="categories.map((c) => ({ label: c, value: c }))" />
        </t-form-item>
        <t-form-item label="颜色">
          <div class="color-picker">
            <div v-for="c in colorOptions" :key="c" class="color-option" :class="{ selected: form.color === c }" :style="{ background: c }" @click="form.color = c"></div>
          </div>
        </t-form-item>
      </t-form>
      <template #footer>
        <t-button theme="primary" @click="onSubmit">确定</t-button>
        <t-button theme="default" @click="dialogVisible = false">取消</t-button>
      </template>
    </t-dialog>
  </div>
</template>

<style scoped>
.tag-layout {
  display: flex;
  gap: 16px;
}
.tag-tree {
  width: 240px;
  flex-shrink: 0;
}
.category-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.15s;
}
.category-item:hover {
  background: var(--bg-page);
}
.category-item.active {
  background: rgba(0, 82, 217, 0.08);
  color: var(--brand-color);
  font-weight: 500;
}
.category-group {
  margin-left: 8px;
}
.tag-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
  align-content: start;
}
.tag-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
}
.tag-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}
.tag-dot {
  width: 24px;
  height: 24px;
  border-radius: 50%;
}
.tag-name {
  font-size: 14px;
  font-weight: 500;
}
.tag-category {
  font-size: 12px;
  color: var(--text-secondary);
}
.tag-actions {
  display: flex;
  gap: 8px;
  margin-top: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}
.tag-card:hover .tag-actions {
  opacity: 1;
}
.color-picker {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.color-option {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  cursor: pointer;
  border: 2px solid transparent;
}
.color-option.selected {
  border-color: #181818;
}
</style>

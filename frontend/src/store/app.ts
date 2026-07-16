import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const collapsed = ref(false)
  const userName = ref('数据治理管理员')
  const userRole = ref('admin')

  function toggleCollapsed() {
    collapsed.value = !collapsed.value
  }

  function setCollapsed(val: boolean) {
    collapsed.value = val
  }

  return { collapsed, userName, userRole, toggleCollapsed, setCollapsed }
})

<script setup lang="ts">
import AppSidebar from './AppSidebar.vue'
import AppHeader from './AppHeader.vue'
import { useAppStore } from '@/store/app'

const appStore = useAppStore()
</script>

<template>
  <div class="app-layout">
    <aside class="app-sidebar" :class="{ collapsed: appStore.collapsed }">
      <AppSidebar />
    </aside>
    <div class="app-main">
      <AppHeader />
      <main class="app-content">
        <router-view v-slot="{ Component }">
          <transition name="route-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<style scoped>
.app-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}
.app-sidebar {
  width: 240px;
  flex-shrink: 0;
  transition: width 0.2s ease;
  overflow: hidden;
}
.app-sidebar.collapsed {
  width: 64px;
}
.app-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.app-content {
  flex: 1;
  overflow-y: auto;
  background: var(--bg-page);
}
</style>

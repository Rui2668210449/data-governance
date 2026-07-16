<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'

const props = defineProps<{
  text?: string
  opacity?: number
  fontSize?: number
  rotate?: number
}>()

const canvasRef = ref<HTMLCanvasElement>()
const watermarkRef = ref<HTMLDivElement>()

const defaultText = props.text || 'Written by RCF'
const defaultOpacity = props.opacity || 0.05
const defaultFontSize = props.fontSize || 16
const defaultRotate = props.rotate || -15

function createWatermark() {
  const canvas = canvasRef.value
  const container = watermarkRef.value
  if (!canvas || !container) return

  const ctx = canvas.getContext('2d')
  if (!ctx) return

  const width = 200
  const height = 100
  canvas.width = width
  canvas.height = height

  ctx.font = `${defaultFontSize}px sans-serif`
  ctx.fillStyle = `rgba(0, 0, 0, ${defaultOpacity})`
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.translate(width / 2, height / 2)
  ctx.rotate((defaultRotate * Math.PI) / 180)
  ctx.fillText(defaultText, 0, 0)

  const url = canvas.toDataURL()
  container.style.backgroundImage = `url(${url})`
}

let resizeObserver: ResizeObserver | null = null

onMounted(() => {
  createWatermark()
  resizeObserver = new ResizeObserver(() => {
    createWatermark()
  })
  const container = watermarkRef.value
  if (container) {
    resizeObserver.observe(container)
  }
})

onUnmounted(() => {
  resizeObserver?.disconnect()
})

watch(() => [props.text, props.opacity, props.fontSize, props.rotate], () => {
  createWatermark()
})
</script>

<template>
  <div
    ref="watermarkRef"
    class="watermark-container"
  >
    <canvas ref="canvasRef" class="watermark-canvas"></canvas>
  </div>
</template>

<style scoped>
.watermark-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  pointer-events: none;
  z-index: 9999;
  background-repeat: repeat;
  background-size: 200px 100px;
}

.watermark-canvas {
  display: none;
}
</style>

const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 7070,  // 前端开发服务器端口
    proxy: {
      '^/ws/download': {
        target: 'ws://localhost:8088',
        ws: true,
        changeOrigin: true
      },
      '^/ws/logs': {
        target: 'ws://localhost:8088',
        ws: true,
        changeOrigin: true
      },
      '/': {
        target: 'http://localhost:8088',  // 后端 HTTP 服务地址
        ws: false,
        changeOrigin: true
      }
    },
  }
})
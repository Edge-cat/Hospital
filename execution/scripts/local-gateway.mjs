/**
 * 本地 Nginx 联调替代：静态资源 + /api 反代（无需安装 Nginx）
 * 用法: node execution/scripts/local-gateway.mjs
 */
import http from 'node:http'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const root = path.resolve(__dirname, '../..')
const PORT = 8088
const BACKEND = 'http://127.0.0.1:8080'

const sites = {
  '/admin/': path.join(root, 'hospital-admin/dist'),
  '/user/': path.join(root, 'hospital-user/dist'),
}

function sendFile(res, filePath) {
  const ext = path.extname(filePath)
  const types = { '.html': 'text/html', '.js': 'application/javascript', '.css': 'text/css', '.svg': 'image/svg+xml', '.png': 'image/png', '.ico': 'image/x-icon', '.json': 'application/json', '.woff2': 'font/woff2' }
  res.writeHead(200, { 'Content-Type': types[ext] || 'application/octet-stream' })
  fs.createReadStream(filePath).pipe(res)
}

function proxyApi(req, res) {
  const url = BACKEND + req.url
  const headers = { ...req.headers, host: '127.0.0.1:8080' }
  const opts = { method: req.method, headers }
  const proxyReq = http.request(url, opts, (proxyRes) => {
    res.writeHead(proxyRes.statusCode, proxyRes.headers)
    proxyRes.pipe(res)
  })
  proxyReq.on('error', (e) => {
    res.writeHead(502, { 'Content-Type': 'text/plain' })
    res.end('Bad Gateway: ' + e.message)
  })
  if (req.method === 'GET' || req.method === 'HEAD') {
    proxyReq.end()
  } else {
    req.pipe(proxyReq)
  }
}

const server = http.createServer((req, res) => {
  if (req.url.startsWith('/api/')) {
    return proxyApi(req, res)
  }
  if (req.url === '/') {
    res.writeHead(302, { Location: '/user/' })
    return res.end()
  }
  for (const [prefix, dir] of Object.entries(sites)) {
    if (req.url.startsWith(prefix)) {
      let rel = req.url.slice(prefix.length - 1) || '/index.html'
      if (rel.endsWith('/')) rel += 'index.html'
      const filePath = path.join(dir, rel)
      if (fs.existsSync(filePath) && fs.statSync(filePath).isFile()) {
        return sendFile(res, filePath)
      }
      const fallback = path.join(dir, 'index.html')
      if (fs.existsSync(fallback)) return sendFile(res, fallback)
    }
  }
  res.writeHead(404)
  res.end('Not Found')
})

server.listen(PORT, () => console.log(`Gateway http://127.0.0.1:${PORT} (admin/user + /api proxy)`))

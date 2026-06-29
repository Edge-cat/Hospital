const fs = require('fs')
const path = require('path')

const root = path.join(__dirname, '../src/views')

function walk(dir, files = []) {
  for (const f of fs.readdirSync(dir)) {
    const p = path.join(dir, f)
    if (fs.statSync(p).isDirectory()) walk(p, files)
    else if (f.endsWith('.vue')) files.push(p)
  }
  return files
}

const deptImport = "import { DEPARTMENTS } from '@/constants'"
const deptLine = "const departments = ['内科', '外科', '儿科', '骨科', '眼科']"

for (const file of walk(root)) {
  let c = fs.readFileSync(file, 'utf8')
  let changed = false

  const styleRe = /\n<style scoped>\s*\.pagination\s*\{[^}]+\}\s*<\/style>\s*$/
  if (styleRe.test(c)) {
    c = c.replace(styleRe, '\n')
    changed = true
  }

  const styleRe2 = /\n<style scoped>\.pagination\s*\{[^}]+\}<\/style>\s*$/
  if (styleRe2.test(c)) {
    c = c.replace(styleRe2, '\n')
    changed = true
  }

  if (c.includes('layout="total, prev, pager, next"')) {
    c = c.replace(
      /layout="total, prev, pager, next" class="pagination"/g,
      'class="table-pagination" layout="total, sizes, prev, pager, next, jumper" background'
    )
    changed = true
  }

  if (c.includes('class="pagination"') && c.includes('el-pagination') && !c.includes('AppPagination')) {
    c = c.replace(/class="pagination"/g, 'class="table-pagination" background')
    if (!c.includes('layout="total, sizes')) {
      c = c.replace(/<el-pagination(\s+)/g, '<el-pagination layout="total, sizes, prev, pager, next, jumper"$1')
    }
    changed = true
  }

  if (c.includes(deptLine)) {
    c = c.replace(deptLine, '')
    if (!c.includes("from '@/constants'")) {
      c = c.replace(/(<script setup>\n)/, `$1${deptImport}\n`)
    }
    c = c.replace(/\bdepartments\b/g, 'DEPARTMENTS')
    changed = true
  }

  if (changed) {
    fs.writeFileSync(file, c)
    console.log('updated:', path.relative(root, file))
  }
}

console.log('done')

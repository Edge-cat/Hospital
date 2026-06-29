<script>
import { resolvePageAudit } from './utils/auditMap'
import { reportOperation } from './utils/operationReport'

export default {
  onLaunch() {
    console.log('东软云医院小程序启动')
  },
  onShow() {
    const pages = getCurrentPages()
    const current = pages[pages.length - 1]
    if (!current) return
    const route = '/' + (current.route || '')
    const title = current.$page?.meta?.navigationBarTitleText
    const audit = resolvePageAudit(route, title)
    if (audit) reportOperation(audit)
  }
}
</script>

<style lang="scss">
@import './styles/common.scss';
</style>

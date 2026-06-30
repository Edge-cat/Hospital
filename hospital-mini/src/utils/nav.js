/** 小程序统一导航 — 避免 webview 栈溢出（上限 10 层） */

export const LOGIN_PATH = '/pages/login/login'
const TAB_PATHS = new Set([
  '/pages/index/index',
  '/pages/visit/visit',
  '/pages/payment/payment',
  '/pages/mine/mine'
])
/** 接近上限时改用 redirectTo */
const STACK_SOFT_LIMIT = 7

function getPages() {
  return getCurrentPages()
}

function currentRoute() {
  const pages = getPages()
  return pages.length ? `/${pages[pages.length - 1].route}` : ''
}

function routeOf(url) {
  return (url || '').split('?')[0]
}

/** 普通子页跳转（自动在栈深时降级为 redirectTo） */
export function safeNavigateTo(url) {
  const target = routeOf(url)
  if (!target || currentRoute() === target) return

  if (TAB_PATHS.has(target)) {
    uni.redirectTo({ url, fail: () => uni.reLaunch({ url }) })
    return
  }

  const depth = getPages().length
  if (depth >= STACK_SOFT_LIMIT) {
    uni.redirectTo({ url, fail: () => uni.reLaunch({ url: '/pages/index/index' }) })
  } else {
    uni.navigateTo({
      url,
      fail: () => uni.redirectTo({ url, fail: () => uni.reLaunch({ url: '/pages/index/index' }) })
    })
  }
}

/** 跳转登录 — 栈中已有登录页或栈过深时用 redirectTo，避免重复压栈 */
export function navigateToLogin() {
  if (currentRoute() === LOGIN_PATH) return

  const pages = getPages()
  const hasLoginPage = pages.some((p) => `/${p.route}` === LOGIN_PATH)

  if (hasLoginPage || pages.length >= STACK_SOFT_LIMIT) {
    uni.redirectTo({ url: LOGIN_PATH })
  } else {
    uni.navigateTo({
      url: LOGIN_PATH,
      fail: () => uni.redirectTo({ url: LOGIN_PATH })
    })
  }
}

export function goHome() {
  uni.reLaunch({ url: '/pages/index/index' })
}

export function goBackOrHome() {
  const pages = getPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    goHome()
  }
}

export function navigateAfterAuth(successTitle = '登录成功') {
  uni.showToast({ title: successTitle, icon: 'success' })
  setTimeout(() => {
    goBackOrHome()
  }, 800)
}

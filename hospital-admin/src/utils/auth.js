/**
 * 鉴权工具（与 Store / Router 配合使用）
 */

/** 路由 meta.roles 是否包含当前角色 */
export function hasRole(meta, role) {
  const roles = meta?.roles
  if (!roles?.length) return true
  return roles.includes(role)
}

/** 批量过滤带 roles 的路由配置 */
export function filterByRole(routes, role) {
  return routes.filter((route) => {
    if (!hasRole(route.meta, role)) return false
    if (route.children?.length) {
      route.children = filterByRole(route.children, role)
    }
    return true
  })
}

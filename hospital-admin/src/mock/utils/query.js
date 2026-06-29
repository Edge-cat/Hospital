export function parseQuery(url) {
  const query = {}
  const qs = url.split('?')[1]
  if (!qs) return query
  qs.split('&').forEach((pair) => {
    const [k, v] = pair.split('=')
    query[decodeURIComponent(k)] = decodeURIComponent(v || '')
  })
  return query
}

export function parseBody(options) {
  try {
    return JSON.parse(options.body || '{}')
  } catch {
    return {}
  }
}

export function matchId(url) {
  const m = url.match(/\/(\d+)(?:\/|$|\?)/)
  return m ? Number(m[1]) : null
}

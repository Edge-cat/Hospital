/** 从18位身份证号解析性别(1男2女)与年龄 */
export function parseIdCard(idCard) {
  const id = String(idCard || '').trim()
  if (!/^\d{17}[\dXx]$/.test(id)) return null

  const birth = id.slice(6, 14)
  const year = Number(birth.slice(0, 4))
  const month = Number(birth.slice(4, 6))
  const day = Number(birth.slice(6, 8))
  const birthDate = new Date(year, month - 1, day)
  if (Number.isNaN(birthDate.getTime())) return null

  const today = new Date()
  let age = today.getFullYear() - year
  if (today.getMonth() + 1 < month || (today.getMonth() + 1 === month && today.getDate() < day)) {
    age -= 1
  }

  const genderCode = Number(id.charAt(16))
  return {
    gender: genderCode % 2 === 1 ? 1 : 2,
    age: Math.max(0, Math.min(150, age)),
    birthDate: `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
  }
}

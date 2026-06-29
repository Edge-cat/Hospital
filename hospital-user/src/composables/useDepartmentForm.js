import { ref, onMounted } from 'vue'
import { userApi } from '@/api'

/** 科室 / 医生联动选择 */
export function useDepartmentForm(form, { autoLoad = true } = {}) {
  const departments = ref([])
  const doctors = ref([])
  const loadingDepts = ref(false)

  async function loadDepartments() {
    loadingDepts.value = true
    try {
      const res = await userApi.departments()
      departments.value = res.data.list.filter((d) => d.parentId === 0)
    } finally {
      loadingDepts.value = false
    }
  }

  async function loadDoctors(keepDoctor = false) {
    if (!keepDoctor) form.doctorName = ''
    if (!form.department) {
      doctors.value = []
      return
    }
    const res = await userApi.doctors({ department: form.department })
    doctors.value = res.data.list
  }

  async function initFromQuery(departmentName, doctorName) {
    if (!departmentName) return
    form.department = departmentName
    await loadDoctors(!!doctorName)
    if (doctorName) form.doctorName = doctorName
  }

  if (autoLoad) onMounted(loadDepartments)

  return { departments, doctors, loadingDepts, loadDepartments, loadDoctors, initFromQuery }
}

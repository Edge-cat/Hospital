import { ref } from 'vue'
import { aiApi } from '@/api'
import { AI_DOCTOR_INITIAL_QUESTION } from '@/constants/aiDoctorAssist'

export function useDoctorAiAssist() {
  const visible = ref(false)
  const patient = ref(null)
  const context = ref(null)
  const messages = ref([])
  const loading = ref(false)
  const input = ref('')
  const disclaimer = ref('')
  const demoMode = ref(false)

  let formRef = null
  let collectExamsFn = null

  function buildContext(activePatient, consultForm, collectExamNames) {
    return {
      patientName: activePatient?.name,
      patientNo: activePatient?.patientNo,
      gender: activePatient?.gender,
      age: activePatient?.age,
      department: activePatient?.department,
      chiefComplaint: consultForm.chiefComplaint,
      presentIllness: consultForm.presentIllness,
      diagnosis: consultForm.diagnosis,
      exams: collectExamNames(),
      examNote: consultForm.examNote,
      drugs: consultForm.drugs
        .filter((d) => d.name?.trim())
        .map((d) => ({ name: d.name, qty: d.qty, usage: d.usage }))
    }
  }

  function syncContext() {
    if (patient.value && formRef && collectExamsFn) {
      context.value = buildContext(patient.value, formRef, collectExamsFn)
    }
  }

  async function open(activePatient, consultForm, collectExamNames) {
    patient.value = activePatient
    formRef = consultForm
    collectExamsFn = collectExamNames
    syncContext()
    messages.value = []
    input.value = ''
    disclaimer.value = ''
    demoMode.value = false
    visible.value = true
    await send(AI_DOCTOR_INITIAL_QUESTION)
  }

  function close() {
    visible.value = false
    patient.value = null
    context.value = null
    formRef = null
    collectExamsFn = null
    messages.value = []
    input.value = ''
    loading.value = false
  }

  async function send(content) {
    const text = String(content || '').trim()
    if (!text || loading.value || !context.value) return

    syncContext()
    messages.value.push({ role: 'user', content: text })
    loading.value = true
    try {
      const res = await aiApi.doctorAssist({
        patientId: patient.value?.id,
        context: context.value,
        messages: messages.value.map(({ role, content: c }) => ({ role, content: c }))
      })
      disclaimer.value = res.data?.disclaimer || ''
      demoMode.value = !!res.data?.demoMode
      messages.value.push({ role: 'assistant', content: res.data?.reply || '暂无回复' })
    } catch (e) {
      messages.value.pop()
      throw e
    } finally {
      loading.value = false
      input.value = ''
    }
  }

  return {
    visible,
    patient,
    messages,
    loading,
    input,
    disclaimer,
    demoMode,
    open,
    close,
    send
  }
}

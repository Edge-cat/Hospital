/**
 * 医生权威档案（批量维护）
 * API 未返回 education/degree 时，按姓名 enrich 展示
 */
export const DOCTOR_PROFILE = {
  张明: {
    education: '毕业于北京协和医学院',
    degree: '医学博士',
    title: '主任医师',
    specialty: '心血管疾病的诊断与介入治疗，擅长高血压、冠心病、心律失常及心力衰竭的个体化综合管理'
  },
  王芳: {
    education: '毕业于首都医科大学儿科系',
    degree: '医学硕士',
    title: '副主任医师',
    specialty: '儿童常见病与多发病诊治，擅长儿科保健、生长发育评估及反复呼吸道感染管理'
  },
  李强: {
    education: '毕业于复旦大学上海医学院',
    degree: '医学硕士',
    title: '主治医师',
    specialty: '普外科常见疾病诊疗，擅长腹腔镜微创手术、甲状腺及疝气外科治疗'
  },
  赵敏: {
    education: '毕业于四川大学华西医学院',
    degree: '医学博士',
    title: '主任医师',
    specialty: '复杂骨折与关节置换手术，擅长脊柱退变性疾病及运动损伤的规范化治疗'
  },
  刘洋: {
    education: '毕业于中山大学中山眼科中心',
    degree: '医学硕士',
    title: '主治医师',
    specialty: '白内障、青光眼及眼底病筛查，擅长屈光不正与干眼症的系统化管理'
  },
  陈静: {
    education: '毕业于中国医科大学',
    degree: '医学硕士',
    title: '副主任医师',
    specialty: '湿疹、痤疮、银屑病等常见皮肤病，擅长皮肤镜检测与激光美容相关治疗'
  },
  杨帆: {
    education: '毕业于武汉大学口腔医学院',
    degree: '医学硕士',
    title: '主治医师',
    specialty: '口腔内科与修复治疗，擅长牙体牙髓病、固定修复及儿童口腔保健指导'
  },
  黄磊: {
    education: '毕业于华中科技大学同济医学院',
    degree: '医学博士',
    title: '主任医师',
    specialty: '肝胆胰脾外科疾病，擅长腹腔镜肝切除、胆道结石及胰腺肿瘤的综合外科治疗'
  },
  林娜: {
    education: '毕业于重庆医科大学',
    degree: '医学硕士',
    title: '主治医师',
    specialty: '小儿呼吸系统疾病，擅长儿童哮喘、肺炎及反复咳嗽的规范化诊疗'
  },
  马超: {
    education: '毕业于北京大学医学部',
    degree: '医学博士',
    title: '副主任医师',
    specialty: '脊柱外科与微创技术，擅长颈椎病、腰椎间盘突出及骨质疏松性骨折治疗'
  },
  周伟: {
    education: '毕业于上海交通大学医学院',
    degree: '医学硕士',
    title: '主治医师',
    specialty: '消化内科常见疾病，擅长慢性胃炎、功能性胃肠病及早期消化道肿瘤筛查'
  },
  李华: {
    education: '毕业于浙江大学医学院',
    degree: '医学硕士',
    title: '副主任医师',
    specialty: '普外及微创外科，擅长胃肠道肿瘤、阑尾炎及腹壁疝的手术治疗'
  },
  孙磊: {
    education: '毕业于中南大学湘雅医学院',
    degree: '医学博士',
    title: '主任医师',
    specialty: '肝胆胰外科与腹腔镜技术，擅长复杂胆道疾病及胰腺外科高难度手术'
  },
  吴敏: {
    education: '毕业于南京医科大学',
    degree: '医学硕士',
    title: '副主任医师',
    specialty: '小儿呼吸与过敏性疾病，擅长儿童慢性咳嗽、哮喘及肺炎的随访管理'
  },
  赵强: {
    education: '毕业于山东大学齐鲁医学院',
    degree: '医学博士',
    title: '主任医师',
    specialty: '关节外科与运动医学，擅长人工关节置换、运动损伤修复及骨关节炎阶梯治疗'
  },
  郑涛: {
    education: '毕业于天津医科大学',
    degree: '医学硕士',
    title: '主治医师',
    specialty: '运动损伤与关节镜技术，擅长半月板损伤、韧带重建及肩肘关节疾病治疗'
  },
  黄丽: {
    education: '毕业于中山大学中山眼科中心',
    degree: '医学硕士',
    title: '副主任医师',
    specialty: '白内障超声乳化与人工晶体植入，擅长老年性白内障及并发性白内障手术'
  }
}

export function withDoctorProfile(doc) {
  if (!doc) return doc
  const meta = DOCTOR_PROFILE[doc.name] || {}
  return {
    ...doc,
    education: doc.education || meta.education || '',
    degree: doc.degree || meta.degree || '',
    title: doc.title || meta.title || '',
    specialty: doc.specialty || meta.specialty || '',
    profileReady: !!(doc.education || meta.education)
  }
}

export function mapDoctorList(list) {
  return (list || []).map(withDoctorProfile)
}

<template>
  <el-row :gutter="20">
    <el-col :xs="24" :md="7" :lg="6">
      <ProfileSidebar />
    </el-col>
    <el-col :xs="24" :md="17" :lg="18">
      <slot />
    </el-col>
  </el-row>
</template>

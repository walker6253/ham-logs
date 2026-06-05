# HAM 日志 — 业余无线电通联日志

一款面向业余无线电爱好者（HAM）的 Android 通联日志应用。支持快速录入、历史查询、ADIF 导入导出、Cloudlog 云端同步、设备天馈管理等功能。

## 功能概览

### 通联日志
- **今日通联** — 按日期分组展示通联记录，卡片式布局，模式彩色标签（USB 绿 / LSB 橙 / FM 红 等）
- **历史查询** — 点击首页 + 号选择日期，跳转至对应日期的通联记录
- **左右滑动删除** — 通联记录支持左右滑动快速删除，带红色垃圾桶图标
- **呼号联想** — 输入呼号时自动匹配历史记录，支持将小写字母自动转大写
- **B 台分区识别** — 输入 B 开头呼号时，根据第三位数字自动显示对应省份（灰色小字）

### 日志录入
- **频率/模式/波段** — 快捷选择常用频率、模式和波段，支持自定义输入
- **信号报告 (RST)** — 自定义 RST 数字键盘，1-5 行渐变红→绿，1-9 行渐变红→绿，快速组合输入
- **功率输入** — 自定义功率数字键盘，1-0 排列，含清空和删除键
- **快捷标签** — 59/58/57/56 等 RST 快捷标签渐变绿→黄，5W/10W/25W/50W/100W 功率标签渐变绿→红，选中边框高亮
- **智能输入** — 支持一次性输入呼号、频率、模式等，自动解析填充
- **位置/GPS** — 自动获取当前位置并计算梅登海德网格坐标
- **键盘覆盖模式** — 软键盘弹出时覆盖页面而不挤压布局

### 设置
- **OP 设置** — 呼号、姓名、设备、位置、网格坐标
- **设备天馈维护** — 天线品牌和型号的分组管理，支持长按拖拽排序、左右滑动删除、拖拽插入动画
- **时区** — 默认 UTC+8，支持下拉选择其他时区
- **ADIF 导入/导出** — 支持标准 ADIF 格式导入导出，自动解析日期创建对应记录
- **Cloudlog 同步** — 支持连接 Cloudlog 站台，ADIF 格式上传 QSO，自动上传开关，同步结果显示成功/失败详情和服务器响应
- **台站选择** — 测试连接后自动拉取可用台站列表，下拉选择并缓存
- **检查更新** — 从 GitHub Release 获取最新版本信息

### 界面
- **WindowSizeClass 自适应** — 根据屏幕 dp 宽度自动分三档（Compact/Medium/Expanded）适配间距和字号
- **深色模式** — 完整支持 Material3 深色主题
- **字体规范** — Noto Serif（衬线体）用于标题/呼号/通联数据，Space Grotesk（无衬线体）用于标签/按钮/次要信息

## 技术栈

- **语言**：Kotlin
- **UI 框架**：Jetpack Compose + Material3
- **架构**：MVVM（ViewModel + StateFlow）
- **数据库**：Room
- **构建**：Gradle (Kotlin DSL)

## 开发环境

- Android Studio Hedgehog+
- JDK 17
- minSdk 26 / targetSdk 34

## 构建

```bash
# Debug
./gradlew assembleDebug

# Release
./gradlew assembleRelease
```

## Star History

<a href="https://www.star-history.com/?repos=walker6253%2Fham-logs&type=date&legend=bottom-right">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/chart?repos=walker6253/ham-logs&type=date&theme=dark&legend=top-left" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/chart?repos=walker6253/ham-logs&type=date&legend=top-left" />
   <img alt="Star History Chart" src="https://api.star-history.com/chart?repos=walker6253/ham-logs&type=date&legend=top-left" />
 </picture>
</a>

GitHub: [walker6253/ham-logs](https://github.com/walker6253/ham-logs)

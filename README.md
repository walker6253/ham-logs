# 业余无线电通联日志 (HAM Log)

基于 Jetpack Compose 开发的业余无线电通联日志 Android 应用。

## 功能

- **通联录入** — 智能解析呼号、频率、模式、信号报告，支持自定义键盘快速输入 RST 和功率
- **通联记录** — 按日期查看历史通联，支持左右滑动删除和编辑
- **呼号联想** — 输入呼号自动匹配历史记录，B 开头呼号显示对应省份
- **设备维护** — 管理天馈和设备型号，录入时快捷标签选择
- **位置获取** — 一键获取当前位置，自动计算 Maidenhead 网格坐标，反向地理编码显示省市区
- **ADIF 导入** — 支持导入标准 ADIF 格式日志
- **深色模式** — 自动适配系统深色/浅色主题
- **字体规范** — 标题和通联数据使用 Noto Serif（衬线体），标签使用 Space Grotesk（无衬线体）
- **检查更新** — 连接 GitHub Releases 检查新版本

## 技术栈

- Kotlin + Jetpack Compose
- Material 3 (Material You)
- Room 数据库
- MVVM 架构
- Navigation Compose

## 构建

```bash
./gradlew assembleDebug    # Debug 包
./gradlew assembleRelease  # Release 包（需配置签名）
```

## 作者

Designed by [BI9BRH](https://github.com/walker6253)

## 协议

[MIT License](LICENSE)

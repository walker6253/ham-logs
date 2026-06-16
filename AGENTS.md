# AGENTS.md 指令文件

## 基本规则
1. **必须使用中文**回答我的所有问题。
2. 代码中**所有注释必须使用中文**。

## 构建约束
3. **每次问答完成后**，必须执行以下操作：
   - 运行 `flutter build apk --debug` 构建 Debug APK。
   - 在回复中明确告知 APK 文件的位置（默认路径为 `build\app\outputs\flutter-apk\app-debug.apk`）。
   - 如果构建失败，需说明失败原因并尝试修复。

## 推送约束
4. **每次修改代码后**，必须执行以下操作：
   - `git add` 相关的修改文件。
   - `git commit -m "..."` 提交修改（提交信息应简明描述本次改动）。
   - `git push` 推送到远程仓库 `origin`。
   - 在回复中告知用户已推送成功。

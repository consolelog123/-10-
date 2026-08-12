# 抖音限制助手 - 使用 GitHub Actions 构建指南

## 步骤 1：上传项目到 GitHub

### 方法 A：使用 Git 命令行

在项目文件夹中执行：

```bash
cd E:\claude\DouyinBlocker

# 初始化 Git 仓库
git init

# 添加所有文件
git add .

# 提交
git commit -m "Initial commit: 抖音限制助手"

# 在 GitHub 上创建一个新仓库，然后关联
git remote add origin https://github.com/你的用户名/DouyinBlocker.git

# 推送到 GitHub
git push -u origin main
```

### 方法 B：使用 GitHub Desktop

1. 下载并安装 GitHub Desktop
2. 点击 "Add" → "Add Existing Repository"
3. 选择 `E:\claude\DouyinBlocker` 文件夹
4. 点击 "Publish repository"

### 方法 C：直接在 GitHub 网页上传

1. 访问 https://github.com/new 创建新仓库
2. 仓库名：DouyinBlocker
3. 点击 "uploading an existing file"
4. 将 `E:\claude\DouyinBlocker` 文件夹中的所有文件拖入上传区

## 步骤 2：自动构建

上传后，GitHub Actions 会自动开始构建：

1. 在你的仓库页面，点击 "Actions" 标签
2. 等待构建完成（大约 3-5 分钟）
3. 构建成功后，点击工作流运行记录
4. 在 "Artifacts" 部分下载 `app-debug.zip`
5. 解压后得到 `app-debug.apk`

## 步骤 3：手动触发构建

如果需要重新构建：

1. 进入 "Actions" 标签
2. 选择 "Build Android APK" 工作流
3. 点击 "Run workflow" 按钮
4. 等待构建完成并下载

## 步骤 4：安装到手机

将下载的 `app-debug.apk` 传输到手机：

- 通过 USB 数据线
- 或上传到云盘后在手机上下载
- 或通过 QQ/微信传给自己

在手机上点击 APK 文件安装即可。

## 注意事项

- 首次上传可能需要等待 GitHub Actions 初始化
- 如果构建失败，检查 Actions 日志查看错误信息
- Debug 版本的 APK 未签名，部分手机可能提示"未知来源"

## 项目文件位置

- 项目文件夹：`E:\claude\DouyinBlocker`
- 压缩包：`E:\claude\DouyinBlocker.tar.gz`（可用于手动上传）

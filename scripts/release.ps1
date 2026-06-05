# auto-version-release/release.ps1
# 版本递增与APK重命名脚本
param(
    [Parameter(Mandatory=$true)]
    [string]$ProjectRoot,

    [switch]$IncrementOnly,
    [switch]$RenameOnly,
    [switch]$BuildAndRelease
)

$ErrorActionPreference = "Stop"

$buildGradlePath = Join-Path $ProjectRoot "app\build.gradle.kts"

if (-not (Test-Path $buildGradlePath)) {
    Write-Error "找不到 app/build.gradle.kts: $buildGradlePath"
    exit 1
}

# ============================================================
# 1. 读取当前版本并递增
# ============================================================
function Increment-Version {
    $content = Get-Content $buildGradlePath -Raw

    # 提取当前 versionCode
    if ($content -match 'versionCode\s*=\s*(\d+)') {
        $oldVersionCode = [int]$Matches[1]
    } else {
        Write-Error "无法解析 versionCode"
        exit 1
    }

    # 提取当前 versionName
    if ($content -match 'versionName\s*=\s*"([^"]+)"') {
        $oldVersionName = $Matches[1]
    } else {
        Write-Error "无法解析 versionName"
        exit 1
    }

    # 解析版本号 (major.minor.patch)
    $parts = $oldVersionName -split '\.'
    if ($parts.Count -ge 3) {
        $major = $parts[0]
        $minor = $parts[1]
        $patch = [int]$parts[2] + 1
        $newVersionName = "$major.$minor.$patch"
    } else {
        $newVersionName = $oldVersionName + ".1"
    }

    $newVersionCode = $oldVersionCode + 1

    # 替换 versionCode
    $content = $content -replace "versionCode\s*=\s*$oldVersionCode", "versionCode = $newVersionCode"
    # 替换 versionName
    $content = $content -replace "versionName\s*=\s*`"$oldVersionName`"", "versionName = `"$newVersionName`""

    Set-Content -Path $buildGradlePath -Value $content -NoNewline

    Write-Host "[版本递增] versionCode: $oldVersionCode -> $newVersionCode"
    Write-Host "[版本递增] versionName: $oldVersionName -> $newVersionName"

    return @{
        OldCode   = $oldVersionCode
        OldName   = $oldVersionName
        NewCode   = $newVersionCode
        NewName   = $newVersionName
    }
}

# ============================================================
# 2. 提取项目名
# ============================================================
function Get-ProjectName {
    $content = Get-Content $buildGradlePath -Raw

    # 尝试从 applicationId 提取
    if ($content -match 'applicationId\s*=\s*"([^"]+)"') {
        $appId = $Matches[1]
        $parts = $appId -split '\.'
        return $parts[-1]
    }

    # 回退: 从 namespace 提取
    if ($content -match 'namespace\s*=\s*"([^"]+)"') {
        $ns = $Matches[1]
        $parts = $ns -split '\.'
        return $parts[-1]
    }

    # 最终回退
    return (Split-Path $ProjectRoot -Leaf)
}

# ============================================================
# 3. 重命名 APK
# ============================================================
function Rename-Apk {
    param(
        [string]$VersionCode,
        [string]$VersionName
    )

    $projectName = Get-ProjectName
    $apkDir = Join-Path $ProjectRoot "app\build\outputs\apk\release"

    if (-not (Test-Path $apkDir)) {
        Write-Error "APK 输出目录不存在: $apkDir"
        exit 1
    }

    # 查找 APK 文件
    $apkFiles = Get-ChildItem -Path $apkDir -Filter "*.apk" | Sort-Object LastWriteTime -Descending

    if ($apkFiles.Count -eq 0) {
        Write-Error "在 $apkDir 中未找到 .apk 文件"
        exit 1
    }

    $apk = $apkFiles[0]
    $newName = "${projectName}-v${VersionCode}-${VersionName}.apk"
    $newPath = Join-Path $apkDir $newName

    # 如果目标文件已存在，先删除
    if (Test-Path $newPath) {
        Remove-Item $newPath -Force
    }

    Rename-Item -Path $apk.FullName -NewName $newName

    Write-Host "[APK重命名] $($apk.Name) -> $newName"
    Write-Host "[输出路径] $newPath"

    return $newPath
}

# ============================================================
# 主流程
# ============================================================
if ($RenameOnly) {
    $content = Get-Content $buildGradlePath -Raw
    if ($content -match 'versionCode\s*=\s*(\d+)') { $vc = $Matches[1] } else { $vc = "0" }
    if ($content -match 'versionName\s*=\s*"([^"]+)"') { $vn = $Matches[1] } else { $vn = "0.0.0" }
    $outputPath = Rename-Apk -VersionCode $vc -VersionName $vn
    Write-Host ""
    Write-Host "=== 构建完成 ==="
    Write-Host "[最终产物] $outputPath"
} elseif ($IncrementOnly) {
    $null = Increment-Version
    Write-Host ""
    Write-Host "[完成] 版本号已递增，未构建。"
} elseif ($BuildAndRelease) {
    # 全自动模式: 递增 -> 构建 -> 重命名
    $result = Increment-Version
    Write-Host ""
    Write-Host "=== 开始构建 Release APK... ==="
    $gradlew = Join-Path $ProjectRoot "gradlew.bat"
    & $gradlew assembleRelease -p $ProjectRoot
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Gradle 构建失败 (exit code: $LASTEXITCODE)"
        exit $LASTEXITCODE
    }
    Write-Host ""
    Write-Host "=== 构建成功，开始重命名... ==="
    $outputPath = Rename-Apk -VersionCode $result.NewCode -VersionName $result.NewName
    Write-Host ""
    Write-Host "==========================================="
    Write-Host "  Release 构建完成!"
    Write-Host "  产物: $outputPath"
    Write-Host "  版本: v$($result.NewCode) ($($result.NewName))"
    Write-Host "==========================================="
} else {
    # 默认: 先递增版本
    $result = Increment-Version
    Write-Host ""
    Write-Host "=== 版本已递增，请执行构建命令: ==="
    Write-Host "  .\gradlew.bat assembleRelease"
    Write-Host ""
    Write-Host "=== 构建完成后，运行以下命令重命名APK: ==="
    Write-Host "  powershell -File .\release.ps1 -ProjectRoot `"$ProjectRoot`" -RenameOnly"
    Write-Host ""
    Write-Host "[预期输出名称] $(Get-ProjectName)-v$($result.NewCode)-$($result.NewName).apk"
}

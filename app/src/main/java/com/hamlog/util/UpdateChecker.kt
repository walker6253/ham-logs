package com.hamlog.util

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val hasUpdate: Boolean,
    val latestVersion: String = "",
    val currentVersion: String = "",
    val releaseUrl: String = "",
    val body: String = ""
)

object UpdateChecker {
    private const val GITHUB_API = "https://api.github.com/repos/walker6253/ham-logs/releases/latest"
    private const val GITHUB_RELEASES = "https://github.com/walker6253/ham-logs/releases/latest"

    fun getCurrentVersion(context: Context): String {
        return try {
            val pkgInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pkgInfo.versionName ?: "0.0.0"
        } catch (_: PackageManager.NameNotFoundException) {
            "0.0.0"
        }
    }

    suspend fun checkForUpdate(context: Context): UpdateInfo = withContext(Dispatchers.IO) {
        try {
            val currentVersion = getCurrentVersion(context)
            val url = URL(GITHUB_API)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val json = connection.inputStream.bufferedReader().use { it.readText() }
                val release = JSONObject(json)
                val tagName = release.getString("tag_name").trimStart('v')
                val htmlUrl = release.optString("html_url", GITHUB_RELEASES)
                val body = release.optString("body", "")

                val hasUpdate = compareVersions(tagName, currentVersion) > 0
                UpdateInfo(
                    hasUpdate = hasUpdate,
                    latestVersion = tagName,
                    currentVersion = currentVersion,
                    releaseUrl = htmlUrl,
                    body = body
                )
            } else {
                UpdateInfo(hasUpdate = false, currentVersion = currentVersion)
            }
        } catch (e: Exception) {
            UpdateInfo(hasUpdate = false, currentVersion = getCurrentVersion(context))
        }
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLen = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLen) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 != p2) return p1.compareTo(p2)
        }
        return 0
    }
}

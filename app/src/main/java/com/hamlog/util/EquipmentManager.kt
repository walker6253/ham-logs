package com.hamlog.util

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

object EquipmentManager {
    private var prefs: SharedPreferences? = null

    data class EquipmentCategory(
        val brand: String,
        val models: List<String>
    )

    private val defaultAntennas = listOf(
        "GP\u5929\u7ebf", "\u516b\u6728", "\u5012V", "\u6b63V",
        "\u957f\u7ebf", "DP", "\u7aef\u9988", "\u5929\u8c03", "\u78c1\u73af"
    )

    private val defaultRigs = listOf(
        EquipmentCategory("ICOM", listOf("IC-7300", "IC-705", "IC-9700", "IC-7610", "IC-9100")),
        EquipmentCategory("\u516b\u91cd\u6d32", listOf("FT-891", "FT-710", "FT-818", "FT-991", "FTdx10", "FT-857", "FT-817")),
        EquipmentCategory("\u534f\u8c37", listOf("G90", "X6100", "X5105", "X108G")),
        EquipmentCategory("\u5176\u4ed6", listOf("KX3", "DX-10", "QRP Labs", "uSDX"))
    )

    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.getSharedPreferences("hamlog_equipment", Context.MODE_PRIVATE)
    }

    fun getAntennas(): List<String> {
        val p = prefs ?: return defaultAntennas
        val json = p.getString("antennas", null) ?: return defaultAntennas
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (_: Exception) { defaultAntennas }
    }

    fun setAntennas(list: List<String>) {
        prefs?.edit()?.putString("antennas", JSONArray(list).toString())?.apply()
    }

    fun addAntenna(item: String) {
        val list = getAntennas().toMutableList()
        if (item.isNotBlank() && item !in list) {
            list.add(item.trim())
            setAntennas(list)
        }
    }

    fun removeAntenna(item: String) {
        val list = getAntennas().toMutableList()
        list.remove(item)
        setAntennas(list)
    }

    fun getRigs(): List<EquipmentCategory> {
        val p = prefs ?: return defaultRigs
        val json = p.getString("rigs", null) ?: return defaultRigs
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                val brand = obj.getString("brand")
                val models = (0 until obj.getJSONArray("models").length())
                    .map { obj.getJSONArray("models").getString(it) }
                EquipmentCategory(brand, models)
            }
        } catch (_: Exception) { defaultRigs }
    }

    fun setRigs(list: List<EquipmentCategory>) {
        val arr = JSONArray()
        list.forEach { cat ->
            val obj = JSONObject()
            obj.put("brand", cat.brand)
            val modelsArr = JSONArray()
            cat.models.forEach { modelsArr.put(it) }
            obj.put("models", modelsArr)
            arr.put(obj)
        }
        prefs?.edit()?.putString("rigs", arr.toString())?.apply()
    }

    fun addRigModel(brand: String, model: String) {
        val list = getRigs().toMutableList()
        val idx = list.indexOfFirst { it.brand == brand }
        if (idx >= 0) {
            val cat = list[idx]
            val newModels = cat.models.toMutableList()
            if (model.isNotBlank() && model.trim() !in newModels) {
                newModels.add(model.trim())
                list[idx] = cat.copy(models = newModels)
                setRigs(list)
            }
        }
    }

    fun removeRigModel(brand: String, model: String) {
        val list = getRigs().toMutableList()
        val idx = list.indexOfFirst { it.brand == brand }
        if (idx >= 0) {
            val cat = list[idx]
            val newModels = cat.models.toMutableList()
            newModels.remove(model)
            list[idx] = cat.copy(models = newModels)
            setRigs(list)
        }
    }

    fun addRigBrand(brand: String) {
        if (brand.isBlank()) return
        val list = getRigs().toMutableList()
        if (list.none { it.brand == brand.trim() }) {
            list.add(EquipmentCategory(brand.trim(), emptyList()))
            setRigs(list)
        }
    }

    fun removeRigBrand(brand: String) {
        val list = getRigs().toMutableList()
        list.removeAll { it.brand == brand }
        setRigs(list)
    }
}

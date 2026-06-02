package com.hamlog.util

object CallSignUtils {
    private val districtMap = mapOf(
        '1' to "\u5317\u4eac",
        '2' to "\u9ed1\u9f99\u6c5f/\u5409\u6797/\u8fbd\u5b81",
        '3' to "\u5929\u6d25/\u5185\u8499\u53e4/\u6cb3\u5317/\u5c71\u897f",
        '4' to "\u4e0a\u6d77/\u5c71\u4e1c/\u6c5f\u82cf",
        '5' to "\u6d59\u6c5f/\u6c5f\u897f/\u798f\u5efa",
        '6' to "\u5b89\u5fbd/\u6cb3\u5357/\u6e56\u5317",
        '7' to "\u6e56\u5357/\u5e7f\u4e1c/\u5e7f\u897f/\u6d77\u5357",
        '8' to "\u56db\u5ddd/\u91cd\u5e86/\u8d35\u5dde/\u4e91\u5357",
        '9' to "\u9655\u897f/\u7518\u8083/\u5b81\u590f/\u9752\u6d77",
        '0' to "\u65b0\u7586/\u897f\u85cf"
    )

    fun getProvince(callsign: String): String? {
        if (callsign.length < 3) return null
        val c = callsign[0].uppercase()
        if (c != "B") return null
        val digit = callsign[2]
        return districtMap[digit]
    }
}

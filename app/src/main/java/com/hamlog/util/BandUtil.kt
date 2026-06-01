package com.hamlog.util

object BandUtil {

    data class Band(val name: String, val minMHz: Double, val maxMHz: Double)

    private val bands = listOf(
        Band("2200m", 0.135, 0.138),
        Band("630m", 0.472, 0.479),
        Band("160m", 1.8, 2.0),
        Band("80m", 3.5, 4.0),
        Band("60m", 5.25, 5.45),
        Band("40m", 7.0, 7.3),
        Band("30m", 10.1, 10.15),
        Band("20m", 14.0, 14.35),
        Band("17m", 18.068, 18.168),
        Band("15m", 21.0, 21.45),
        Band("12m", 24.89, 24.99),
        Band("10m", 28.0, 29.7),
        Band("6m", 50.0, 54.0),
        Band("4m", 70.0, 70.5),
        Band("2m", 144.0, 148.0),
        Band("1.25m", 222.0, 225.0),
        Band("70cm", 430.0, 440.0),
        Band("33cm", 902.0, 928.0),
        Band("23cm", 1240.0, 1300.0),
        Band("13cm", 2300.0, 2450.0)
    )

    /** Auto-suggest mode based on frequency: <10MHz → LSB, 10-29.7MHz → USB, 50-54MHz → USB, VHF+ → FM */
    fun autoMode(frequencyMHz: Double): String {
        if (frequencyMHz <= 0) return ""
        return when {
            frequencyMHz < 10.0 -> "LSB"
            frequencyMHz <= 29.7 -> "USB"
            frequencyMHz <= 54.0 -> "USB"
            frequencyMHz <= 148.0 -> "FM"
            frequencyMHz <= 225.0 -> "FM"
            frequencyMHz <= 440.0 -> "FM"
            frequencyMHz <= 1300.0 -> "FM"
            else -> "FM"
        }
    }

    fun getBand(frequencyMHz: Double): String {
        if (frequencyMHz <= 0) return ""
        for (band in bands) {
            if (frequencyMHz >= band.minMHz && frequencyMHz <= band.maxMHz) {
                return band.name
            }
        }
        // Approximate: wavelength = 300 / f
        val meters = (300.0 / frequencyMHz).toInt()
        return when {
            meters >= 100 -> ""
            else -> "${meters}m"
        }
    }
}

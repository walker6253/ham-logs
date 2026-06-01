package com.hamlog.util

data class ParsedFields(
    val callsign: String = "",
    val frequencyMHz: String = "",
    val mode: String = "",
    val rstSent: String = "",
    val rstReceived: String = "",
    val powerTx: String = "",
    val powerRx: String = "",
    val notes: String = ""
)

object SmartInputParser {

    private val callsignRegex = Regex("""^[A-Za-z]{1,2}[0-9][A-Za-z]{1,4}""")
    private val frequencyRegex = Regex("""^\d{1,7}$""")
    private val frequencyWithDotRegex = Regex("""^\d{1,3}\.\d{1,6}$""")
    private val modeKeywords = setOf(
        "SSB", "USB", "LSB", "CW", "FM", "AM", "RTTY", "FT8", "FT4",
        "PSK31", "PSK63", "JT65", "JT9", "MSK144", "FSK441", "ISCAT",
        "Q65", "FST4", "FST4W", "FREEDV", "SSTV", "MFSK", "OLIVIA", "CONTESTIA",
        "JS8", "VARAC", "VARA", "ARDOP", "PKT", "TOR"
    )
    private val rstRegex = Regex("""^[1-5][1-9]{1,2}""")
    private val powerRegex = Regex("""^(\d+)\s*(W|KW|w|kw|mW|mw)""")

    fun parse(input: String): ParsedFields {
        var callsign = ""
        var frequencyMHz = ""
        var mode = ""
        var rstSent = ""
        var rstReceived = ""
        var powerTx = ""
        var powerRx = ""
        val notesParts = mutableListOf<String>()

        var rstCount = 0
        var powerCount = 0

        val tokens = input.trim().split(Regex("\\s+"))
        val processedTokens = mutableSetOf<Int>()

        // First pass: detect mode, frequency, power, RST, callsign by pattern
        for ((index, token) in tokens.withIndex()) {
            if (token.isBlank()) continue

            // Check mode keywords
            if (mode.isEmpty() && token.uppercase() in modeKeywords) {
                mode = token.uppercase()
                processedTokens.add(index)
                continue
            }

            // Check RST
            if (rstRegex.matches(token) && rstCount < 2) {
                if (rstCount == 0) {
                    rstSent = token
                } else {
                    rstReceived = token
                }
                rstCount++
                processedTokens.add(index)
                continue
            }

            // Check power
            val powerMatch = powerRegex.find(token.uppercase())
            if (powerMatch != null && powerCount < 2) {
                if (powerCount == 0) {
                    powerTx = token.uppercase()
                } else {
                    powerRx = token.uppercase()
                }
                powerCount++
                processedTokens.add(index)
                continue
            }
        }

        // Second pass: detect callsign and frequency from remaining tokens
        for ((index, token) in tokens.withIndex()) {
            if (index in processedTokens) continue
            if (token.isBlank()) continue

            // Check callsign pattern
            if (callsign.isEmpty() && callsignRegex.matches(token.uppercase())) {
                callsign = token.uppercase()
                continue
            }

            // Check frequency (pure digits -> auto format)
            if (frequencyMHz.isEmpty() && frequencyRegex.matches(token)) {
                frequencyMHz = formatFrequency(token)
                continue
            }

            // Check frequency with dot
            if (frequencyMHz.isEmpty() && frequencyWithDotRegex.matches(token)) {
                frequencyMHz = token
                continue
            }
        }

        // Third pass: collect remaining tokens as notes
        for ((index, token) in tokens.withIndex()) {
            if (index in processedTokens) continue
            if (token.isBlank()) continue
            // Skip if already used as callsign or frequency
            if (token.uppercase() == callsign || token == frequencyMHz) continue
            notesParts.add(token)
        }

        val notes = notesParts.joinToString(" ")

        return ParsedFields(
            callsign = callsign,
            frequencyMHz = frequencyMHz,
            mode = mode,
            rstSent = rstSent,
            rstReceived = rstReceived,
            powerTx = powerTx,
            powerRx = powerRx,
            notes = notes
        )
    }

    private fun formatFrequency(digits: String): String {
        return when {
            digits.length <= 3 -> digits
            digits.length == 4 -> "."
            digits.length == 5 -> "."
            digits.length == 6 -> "."
            digits.length == 7 -> "."
            else -> digits
        }
    }
}

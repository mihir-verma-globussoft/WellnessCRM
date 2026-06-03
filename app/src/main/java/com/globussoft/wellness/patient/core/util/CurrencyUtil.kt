package com.globussoft.wellness.patient.core.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtil {

    private val indianLocale = Locale("en", "IN")

    fun formatPaise(paise: Long, currency: String = "INR"): String {
        val amount = paise / 100.0
        return if (currency == "INR") {
            val formatter = NumberFormat.getCurrencyInstance(indianLocale)
            formatter.format(amount)
        } else {
            "%.2f %s".format(amount, currency)
        }
    }

    fun formatRupees(rupees: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(indianLocale)
        return formatter.format(rupees)
    }

    fun formatRupees(rupees: Long): String = formatRupees(rupees.toDouble())

    fun paiseTo(paise: Long): Double = paise / 100.0
}

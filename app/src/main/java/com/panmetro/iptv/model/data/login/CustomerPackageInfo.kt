package com.panmetro.iptv.model.data.login

import android.annotation.SuppressLint
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class CustomerPackageInfo(
    val count: Int,
    val page: Int,
    val results: List<Result>?,
    @SerializedName("results-per-page")
    val resultsPerPage: Int
)


data class Result(
    @SerializedName("customer-id")
    val customerId: Int,
    @SerializedName("customer-number")
    val customerNumber: String,
    @SerializedName("end-date")
    val endDate: String,
    @SerializedName("expire-date")
    val expireDate: String,
    val id: Int,
    @SerializedName("org-start-date")
    val orgStartDate: String,
    @SerializedName("service-id")
    val serviceId: Int,
    @SerializedName("service-name")
    val serviceName: String
){
    @SuppressLint("SimpleDateFormat")
    fun isExpired(): Boolean {
        return try {
            // Parse the provided date string
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone("UTC") // Important for consistency

            val expirationDate = formatter.parse(expireDate)
            val currentDate = Date() // Current date

            // Check if current date is after the expiration date
            currentDate.after(expirationDate)
        } catch (e: Exception) {
            // Handle parsing errors (invalid date format)
            // You might want to log this error or handle it differently
            true // Or false, depending on your business logic for invalid dates
        }
    }
}
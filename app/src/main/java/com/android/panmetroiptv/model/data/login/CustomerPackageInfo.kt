package com.android.panmetroiptv.model.data.login

import com.google.gson.annotations.SerializedName

data class CustomerPackageInfo(
    val count: Int,
    val page: Int,
    val results: List<Result>,
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
)
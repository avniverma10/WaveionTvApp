package com.caastv.tvapp.utils.crash

data class LogEntry(var timestamp: String?="",var logLevel: String?="",var tag: String?="",var message: String?="",var deviceInfo: String?="",var appVersion: String?="")
data class CrashReport(var timestamp: String?="",var stackTrace: String?="",var deviceInfo: String?="",var appVersion: String?="",var userActions: String?="")


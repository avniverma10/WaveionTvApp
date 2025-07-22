package com.caastv.tvapp.model.wtvdatabase

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class EPGDbHelper(context: Context) : SQLiteOpenHelper(context, "wtv_epg.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE ${EPGContract.EPGEntry.TABLE_NAME} (
                ${EPGContract.EPGEntry.COLUMN_ID} TEXT,
                ${EPGContract.EPGEntry.COLUMN_CHANNEL_ID} TEXT PRIMARY KEY,
                ${EPGContract.EPGEntry.COLUMN_DATA} TEXT,
                ${EPGContract.EPGEntry.COLUMN_CHANNEL_HASH} TEXT, 
                ${EPGContract.EPGEntry.COLUMN_LAST_UPDATED} TEXT
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${EPGContract.EPGEntry.TABLE_NAME}")
        onCreate(db)
    }
}

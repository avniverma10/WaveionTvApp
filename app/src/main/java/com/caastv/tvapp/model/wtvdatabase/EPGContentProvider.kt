package com.caastv.tvapp.model.wtvdatabase

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri

class EPGContentProvider : ContentProvider() {
    private lateinit var dbHelper: EPGDbHelper

    override fun onCreate(): Boolean {
        context?.let { dbHelper = EPGDbHelper(it) }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        val cursor = dbHelper.readableDatabase.query(
            EPGContract.EPGEntry.TABLE_NAME,
            projection, selection, selectionArgs, null, null, sortOrder
        )
        cursor.setNotificationUri(context!!.contentResolver, uri)
        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val id = dbHelper.writableDatabase.insertWithOnConflict(
            EPGContract.EPGEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE
        )
        context?.contentResolver?.notifyChange(uri, null)
        return ContentUris.withAppendedId(uri, id)
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?) = 0
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?) = 0
    override fun getType(uri: Uri): String? = null
}

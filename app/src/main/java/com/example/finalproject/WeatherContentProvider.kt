package com.example.finalproject

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

class WeatherContentProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        // 初始化資料庫等操作
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        // 處理查詢操作
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        // 處理插入操作
        return null
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        // 處理更新操作
        return 0
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        // 處理刪除操作
        return 0
    }

    override fun getType(uri: Uri): String? {
        // 返回此 URI 的 MIME 類型
        return null
    }
}

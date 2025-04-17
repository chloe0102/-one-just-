package com.example.finalproject

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*

class FocusManager(private val userId: String, private val db: FirebaseFirestore) {

    private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    // 初始化每日專注文檔
    fun initializeDailyFocusDocument() {
        val todayDate = dateFormat.format(Date())
        val dailyFocusDoc = db.collection("users").document(userId)
            .collection("concentrateTime").document(todayDate)

        dailyFocusDoc.get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    // 初始化每日文檔
                    val initialData = mapOf(
                        "earliestStartTime" to Timestamp.now(),
                        "latestEndTime" to null,
                        "longestFocus" to 0L,
                        "totalFocusTime" to 0L
                    )
                    dailyFocusDoc.set(initialData)
                        .addOnSuccessListener {
                            Log.d("FocusManager", "每日專注文檔初始化成功")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FocusManager", "每日專注文檔初始化失敗：${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FocusManager", "檢查每日文檔失敗：${e.message}")
            }

        dailyFocusDoc.update(
            mapOf(
                "lastFocusTime" to FieldValue.delete(),
                "longestFocusDuration" to FieldValue.delete()
            )
        ).addOnSuccessListener {
            Log.d("Firestore", "冗餘字段清理成功")
        }.addOnFailureListener { e ->
            Log.e("Firestore", "冗餘字段清理失敗：${e.message}")
        }

    }

    fun startFocus(): Timestamp {
        return Timestamp.now()
    }

    //endFocus後建於本日文件專注集合中分次建立每次專注相關欄位
    fun endFocus(startTime: Timestamp) {
        val endTime = Timestamp.now()
        val duration = (endTime.seconds - startTime.seconds).toInt()

        val todayDate = dateFormat.format(Date())
        val dailyFocusDoc = db.collection("users").document(userId)
            .collection("concentrateTime").document(todayDate)

        val focusRecord = hashMapOf(
            "startTime" to startTime,
            "endTime" to endTime,
            "duration" to duration
        )

        dailyFocusDoc.collection("focusRecords").add(focusRecord)
            .addOnSuccessListener {
                Log.d("FocusManager", "專注記錄成功新增")
                updateDailySummary(dailyFocusDoc, startTime, endTime, duration)
            }
            .addOnFailureListener { e ->
                Log.e("FocusManager", "專注記錄失敗：${e.message}")
            }
    }

    private fun updateDailySummary(
        dailyFocusDoc: DocumentReference,
        startTime: Timestamp,
        endTime: Timestamp,
        duration: Int
    ) {
        dailyFocusDoc.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // 更新已存在的每日文件
                    val earliestStartTime = document.getTimestamp("earliestStartTime")
                    val latestEndTime = document.getTimestamp("latestEndTime")
                    val longestFocus = document.getLong("longestFocus") ?: 0L
                    val totalFocusTime = document.getLong("totalFocusTime") ?: 0L

                    val updatedData = mapOf(
                        "earliestStartTime" to (earliestStartTime?.let { if (startTime < it) startTime else it } ?: startTime),
                        "latestEndTime" to (latestEndTime?.let { if (endTime > it) endTime else it } ?: endTime),
                        "longestFocus" to maxOf(longestFocus, duration.toLong()),
                        "totalFocusTime" to (totalFocusTime + duration)
                    )


                    dailyFocusDoc.set(updatedData, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d("Firestore", "更新每日專注數據成功")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "更新每日專注數據失敗：${e.message}")
                        }
                } else {
                    // 初始化每日文件
                    val initialData = mapOf(
                        "earliestStartTime" to startTime,
                        "latestEndTime" to endTime,
                        "longestFocus" to duration.toLong(),
                        "totalFocusTime" to duration.toLong()
                    )

                    dailyFocusDoc.set(initialData)
                        .addOnSuccessListener {
                            Log.d("Firestore", "初始化每日專注數據成功")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "初始化每日專注數據失敗：${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "獲取每日專注數據失敗：${e.message}")
            }
    }

    fun storeFocusResult(startTime: Timestamp, endTime: Timestamp, duration: Int) {
        val dateKey = dateFormat.format(Date()) // 當前日期
        val dailyFocusRef = db.collection("users")
            .document(userId)
            .collection("concentrateTime")
            .document(dateKey)

        dailyFocusRef.get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    // 初始化每日文檔
                    val initialData = mapOf(
                        "earliestStartTime" to startTime,
                        "latestEndTime" to endTime,
                        "longestFocus" to duration,
                        "totalFocusTime" to duration
                    )
                    dailyFocusRef.set(initialData)
                } else {
                    // 更新已有的每日文檔
                    val data = document.data
                    val currentLongestFocus = (data?.get("longestFocus") as? Long)?.toInt() ?: 0
                    val newLongestFocus = maxOf(currentLongestFocus, duration)
                    val totalFocusTime = (data?.get("totalFocusTime") as? Long)?.toInt() ?: 0

                    dailyFocusRef.update(
                        mapOf(
                            "latestEndTime" to endTime,
                            "longestFocus" to newLongestFocus,
                            "totalFocusTime" to totalFocusTime + duration
                        )
                    )
                }

                // 儲存當前專注次數
                dailyFocusRef.collection("focusSessions").add(
                    mapOf(
                        "startTime" to startTime,
                        "endTime" to endTime,
                        "duration" to duration
                    )
                )
            }
            .addOnFailureListener { e ->
                Log.e("FocusManager", "儲存計時結果失敗: ${e.message}")
            }
    }
}
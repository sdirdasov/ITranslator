package ru.sdirdasov.itranslator

import android.content.ContentValues
import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*

class AppDatabase(context: Context?, name: String?) : SQLiteOpenHelper(context, name, null, 1)
{
    val allWords: ArrayList<Dictionary>
        get() {
            val arrayList = ArrayList<Dictionary>()
            var db = this.writableDatabase
            db.delete("words", "isDeleted = ?", arrayOf(1.toString()))

            db = this.readableDatabase
            val cursor = db.rawQuery(
                "SELECT word, translation, sourcePosition, targetPosition FROM words",
                null
            )

            cursor.moveToFirst()

            while (!cursor.isAfterLast) {
                val item = Dictionary(
                    cursor.getString(cursor.getColumnIndex("word")),
                    cursor.getString(cursor.getColumnIndex("translation")),
                    cursor.getInt(cursor.getColumnIndex("sourcePosition")),
                    cursor.getInt(cursor.getColumnIndex("targetPosition"))
                )
                if (!item.isEmpty) {
                    arrayList.add(item)
                }
                cursor.moveToNext()
            }

            return arrayList
        }

    override fun onCreate(db: SQLiteDatabase)
    {
        db.execSQL(
            "CREATE TABLE words (word TEXT, translation TEXT, isDeleted INTEGER, " +
                    "sourcePosition INTEGER, targetPosition INTEGER, sourceLanguage TEXT, " +
                    "targetLanguage TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int)
    {
        db.execSQL("DROP TABLE IF EXISTS words")
        onCreate(db)
    }

    fun insertWord(item: Dictionary)
    {
        if (!isFindDB(item)) {
            val db = this.writableDatabase
            val contentValues = ContentValues()
            contentValues.put("word", item.word)
            contentValues.put("translation", item.translation)
            contentValues.put("isDeleted", 0)
            contentValues.put("sourcePosition", item.sourcePosition)
            contentValues.put("targetPosition", item.targetPosition)
            contentValues.put("sourceLanguage", item.sourceLanguage)
            contentValues.put("targetLanguage", item.targetLanguage)
            db.insert("words", null, contentValues)
        }
    }

    private fun isFindDB(word: Dictionary): Boolean
    {
        val count = DatabaseUtils.queryNumEntries(
            this.readableDatabase, "words",
            "word = ? AND sourcePosition = ? AND targetPosition = ? AND isDeleted = ?",
            arrayOf(
                word.word,
                word.sourcePosition.toString(),
                word.targetPosition.toString(),
                "0"
            )
        )

        return count != 0L
    }


}

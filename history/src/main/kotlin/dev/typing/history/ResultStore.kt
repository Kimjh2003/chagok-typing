package dev.typing.history

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

data class SavedResult(
    val id: String, val completedAt: Long, val language: String, val mode: String,
    val prompt: String, val input: String, val units: Int, val elapsedMillis: Long,
    val accuracy: Int, val perMinute: Int,
)

data class HistorySummary(val id: String, val completedAt: Long, val mode: String,
    val perMinute: Int, val accuracy: Int)

data class RecentAverages(val count: Int, val perMinute: Int, val accuracy: Int)

/** Input is already ordered newest first and filtered by language and mode. */
fun recentAverages(records: List<HistorySummary>): RecentAverages? {
    val recent = records.take(7)
    if (recent.isEmpty()) return null
    return RecentAverages(recent.size,
        kotlin.math.round(recent.sumOf { it.perMinute.toDouble() } / recent.size).toInt(),
        kotlin.math.round(recent.sumOf { it.accuracy.toDouble() } / recent.size).toInt())
}

private fun Cursor.toSavedResult() = SavedResult(
    getString(0), getLong(1), getString(2), getString(3),
    getString(4), getString(5), getInt(6), getLong(7),
    getInt(8), getInt(9)
)

private fun Cursor.toHistorySummary() = HistorySummary(
    getString(0), getLong(1), getString(2), getInt(3), getInt(4)
)

/** App-private local history. All database work is serialized off the input/UI thread. */
class ResultStore(context: Context, databaseName: String = "results.db") : java.lang.AutoCloseable {
    private val worker = Executors.newSingleThreadExecutor()
    private val gate = Any()
    @Volatile private var closed = false
    private val main = Handler(Looper.getMainLooper())
    private val helper = object : SQLiteOpenHelper(context.applicationContext, databaseName, null, 2) {
        override fun onConfigure(db: SQLiteDatabase) {
            super.onConfigure(db)
            db.enableWriteAheadLogging()
        }
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL("""CREATE TABLE results (
                id TEXT PRIMARY KEY, completed_at INTEGER NOT NULL, language TEXT NOT NULL,
                mode TEXT NOT NULL, prompt TEXT NOT NULL, input TEXT NOT NULL,
                units INTEGER NOT NULL, elapsed_ms INTEGER NOT NULL,
                accuracy INTEGER NOT NULL, rate INTEGER NOT NULL)""")
            db.execSQL("CREATE INDEX results_date ON results(completed_at DESC)")
            db.execSQL("CREATE INDEX results_lang_mode_date ON results(language, mode, completed_at DESC)")
        }
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            if (oldVersion < 2) {
                db.execSQL("CREATE INDEX IF NOT EXISTS results_lang_mode_date ON results(language, mode, completed_at DESC)")
            }
        }
    }

    override fun close() {
        synchronized(gate) {
            if (closed) return
            closed = true
            // Drain accepted work before closing SQLite, on its owning worker.
            worker.execute { helper.close() }
            worker.shutdown()
        }
    }

    fun save(record: SavedResult,
             callback: (Result<Unit>) -> Unit) = execute(callback) {
        val values = ContentValues().apply {
            put("id", record.id); put("completed_at", record.completedAt); put("language", record.language); put("mode", record.mode)
            put("prompt", record.prompt); put("input", record.input); put("units", record.units)
            put("elapsed_ms", record.elapsedMillis); put("accuracy", record.accuracy); put("rate", record.perMinute)
        }
        val db = helper.writableDatabase
        if (db.insertWithOnConflict("results", null, values, SQLiteDatabase.CONFLICT_IGNORE) == -1L) {
            db.query("results", arrayOf("id"), "id = ?", arrayOf(record.id), null, null, null).use {
                check(it.moveToFirst()) { "Result was not saved" }
            }
        }
    }

    fun list(callback: (Result<List<SavedResult>>) -> Unit) = execute(callback) {
        helper.readableDatabase.query("results", null, null, null, null, null,
            "completed_at DESC, rowid DESC", "100").use { cursor ->
            buildList { while (cursor.moveToNext()) add(cursor.toSavedResult()) }
        }
    }

    fun summaries(language: String, callback: (Result<List<HistorySummary>>) -> Unit) = summaries(language, null, callback)

    fun summaries(language: String, mode: String?, callback: (Result<List<HistorySummary>>) -> Unit) = execute(callback) {
        helper.readableDatabase.query("results", arrayOf("id", "completed_at", "mode", "rate", "accuracy"),
            if (mode == null) "language = ?" else "language = ? AND mode = ?",
            if (mode == null) arrayOf(language) else arrayOf(language, mode), null, null, "completed_at DESC, rowid DESC", "100").use { cursor ->
            buildList { while (cursor.moveToNext()) add(cursor.toHistorySummary()) }
        }
    }

    fun delete(id: String, callback: (Result<Boolean>) -> Unit) = execute(callback) {
        helper.writableDatabase.delete("results", "id = ?", arrayOf(id)) > 0
    }

    /** Deletes only the supplied IDs, atomically, on the database worker. */
    fun deleteMany(ids: Collection<String>, callback: (Result<Int>) -> Unit) {
        val snapshot = ids.toSet()
        execute(callback) {
            if (snapshot.isEmpty()) 0 else {
                val db = helper.writableDatabase
                db.beginTransaction()
                try {
                    var deleted = 0
                    db.compileStatement("DELETE FROM results WHERE id = ?").use { statement ->
                        for (id in snapshot) {
                            statement.bindString(1, id)
                            deleted += statement.executeUpdateDelete()
                        }
                    }
                    db.setTransactionSuccessful()
                    deleted
                } finally {
                    db.endTransaction()
                }
            }
        }
    }

    fun find(id: String, callback: (Result<SavedResult?>) -> Unit) = execute(callback) {
        helper.readableDatabase.query("results", null, "id = ?", arrayOf(id), null, null, null).use { c ->
            if (!c.moveToFirst()) null else c.toSavedResult()
        }
    }

    private fun <T> execute(callback: (Result<T>) -> Unit, action: () -> T) {
        synchronized(gate) {
            if (closed) {
                main.post { callback(Result.failure(IllegalStateException("ResultStore is closed"))) }
                return
            }
            worker.execute {
                val result = runCatching(action)
                main.post { callback(result) }
            }
        }
    }

    companion object {
        @Volatile private var instance: ResultStore? = null
        fun get(context: Context): ResultStore = instance?.takeUnless { it.closed } ?: synchronized(this) {
            instance?.takeUnless { it.closed } ?: ResultStore(context).also { instance = it }
        }
    }
}



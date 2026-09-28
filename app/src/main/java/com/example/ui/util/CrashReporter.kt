package com.example.ui.util

import android.content.Context
import android.util.Log
import java.io.File

/**
 * Saves the stack trace of an uncaught crash to a private file so the next
 * launch can show it on screen (no logcat / PC needed). No permissions used.
 */
object CrashReporter {
    private const val FILE_NAME = "last_crash.txt"

    fun install(context: Context) {
        val app = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                File(app.filesDir, FILE_NAME).writeText(
                    "Thread: ${thread.name}\n" + Log.getStackTraceString(throwable)
                )
            } catch (_: Throwable) {
            }
            previous?.uncaughtException(thread, throwable)
        }
    }

    /** Returns the saved crash text (and deletes it), or null if there was none. */
    fun consume(context: Context): String? {
        return try {
            val f = File(context.applicationContext.filesDir, FILE_NAME)
            if (f.exists()) {
                val text = f.readText()
                f.delete()
                text
            } else null
        } catch (_: Throwable) {
            null
        }
    }
}

package de.ba.railroadclient

import android.util.Log

/**
 * @return result object, Result.success stores the result
 */
fun <T> execute(action: () -> T): Result<T> {
    return try {
        Result.success(action())
    } catch (t: Throwable) {
        Log.d("error", "executing action", t)
        Result.failure(t)
    }
}

fun <T> execute(action: () -> T, className: String, message: String): Result<T> {
    return try {
        Result.success(action())
    } catch (t: Throwable) {
        Log.d(className, message, t)
        Result.failure(t)
    }
}

package com.example.proyectoZapateria.utils

import android.util.Log
import com.google.gson.JsonParseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response


object NetworkUtils {
    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Result<T> {
        return try {
            val response = withContext(Dispatchers.IO) { apiCall() }
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) return Result.success(body)
                return Result.failure(Exception("Respuesta vacía del servidor"))
            } else {
                val code = response.code()
                val errBody = try { response.errorBody()?.string() } catch (e: Exception) { null }
                if (!errBody.isNullOrBlank() && errBody.trimStart().startsWith("<")) {
                    Log.w("NetworkUtils", "Respuesta HTML en errorBody: HTTP $code")
                    return Result.failure(Exception("Respuesta del servidor no es JSON válido (HTTP $code)"))
                }
                return Result.failure(Exception("Error HTTP $code: ${response.message()}"))
            }
        } catch (e: java.net.UnknownHostException) {
            Result.failure(Exception("Sin conexión: ${e.message}"))
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(Exception("Timeout de conexión"))
        } catch (e: java.io.IOException) {
            Result.failure(Exception("Error de red: ${e.message}"))
        } catch (e: JsonParseException) {
            Result.failure(Exception("Respuesta JSON malformada"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


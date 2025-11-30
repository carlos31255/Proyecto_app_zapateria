package com.example.proyectoZapateria.utils

import android.util.Log
import com.google.gson.JsonParseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response


object NetworkUtils {
    // metodo para realizar llamadas a API con manejo de excepciones
    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Result<T> {
        return try {
            val response = withContext(Dispatchers.IO) { apiCall() }
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) return Result.success(body)
                return Result.failure(Exception("Respuesta vacía del servidor"))
            } else {
                val code = response.code()
                val errBody = try { response.errorBody()?.string() } catch (_: Exception) { null }
                if (!errBody.isNullOrBlank() && errBody.trimStart().startsWith("<")) {
                    Log.w("NetworkUtils", "Respuesta HTML en errorBody: HTTP $code")
                    return Result.failure(Exception("Respuesta del servidor no es JSON válido (HTTP $code)"))
                }
                // Incluir el cuerpo del error en la excepción para facilitar debugging en cliente
                val errSnippet = errBody?.let { if (it.length > 1000) it.substring(0, 1000) + "..." else it }
                return Result.failure(Exception("Error HTTP $code: ${response.message()}${errSnippet?.let { " - body=$it" } ?: ""}"))
            }
        } catch (e: java.net.UnknownHostException) {
            Result.failure(Exception("Sin conexión: ${e.message}"))
        } catch (_: java.net.SocketTimeoutException) {
            Result.failure(Exception("Timeout de conexión"))
        } catch (e: java.io.IOException) {
            Result.failure(Exception("Error de red: ${e.message}"))
        } catch (_: JsonParseException) {
            Result.failure(Exception("Respuesta JSON malformada"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

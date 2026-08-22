import com.sun.net.httpserver.*
import java.net.InetSocketAddress
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

val SUPABASE_URL = "https://liilnrgovltzidqeckit.supabase.co/rest/v1/employees"
val SUPABASE_KEY = "sb_publishable_rvxO7mN-757eDN5cLxJFvA_8YHHSeJ7"

fun callSupabase(endpoint: String, method: String, jsonBody: String? = null): String {
    val url = URL(endpoint)
    val conn = url.openConnection() as HttpURLConnection
    conn.requestMethod = method
    conn.setRequestProperty("apikey", SUPABASE_KEY)
    conn.setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
    conn.setRequestProperty("Content-Type", "application/json")
    conn.setRequestProperty("Prefer", "return=representation")

    if (jsonBody != null && (method == "POST" || method == "PATCH")) {
        conn.doOutput = true
        conn.outputStream.write(jsonBody.toByteArray(Charsets.UTF_8))
    }

    val responseCode = conn.responseCode
    val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
    val response = stream?.bufferedReader()?.use { it.readText() } ?: ""
    return response
}

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    val myServer = HttpServer.create(InetSocketAddress("0.0.0.0", port), 0)

    myServer.createContext("/") { request ->
        if (request.requestMethod == "GET") {
            val htmlFile = File("index.html")
            if (htmlFile.exists()) {
                val bytes = htmlFile.readBytes()
                request.responseHeaders.set("Content-Type", "text/html; charset=UTF-8")
                request.sendResponseHeaders(200, bytes.size.toLong())
                request.responseBody.write(bytes)
                request.responseBody.close()
            } else {
                request.sendResponseHeaders(404, -1)
            }
        }
    }

    myServer.createContext("/style.css") { request ->
        if (request.requestMethod == "GET") {
            val cssFile = File("style.css")
            if (cssFile.exists()) {
                val bytes = cssFile.readBytes()
                request.responseHeaders.set("Content-Type", "text/css; charset=UTF-8")
                request.sendResponseHeaders(200, bytes.size.toLong())
                request.responseBody.write(bytes)
                request.responseBody.close()
            } else {
                request.sendResponseHeaders(404, -1)
            }
        }
    }

    myServer.createContext("/script.js") { request ->
        if (request.requestMethod == "GET") {
            val jsFile = File("script.js")
            if (jsFile.exists()) {
                val bytes = jsFile.readBytes()
                request.responseHeaders.set("Content-Type", "application/javascript; charset=UTF-8")
                request.sendResponseHeaders(200, bytes.size.toLong())
                request.responseBody.write(bytes)
                request.responseBody.close()
            } else {
                request.sendResponseHeaders(404, -1)
            }
        }
    }

    myServer.createContext("/api/register") { request ->
        if (request.requestMethod == "POST") {
            val incomingData = request.requestBody.readBytes().toString(Charsets.UTF_8)
            val empId = incomingData.substringAfter("\"emp_id\":\"").substringBefore("\"")
            
            val checkExisting = callSupabase("$SUPABASE_URL?emp_id=eq.$empId", "GET")
            if (checkExisting.trim() != "[]" && checkExisting.trim().isNotEmpty()) {
                sendJsonResponse(request, """{ "status": "error", "message": "❌ Employee ID pehle se registered hai!" }""")
            } else {
                callSupabase(SUPABASE_URL, "POST", incomingData)
                sendJsonResponse(request, """{ "status": "success", "message": "✅ Employee successfully add ho gaya!" }""")
            }
        }
    }

    myServer.createContext("/api/update") { request ->
        if (request.requestMethod == "POST") {
            val incomingData = request.requestBody.readBytes().toString(Charsets.UTF_8)
            val id = incomingData.substringAfter("\"id\":\"").substringBefore("\"").toIntOrNull() ?: 0
            
            val empName = incomingData.substringAfter("\"emp_name\":\"").substringBefore("\"")
            val empId = incomingData.substringAfter("\"emp_id\":\"").substringBefore("\"")
            val department = incomingData.substringAfter("\"department\":\"").substringBefore("\"")
            val workStatus = incomingData.substringAfter("\"work_status\":\"").substringBefore("\"")
            val mobile = incomingData.substringAfter("\"mobile\":\"").substringBefore("\"")
            
            val patchBody = """{"emp_name":"$empName","emp_id":"$empId","department":"$department","work_status":"$workStatus","mobile":"$mobile"}"""
            callSupabase("$SUPABASE_URL?id=eq.$id", "PATCH", patchBody)
            sendJsonResponse(request, """{ "status": "success", "message": "✅ Employee detail update ho gayi!" }""")
        }
    }

    myServer.createContext("/api/delete") { request ->
        if (request.requestMethod == "POST") {
            val incomingData = request.requestBody.readBytes().toString(Charsets.UTF_8)
            val idStr = incomingData.substringAfter("\"id\":").substringBefore("}").trim().toIntOrNull() ?: 0
            
            callSupabase("$SUPABASE_URL?id=eq.$idStr", "DELETE")
            sendJsonResponse(request, """{ "status": "success", "message": "Employee remove ho gaya!" }""")
        }
    }

    myServer.createContext("/api/users") { request ->
        if (request.requestMethod == "GET") {
            val data = callSupabase("$SUPABASE_URL?select=*&order=id.asc", "GET")
            sendJsonResponse(request, if (data.isBlank()) "[]" else data)
        }
    }

    myServer.createContext("/api/export") { request ->
        if (request.requestMethod == "GET") {
            val data = callSupabase("$SUPABASE_URL?select=*&order=id.asc", "GET")
            var csvData = "ID,Employee Name,Employee ID,Department,Work Status,Mobile No\n"
            
            if (data.startsWith("[") && data.endsWith("]")) {
                val items = data.substring(1, data.length - 1).split("},{")
                for (rawItem in items) {
                    if (rawItem.isBlank()) continue
                    val item = rawItem.replace("{", "").replace("}", "")
                    val id = item.substringAfter("\"id\":").substringBefore(",")
                    val name = item.substringAfter("\"emp_name\":\"").substringBefore("\"")
                    val empId = item.substringAfter("\"emp_id\":\"").substringBefore("\"")
                    val dept = item.substringAfter("\"department\":\"").substringBefore("\"")
                    val status = item.substringAfter("\"work_status\":\"").substringBefore("\"")
                    val mobile = item.substringAfter("\"mobile\":\"").substringBefore("\"")
                    csvData += "$id,$name,$empId,$dept,$status,$mobile\n"
                }
            }

            val bytes = csvData.toByteArray(Charsets.UTF_8)
            request.responseHeaders.add("Content-Type", "text/csv; charset=UTF-8")
            request.responseHeaders.add("Content-Disposition", "attachment; filename=\"hr_employee_data.csv\"")
            request.sendResponseHeaders(200, bytes.size.toLong())
            request.responseBody.write(bytes)
            request.responseBody.close()
        }
    }

    println("🚀 TECHCORP HR BACKEND RUNNING WITH SUPABASE CLOUD DATABASE ON PORT $port")
    myServer.start()
}

fun sendJsonResponse(request: HttpExchange, jsonResponse: String) {
    val bytes = jsonResponse.toByteArray(Charsets.UTF_8)
    request.responseHeaders.add("Content-Type", "application/json; charset=UTF-8")
    request.sendResponseHeaders(200, bytes.size.toLong())
    request.responseBody.write(bytes)
    request.responseBody.close()
}
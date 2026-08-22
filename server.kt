import com.sun.net.httpserver.*
import java.net.InetSocketAddress
import java.io.File

data class Employee(
    val id: Int,
    val empName: String,
    val empId: String,
    val department: String,
    val workStatus: String,
    val mobile: String
)

val dataFile = File("employees_data.csv")

fun loadEmployeesFromFile(): MutableList<Employee> {
    val list = mutableListOf<Employee>()
    if (dataFile.exists()) {
        dataFile.readLines().forEach { line ->
            val parts = line.split("|")
            if (parts.size == 6) {
                val id = parts[0].toIntOrNull() ?: 0
                list.add(Employee(id, parts[1], parts[2], parts[3], parts[4], parts[5]))
            }
        }
    }
    return list
}

fun saveEmployeesToFile(list: List<Employee>) {
    val text = list.joinToString("\n") { "${it.id}|${it.empName}|${it.empId}|${it.department}|${it.workStatus}|${it.mobile}" }
    dataFile.writeText(text)
}

fun main() {
    val employeeList = loadEmployeesFromFile()
    // ✅ Sahi line (har Kotlin version par chalti hai):
   var maxId = 0
    for (emp in employeeList) {
        if (emp.id > maxId) maxId = emp.id
    }
var idCounter = maxId + 1

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
            val empName = incomingData.substringAfter("\"emp_name\":\"").substringBefore("\"")
            val empId = incomingData.substringAfter("\"emp_id\":\"").substringBefore("\"")
            val department = incomingData.substringAfter("\"department\":\"").substringBefore("\"")
            val workStatus = incomingData.substringAfter("\"work_status\":\"").substringBefore("\"")
            val mobile = incomingData.substringAfter("\"mobile\":\"").substringBefore("\"")

            val exists = employeeList.any { it.empId == empId }
            if (exists) {
                sendJsonResponse(request, """{ "status": "error", "message": "❌ Employee ID pehle se registered hai!" }""")
            } else {
                employeeList.add(Employee(idCounter++, empName, empId, department, workStatus, mobile))
                saveEmployeesToFile(employeeList)
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

            val index = employeeList.indexOfFirst { it.id == id }
            if (index != -1) {
                employeeList[index] = Employee(id, empName, empId, department, workStatus, mobile)
                saveEmployeesToFile(employeeList)
            }
            sendJsonResponse(request, """{ "status": "success", "message": "✅ Employee detail update ho gayi!" }""")
        }
    }

    myServer.createContext("/api/delete") { request ->
        if (request.requestMethod == "POST") {
            val incomingData = request.requestBody.readBytes().toString(Charsets.UTF_8)
            val idStr = incomingData.substringAfter("\"id\":").substringBefore("}").trim().toIntOrNull() ?: 0

            employeeList.removeIf { it.id == idStr }
            saveEmployeesToFile(employeeList)
            sendJsonResponse(request, """{ "status": "success", "message": "Employee remove ho gaya!" }""")
        }
    }

    myServer.createContext("/api/users") { request ->
        if (request.requestMethod == "GET") {
            var jsonResponse = "["
            var isFirst = true
            for (emp in employeeList) {
                if (!isFirst) jsonResponse += ","
                jsonResponse += """{"id": ${emp.id}, "emp_name": "${emp.empName}", "emp_id": "${emp.empId}", "department": "${emp.department}", "work_status": "${emp.workStatus}", "mobile": "${emp.mobile}"}"""
                isFirst = false
            }
            jsonResponse += "]"
            sendJsonResponse(request, jsonResponse)
        }
    }

    myServer.createContext("/api/export") { request ->
        if (request.requestMethod == "GET") {
            var csvData = "ID,Employee Name,Employee ID,Department,Work Status,Mobile No\n"
            for (emp in employeeList) {
                csvData += "${emp.id},${emp.empName},${emp.empId},${emp.department},${emp.workStatus},${emp.mobile}\n"
            }
            val bytes = csvData.toByteArray(Charsets.UTF_8)
            request.responseHeaders.add("Content-Type", "text/csv; charset=UTF-8")
            request.responseHeaders.add("Content-Disposition", "attachment; filename=\"hr_employee_data.csv\"")
            request.sendResponseHeaders(200, bytes.size.toLong())
            request.responseBody.write(bytes)
            request.responseBody.close()
        }
    }

    println("🚀 TECHCORP HR BACKEND RUNNING WITH PERMANENT FILE STORAGE ON PORT $port")
    myServer.start()
}

fun sendJsonResponse(request: HttpExchange, jsonResponse: String) {
    val bytes = jsonResponse.toByteArray(Charsets.UTF_8)
    request.responseHeaders.add("Content-Type", "application/json; charset=UTF-8")
    request.sendResponseHeaders(200, bytes.size.toLong())
    request.responseBody.write(bytes)
    request.responseBody.close()
}
import com.sun.net.httpserver.*
import java.net.InetSocketAddress
import java.io.File

// Employee Data Structure
data class Employee(
    val id: Int,
    val empName: String,
    val empId: String,
    val department: String,
    val workStatus: String,
    val mobile: String
)

fun main() {
    // In-Memory Database (Server ki RAM mein data store rahega, Render par bhi chalega!)
    val employeeList = mutableListOf<Employee>(
        Employee(1, "Rahul Sharma", "EMP001", "IT", "Office", "9876543210"),
        Employee(2, "Priya Verma", "EMP002", "HR", "WFH", "9123456789")
    )
    var idCounter = 3

    println("📦 TechCorp HR In-Memory Database Ready hai!")

    val port = System.getenv("PORT")?.toInt() ?: 8080
    val myServer = HttpServer.create(InetSocketAddress("0.0.0.0", port), 0)

    myServer.createContext("/") { request: HttpExchange ->
        if (request.requestMethod == "GET") {
            val htmlFile = File("index.html")
            if (htmlFile.exists()) {
                val bytes = htmlFile.readBytes()
                request.responseHeaders.set("Content-Type", "text/html; charset=UTF-8")
                request.sendResponseHeaders(200, bytes.size.toLong())
                val out = request.responseBody
                out.write(bytes)
                out.close()
            } else {
                val errorMsg = "Error 404: index.html file nahi mili!".toByteArray()
                request.sendResponseHeaders(404, errorMsg.size.toLong())
                request.responseBody.write(errorMsg)
                request.responseBody.close()
            }
        }
    }

    myServer.createContext("/style.css") { request: HttpExchange ->
        if (request.requestMethod == "GET") {
            val cssFile = File("style.css")
            if (cssFile.exists()) {
                val bytes = cssFile.readBytes()
                request.responseHeaders.set("Content-Type", "text/css; charset=UTF-8")
                request.sendResponseHeaders(200, bytes.size.toLong())
                val out = request.responseBody
                out.write(bytes)
                out.close()
            } else {
                request.sendResponseHeaders(404, -1)
            }
        }
    }
    // --- 3. ROUTE: JS (Serve script.js) ---
    // 🔥 Yeh handler zaroori hai taaki JavaScript load ho!
    myServer.createContext("/script.js") { request: HttpExchange ->
        if (request.requestMethod == "GET") {
            val jsFile = File("script.js")
            if (jsFile.exists()) {
                val bytes = jsFile.readBytes()
                // Note: JavaScript ka Content-Type application/javascript hota hai
                request.responseHeaders.set("Content-Type", "application/javascript; charset=UTF-8")
                request.sendResponseHeaders(200, bytes.size.toLong())
                val out = request.responseBody
                out.write(bytes)
                out.close()
            } else {
                request.sendResponseHeaders(404, -1)
            }
        }
    }

    myServer.createContext("/api/register") { request: HttpExchange ->
        if (request.requestMethod == "POST") {
            val incomingData = request.requestBody.readBytes().toString(Charsets.UTF_8)
            val empName = incomingData.substringAfter("\"emp_name\":\"").substringBefore("\"")
            val empId = incomingData.substringAfter("\"emp_id\":\"").substringBefore("\"")
            val department = incomingData.substringAfter("\"department\":\"").substringBefore("\"")
            val workStatus = incomingData.substringAfter("\"work_status\":\"").substringBefore("\"")
            val mobile = incomingData.substringAfter("\"mobile\":\"").substringBefore("\"")

            // Check if Emp ID already exists
            val exists = employeeList.any { it.empId == empId }

            if (exists) {
                val jsonResponse = """{ "status": "error", "message": "❌ Yeh Employee ID pehle se registered hai!" }"""
                val bytes = jsonResponse.toByteArray(Charsets.UTF_8)
                request.responseHeaders.add("Content-Type", "application/json; charset=UTF-8")
                request.sendResponseHeaders(200, bytes.size.toLong())
                val out = request.responseBody
                out.write(bytes)
                out.close()
            } else {
                employeeList.add(Employee(idCounter++, empName, empId, department, workStatus, mobile))
                val jsonResponse = """{ "status": "success", "message": "✅ Employee successfully add ho gaya!" }"""
                val bytes = jsonResponse.toByteArray(Charsets.UTF_8)
                request.responseHeaders.add("Content-Type", "application/json; charset=UTF-8")
                request.sendResponseHeaders(200, bytes.size.toLong())
                val out = request.responseBody
                out.write(bytes)
                out.close()
            }
        }
    }

    myServer.createContext("/api/update") { request: HttpExchange ->
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
            }

            val jsonResponse = """{ "status": "success", "message": "✅ Employee detail update ho gayi!" }"""
            val bytes = jsonResponse.toByteArray(Charsets.UTF_8)
            request.responseHeaders.add("Content-Type", "application/json; charset=UTF-8")
            request.sendResponseHeaders(200, bytes.size.toLong())
            val out = request.responseBody
            out.write(bytes)
            out.close()
        }
    }

    myServer.createContext("/api/delete") { request: HttpExchange ->
        if (request.requestMethod == "POST") {
            val incomingData = request.requestBody.readBytes().toString(Charsets.UTF_8)
            val idStr = incomingData.substringAfter("\"id\":").substringBefore("}").trim().toIntOrNull() ?: 0

            employeeList.removeIf { it.id == idStr }

            val jsonResponse = """{ "status": "success", "message": "Employee database se remove ho gaya!" }"""
            val bytes = jsonResponse.toByteArray(Charsets.UTF_8)
            request.responseHeaders.add("Content-Type", "application/json; charset=UTF-8")
            request.sendResponseHeaders(200, bytes.size.toLong())
            val out = request.responseBody
            out.write(bytes)
            out.close()
        }
    }

    myServer.createContext("/api/users") { request: HttpExchange ->
        if (request.requestMethod == "GET") {
            var jsonResponse = "["
            var isFirst = true
            for (emp in employeeList) {
                if (!isFirst) jsonResponse += ","
                jsonResponse += """{"id": ${emp.id}, "emp_name": "${emp.empName}", "emp_id": "${emp.empId}", "department": "${emp.department}", "work_status": "${emp.workStatus}", "mobile": "${emp.mobile}"}"""
                isFirst = false
            }
            jsonResponse += "]"

            val bytes = jsonResponse.toByteArray(Charsets.UTF_8)
            request.responseHeaders.add("Content-Type", "application/json; charset=UTF-8")
            request.sendResponseHeaders(200, bytes.size.toLong())
            val out = request.responseBody
            out.write(bytes)
            out.close()
        }
    }

    myServer.createContext("/api/export") { request: HttpExchange ->
        if (request.requestMethod == "GET") {
            var csvData = "ID,Employee Name,Employee ID,Department,Work Status,Mobile No\n"
            for (emp in employeeList) {
                csvData += "${emp.id},${emp.empName},${emp.empId},${emp.department},${emp.workStatus},${emp.mobile}\n"
            }

            val bytes = csvData.toByteArray(Charsets.UTF_8)
            request.responseHeaders.add("Content-Type", "text/csv; charset=UTF-8")
            request.responseHeaders.add("Content-Disposition", "attachment; filename=\"hr_employee_data.csv\"")
            request.sendResponseHeaders(200, bytes.size.toLong())
            
            val out = request.responseBody
            out.write(bytes)
            out.close()
        }
    }

    println("🚀 TECHCORP HR BACKEND START HO GAYA HAI!")
    println("👉 Server Cloud Port $port par chal raha hai...")
    myServer.start()
}
import com.sun.net.httpserver.*
import java.net.InetSocketAddress
import java.sql.DriverManager
import java.io.File

fun main() {
    val dbUrl = "jdbc:sqlite:my_database.db"
    Class.forName("org.sqlite.JDBC")
    
    val connection = DriverManager.getConnection(dbUrl)
    val statement = connection.createStatement()
    // 👉 NAYI TABLE: Ab Employees ki details save hongi
    statement.execute("CREATE TABLE IF NOT EXISTS employees (id INTEGER PRIMARY KEY AUTOINCREMENT, emp_name TEXT, emp_id TEXT, department TEXT, work_status TEXT, mobile TEXT)")
    println("📦 TechCorp HR Database Ready hai!")

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

    myServer.createContext("/api/register") { request: HttpExchange ->
        if (request.requestMethod == "POST") {
            val incomingData = request.requestBody.readBytes().toString(Charsets.UTF_8)
            val empName = incomingData.substringAfter("\"emp_name\":\"").substringBefore("\"")
            val empId = incomingData.substringAfter("\"emp_id\":\"").substringBefore("\"")
            val department = incomingData.substringAfter("\"department\":\"").substringBefore("\"")
            val workStatus = incomingData.substringAfter("\"work_status\":\"").substringBefore("\"")
            val mobile = incomingData.substringAfter("\"mobile\":\"").substringBefore("\"")

            val checkRs = connection.createStatement().executeQuery("SELECT count(*) AS count FROM employees WHERE emp_id = '$empId'")
            val count = if (checkRs.next()) checkRs.getInt("count") else 0

            if (count > 0) {
                val jsonResponse = """{ "status": "error", "message": "❌ Yeh Employee ID pehle se registered hai!" }"""
                val bytes = jsonResponse.toByteArray(Charsets.UTF_8)
                request.responseHeaders.add("Content-Type", "application/json; charset=UTF-8")
                request.sendResponseHeaders(200, bytes.size.toLong())
                val out = request.responseBody
                out.write(bytes)
                out.close()
            } else {
                val insertQuery = "INSERT INTO employees (emp_name, emp_id, department, work_status, mobile) VALUES ('$empName', '$empId', '$department', '$workStatus', '$mobile')"
                connection.createStatement().execute(insertQuery)

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
            val id = incomingData.substringAfter("\"id\":\"").substringBefore("\"")
            val empName = incomingData.substringAfter("\"emp_name\":\"").substringBefore("\"")
            val empId = incomingData.substringAfter("\"emp_id\":\"").substringBefore("\"")
            val department = incomingData.substringAfter("\"department\":\"").substringBefore("\"")
            val workStatus = incomingData.substringAfter("\"work_status\":\"").substringBefore("\"")
            val mobile = incomingData.substringAfter("\"mobile\":\"").substringBefore("\"")

            val updateQuery = "UPDATE employees SET emp_name = '$empName', emp_id = '$empId', department = '$department', work_status = '$workStatus', mobile = '$mobile' WHERE id = $id"
            connection.createStatement().execute(updateQuery)

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
            val idStr = incomingData.substringAfter("\"id\":").substringBefore("}").trim()

            val deleteQuery = "DELETE FROM employees WHERE id = $idStr"
            connection.createStatement().execute(deleteQuery)

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
            val resultSet = connection.createStatement().executeQuery("SELECT * FROM employees")
            var jsonResponse = "["
            var isFirst = true
            while (resultSet.next()) {
                if (!isFirst) jsonResponse += ","
                val id = resultSet.getInt("id")
                val empName = resultSet.getString("emp_name")
                val empId = resultSet.getString("emp_id")
                val department = resultSet.getString("department")
                val workStatus = resultSet.getString("work_status")
                val mobile = resultSet.getString("mobile")
                jsonResponse += """{"id": $id, "emp_name": "$empName", "emp_id": "$empId", "department": "$department", "work_status": "$workStatus", "mobile": "$mobile"}"""
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
            val resultSet = connection.createStatement().executeQuery("SELECT * FROM employees")
            
            var csvData = "ID,Employee Name,Employee ID,Department,Work Status,Mobile No\n"
            
            while (resultSet.next()) {
                val id = resultSet.getInt("id")
                val empName = resultSet.getString("emp_name")
                val empId = resultSet.getString("emp_id")
                val department = resultSet.getString("department")
                val workStatus = resultSet.getString("work_status")
                val mobile = resultSet.getString("mobile")
                csvData += "$id,$empName,$empId,$department,$workStatus,$mobile\n"
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
    println("👉 Local Check ke liye yahan click karein: http://localhost:$port/")
    myServer.start()
}
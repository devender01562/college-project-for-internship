import com.sun.net.httpserver.*
import java.net.InetSocketAddress
import java.sql.DriverManager
import java.io.File

fun main() {
    val dbUrl = "jdbc:sqlite:my_database.db"
    Class.forName("org.sqlite.JDBC")
    
    val connection = DriverManager.getConnection(dbUrl)
    val statement = connection.createStatement()
    // 👉 NAYI TABLE: Ab device ki details save hongi
    statement.execute("CREATE TABLE IF NOT EXISTS devices (id INTEGER PRIMARY KEY AUTOINCREMENT, owner_name TEXT, device_id TEXT, battery TEXT, status TEXT, emergency_contact TEXT)")
    println("📦 VisionCompanion Database Ready hai!")

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
            val owner = incomingData.substringAfter("\"owner_name\":\"").substringBefore("\"")
            val deviceId = incomingData.substringAfter("\"device_id\":\"").substringBefore("\"")
            val battery = incomingData.substringAfter("\"battery\":\"").substringBefore("\"")
            val status = incomingData.substringAfter("\"status\":\"").substringBefore("\"")
            val emergency = incomingData.substringAfter("\"emergency_contact\":\"").substringBefore("\"")

            val checkRs = connection.createStatement().executeQuery("SELECT count(*) AS count FROM devices WHERE device_id = '$deviceId'")
            val count = if (checkRs.next()) checkRs.getInt("count") else 0

            if (count > 0) {
                val jsonResponse = """{ "status": "error", "message": "❌ Yeh Device ID pehle se registered hai!" }"""
                val bytes = jsonResponse.toByteArray(Charsets.UTF_8)
                request.responseHeaders.add("Content-Type", "application/json; charset=UTF-8")
                request.sendResponseHeaders(200, bytes.size.toLong())
                val out = request.responseBody
                out.write(bytes)
                out.close()
            } else {
                val insertQuery = "INSERT INTO devices (owner_name, device_id, battery, status, emergency_contact) VALUES ('$owner', '$deviceId', '$battery', '$status', '$emergency')"
                connection.createStatement().execute(insertQuery)

                val jsonResponse = """{ "status": "success", "message": "✅ Device successfully register ho gaya!" }"""
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
            val owner = incomingData.substringAfter("\"owner_name\":\"").substringBefore("\"")
            val deviceId = incomingData.substringAfter("\"device_id\":\"").substringBefore("\"")
            val battery = incomingData.substringAfter("\"battery\":\"").substringBefore("\"")
            val status = incomingData.substringAfter("\"status\":\"").substringBefore("\"")
            val emergency = incomingData.substringAfter("\"emergency_contact\":\"").substringBefore("\"")

            val updateQuery = "UPDATE devices SET owner_name = '$owner', device_id = '$deviceId', battery = '$battery', status = '$status', emergency_contact = '$emergency' WHERE id = $id"
            connection.createStatement().execute(updateQuery)

            val jsonResponse = """{ "status": "success", "message": "✅ Device status update ho gaya!" }"""
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

            val deleteQuery = "DELETE FROM devices WHERE id = $idStr"
            connection.createStatement().execute(deleteQuery)

            val jsonResponse = """{ "status": "success", "message": "Device database se delete ho gaya!" }"""
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
            val resultSet = connection.createStatement().executeQuery("SELECT * FROM devices")
            var jsonResponse = "["
            var isFirst = true
            while (resultSet.next()) {
                if (!isFirst) jsonResponse += ","
                val id = resultSet.getInt("id")
                val owner = resultSet.getString("owner_name")
                val deviceId = resultSet.getString("device_id")
                val battery = resultSet.getString("battery")
                val status = resultSet.getString("status")
                val emergency = resultSet.getString("emergency_contact")
                jsonResponse += """{"id": $id, "owner_name": "$owner", "device_id": "$deviceId", "battery": "$battery", "status": "$status", "emergency_contact": "$emergency"}"""
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
            val resultSet = connection.createStatement().executeQuery("SELECT * FROM devices")
            
            var csvData = "ID,Owner Name,Device ID,Battery,Status,Emergency Contact\n"
            
            while (resultSet.next()) {
                val id = resultSet.getInt("id")
                val owner = resultSet.getString("owner_name")
                val deviceId = resultSet.getString("device_id")
                val battery = resultSet.getString("battery")
                val status = resultSet.getString("status")
                val emergency = resultSet.getString("emergency_contact")
                csvData += "$id,$owner,$deviceId,$battery,$status,$emergency\n"
            }

            val bytes = csvData.toByteArray(Charsets.UTF_8)
            request.responseHeaders.add("Content-Type", "text/csv; charset=UTF-8")
            request.responseHeaders.add("Content-Disposition", "attachment; filename=\"device_logs.csv\"")
            request.sendResponseHeaders(200, bytes.size.toLong())
            
            val out = request.responseBody
            out.write(bytes)
            out.close()
        }
    }

   println("🚀 VISION COMPANION BACKEND START HO GAYA HAI!")
    println("👉 Server Cloud Port $port par chal raha hai...")
    println("👉 Local Check ke liye yahan click karein: http://localhost:$port/") // Yeh line add karni hai
    myServer.start()
}
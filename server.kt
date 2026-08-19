import com.sun.net.httpserver.*
import java.net.InetSocketAddress
import java.sql.DriverManager
import java.io.File

fun main() {
    val dbUrl = "jdbc:sqlite:my_database.db"
    Class.forName("org.sqlite.JDBC")
    
    val connection = DriverManager.getConnection(dbUrl)
    val statement = connection.createStatement()
    // 👉 NAYI TABLE: Ab isme age, gender aur disability_type bhi save hoga
    statement.execute("CREATE TABLE IF NOT EXISTS profiles (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, email TEXT, age TEXT, gender TEXT, disability_type TEXT)")
    println("📦 InclusionMatrimony Database Ready hai!")

    val myServer = HttpServer.create(InetSocketAddress(8080), 0)

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

    // 👉 POST API (Create Profile)
    myServer.createContext("/api/register") { request: HttpExchange ->
        if (request.requestMethod == "POST") {
            val incomingData = request.requestBody.readBytes().toString(Charsets.UTF_8)
            val name = incomingData.substringAfter("\"name\":\"").substringBefore("\"")
            val email = incomingData.substringAfter("\"email\":\"").substringBefore("\"")
            val age = incomingData.substringAfter("\"age\":\"").substringBefore("\"")
            val gender = incomingData.substringAfter("\"gender\":\"").substringBefore("\"")
            val disability = incomingData.substringAfter("\"disability_type\":\"").substringBefore("\"")

            val checkRs = connection.createStatement().executeQuery("SELECT count(*) AS count FROM profiles WHERE email = '$email'")
            val count = if (checkRs.next()) checkRs.getInt("count") else 0

            if (count > 0) {
                val jsonResponse = """{ "status": "error", "message": "❌ Yeh Email ID pehle se registered hai!" }"""
                val bytes = jsonResponse.toByteArray(Charsets.UTF_8)
                request.responseHeaders.add("Content-Type", "application/json; charset=UTF-8")
                request.sendResponseHeaders(200, bytes.size.toLong())
                val out = request.responseBody
                out.write(bytes)
                out.close()
            } else {
                val insertQuery = "INSERT INTO profiles (name, email, age, gender, disability_type) VALUES ('$name', '$email', '$age', '$gender', '$disability')"
                connection.createStatement().execute(insertQuery)

                val jsonResponse = """{ "status": "success", "message": "✅ Profile successfully ban gayi!" }"""
                val bytes = jsonResponse.toByteArray(Charsets.UTF_8)
                request.responseHeaders.add("Content-Type", "application/json; charset=UTF-8")
                request.sendResponseHeaders(200, bytes.size.toLong())
                val out = request.responseBody
                out.write(bytes)
                out.close()
            }
        }
    }

    // 👉 UPDATE API (Edit Profile)
    myServer.createContext("/api/update") { request: HttpExchange ->
        if (request.requestMethod == "POST") {
            val incomingData = request.requestBody.readBytes().toString(Charsets.UTF_8)
            val id = incomingData.substringAfter("\"id\":\"").substringBefore("\"")
            val name = incomingData.substringAfter("\"name\":\"").substringBefore("\"")
            val email = incomingData.substringAfter("\"email\":\"").substringBefore("\"")
            val age = incomingData.substringAfter("\"age\":\"").substringBefore("\"")
            val gender = incomingData.substringAfter("\"gender\":\"").substringBefore("\"")
            val disability = incomingData.substringAfter("\"disability_type\":\"").substringBefore("\"")

            val checkRs = connection.createStatement().executeQuery("SELECT count(*) AS count FROM profiles WHERE email = '$email' AND id != $id")
            val count = if (checkRs.next()) checkRs.getInt("count") else 0

            if (count > 0) {
                val jsonResponse = """{ "status": "error", "message": "❌ Yeh Email pehle se kisi aur ke paas hai!" }"""
                val bytes = jsonResponse.toByteArray(Charsets.UTF_8)
                request.responseHeaders.add("Content-Type", "application/json; charset=UTF-8")
                request.sendResponseHeaders(200, bytes.size.toLong())
                val out = request.responseBody
                out.write(bytes)
                out.close()
            } else {
                val updateQuery = "UPDATE profiles SET name = '$name', email = '$email', age = '$age', gender = '$gender', disability_type = '$disability' WHERE id = $id"
                connection.createStatement().execute(updateQuery)

                val jsonResponse = """{ "status": "success", "message": "✅ Profile Update ho gayi!" }"""
                val bytes = jsonResponse.toByteArray(Charsets.UTF_8)
                request.responseHeaders.add("Content-Type", "application/json; charset=UTF-8")
                request.sendResponseHeaders(200, bytes.size.toLong())
                val out = request.responseBody
                out.write(bytes)
                out.close()
            }
        }
    }

    // 👉 DELETE API
    myServer.createContext("/api/delete") { request: HttpExchange ->
        if (request.requestMethod == "POST") {
            val incomingData = request.requestBody.readBytes().toString(Charsets.UTF_8)
            val idStr = incomingData.substringAfter("\"id\":").substringBefore("}").trim()

            val deleteQuery = "DELETE FROM profiles WHERE id = $idStr"
            connection.createStatement().execute(deleteQuery)

            val jsonResponse = """{ "status": "success", "message": "Profile database se hamesha ke liye delete ho gayi!" }"""
            val bytes = jsonResponse.toByteArray(Charsets.UTF_8)
            request.responseHeaders.add("Content-Type", "application/json; charset=UTF-8")
            request.sendResponseHeaders(200, bytes.size.toLong())
            val out = request.responseBody
            out.write(bytes)
            out.close()
        }
    }

    // 👉 GET API (Load Profiles)
    myServer.createContext("/api/users") { request: HttpExchange ->
        if (request.requestMethod == "GET") {
            val resultSet = connection.createStatement().executeQuery("SELECT * FROM profiles")
            var jsonResponse = "["
            var isFirst = true
            while (resultSet.next()) {
                if (!isFirst) jsonResponse += ","
                val id = resultSet.getInt("id")
                val name = resultSet.getString("name")
                val email = resultSet.getString("email")
                val age = resultSet.getString("age")
                val gender = resultSet.getString("gender")
                val disability = resultSet.getString("disability_type")
                jsonResponse += """{"id": $id, "name": "$name", "email": "$email", "age": "$age", "gender": "$gender", "disability_type": "$disability"}"""
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

    // 👉 EXCEL (CSV) EXPORT API (Updated fields)
    myServer.createContext("/api/export") { request: HttpExchange ->
        if (request.requestMethod == "GET") {
            val resultSet = connection.createStatement().executeQuery("SELECT * FROM profiles")
            
            var csvData = "ID,Name,Email,Age,Gender,Disability Type\n"
            
            while (resultSet.next()) {
                val id = resultSet.getInt("id")
                val name = resultSet.getString("name")
                val email = resultSet.getString("email")
                val age = resultSet.getString("age")
                val gender = resultSet.getString("gender")
                val disability = resultSet.getString("disability_type")
                csvData += "$id,$name,$email,$age,$gender,$disability\n"
            }

            val bytes = csvData.toByteArray(Charsets.UTF_8)
            request.responseHeaders.add("Content-Type", "text/csv; charset=UTF-8")
            request.responseHeaders.add("Content-Disposition", "attachment; filename=\"matrimony_profiles.csv\"")
            request.sendResponseHeaders(200, bytes.size.toLong())
            
            val out = request.responseBody
            out.write(bytes)
            out.close()
        }
    }

    println("🚀 INCLUSION MATRIMONY BACKEND START HO GAYA HAI!")
    println("👉 Website yahan dekhein: http://localhost:8080/")
    myServer.start()
}
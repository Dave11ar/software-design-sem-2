import com.mongodb.rx.client.MongoClients
import com.typesafe.config.ConfigFactory
import config.DatabaseConfig
import config.ConverterConfig
import config.ServerConfig
import currency.ConverterProvider
import dao.MongoReactiveDao
import http.ApacheHttpClient
import io.reactivex.netty.protocol.http.server.HttpServer
import query.Query
import java.nio.file.Paths

fun main() {
    val confFile = Paths.get("src/main/resources/application.conf").toFile()
    val appConfig = ConfigFactory.parseFile(confFile)
    val serverConfig = ServerConfig.fromConfig(appConfig.getConfig("server"))
    val converterConfig = ConverterConfig.fromConfig(appConfig.getConfig("converter"))
    val databaseConfig = DatabaseConfig.fromConfig(appConfig.getConfig("database"))
    val converterProvider = ConverterProvider(converterConfig, ApacheHttpClient.getInstance())
    val mongoUrl = "${databaseConfig.schema}://${databaseConfig.host}:${databaseConfig.port}"
    val dao = MongoReactiveDao(
        MongoClients.create(mongoUrl).getDatabase(databaseConfig.name),
        converterProvider
    )
    HttpServer.newServer(serverConfig.port)
        .start { request, response ->
            val query = Query.fromRequest(request)
            response.writeString(query.process(dao).map { "$it\n" })
        }.awaitShutdown()
}
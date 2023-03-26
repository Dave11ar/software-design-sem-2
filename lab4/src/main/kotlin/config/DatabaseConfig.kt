package config

import com.typesafe.config.Config

data class DatabaseConfig(val schema: String, val host: String, val port: Int, val name: String) {
    companion object {
        fun fromConfig(conf: Config) = DatabaseConfig(
            conf.getString("schema"),
            conf.getString("host"),
            conf.getInt("port"),
            conf.getString("name")
        )
    }
}
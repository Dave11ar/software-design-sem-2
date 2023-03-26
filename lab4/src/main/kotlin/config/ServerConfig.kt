package config

import com.typesafe.config.Config

data class ServerConfig(val port: Int) {
    companion object {
        fun fromConfig(conf: Config) = ServerConfig(conf.getInt("port"))
    }
}
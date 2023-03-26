package config

import com.typesafe.config.Config

data class ConverterConfig(val schema: String, val host: String, val path: String) {
    companion object {
        fun fromConfig(conf: Config) = ConverterConfig(
            conf.getString("schema"),
            conf.getString("host"),
            conf.getString("path")
        )
    }
}
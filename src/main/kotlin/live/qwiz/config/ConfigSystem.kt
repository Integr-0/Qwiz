package live.qwiz.config

import com.charleskorn.kaml.Yaml
import live.qwiz.config.parts.ConfigTree
import kotlin.io.path.Path
import kotlin.io.path.readText

class ConfigSystem {
    companion object {
        private var config: ConfigTree? = null

        fun load() {
            config = Yaml.default.decodeFromString(ConfigTree.serializer(), Path("./application.yaml").readText())
        }

        fun get(): ConfigTree {
            return config!!
        }
    }
}
package ai.blockwell.qrdemo.data

import ai.blockwell.qrdemo.WalletApplication
import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable

@Serializable
data class ShortcutScreenConfig(
        val title: String,
        val description: String,
        val shortcodes: List<String>
)

@Serializable
data class Shortcuts(
        val screens: List<ShortcutScreenConfig>,
        val displayFab: Boolean
)

object ShortcutConfig {
    val config by lazy {
        val content = WalletApplication.app.assets.open("shortcuts.yaml").bufferedReader().use { it.readText() }

        Yaml.default.parse(Shortcuts.serializer(), content)
    }
}

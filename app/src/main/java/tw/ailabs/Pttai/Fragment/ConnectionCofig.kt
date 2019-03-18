package tw.ailabs.Pttai.Fragment

import android.content.Context
import android.preference.PreferenceManager
import com.google.gson.Gson

data class HostConfig(val ip: String = "",
                 val user: String = "",
                 val pwd: String = "") {
	companion object {
		const val name = "HOST_CONFIG"
	}
}

data class PortConfig(val externalHttpPort: Int = 9774,
                 val externalApiPort: Int = 14779,
                 val internalHttpPort: Int = 9774,
                 val internalApiPort: Int = 14779) {
	companion object {
		const val name = "PORT_CONFIG"
	}
}

class ConfigUtils {
	companion object {
		fun <T> getConfigFromPreference(context: Context, key: String, entityClass: Class<T>): T {
			val value = PreferenceManager.getDefaultSharedPreferences(context).getString(key, "")

			return if (false != value?.isNotEmpty()) {
				Gson().fromJson(value , entityClass)
			} else {
				entityClass.newInstance()
			}
		}

		fun saveConfigToPreference(context: Context, key: String, value: Any) {
			PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, Gson().toJson(value)).apply()
		}
	}
}

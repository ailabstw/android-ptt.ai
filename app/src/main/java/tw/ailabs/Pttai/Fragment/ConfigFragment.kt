package tw.ailabs.Pttai.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_config.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import tw.ailabs.Pttai.R

enum class CONNECTION_STATE {
	START,
	CONNECTED,
	FAILED
}

class ConfigFragment : Fragment() {

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_config, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		btn_login.apply {
			setOnClickListener {
				val editTextList = listOf(edit_ip, edit_user, edit_password,
					edit_external_http_port, edit_external_api_port,
					edit_internal_http_port, edit_internal_api_port)

				for (editText in editTextList) {
					if (editText.text.isEmpty()) {
						Toast.makeText(context, "Please fill \"${editText.hint}\"", Toast.LENGTH_SHORT).show()
						return@setOnClickListener
					}
				}

				ConfigUtils.saveConfigToPreference(context,
					HostConfig.name,
					HostConfig(edit_ip.text.toString(),
						edit_user.text.toString(),
						edit_password.text.toString())
				)
				ConfigUtils.saveConfigToPreference(context,
					PortConfig.name,
					PortConfig(edit_external_http_port.text.toString().toInt(),
						edit_external_api_port.text.toString().toInt(),
						edit_internal_http_port.text.toString().toInt(),
						edit_internal_api_port.text.toString().toInt())
				)

				isEnabled = false
				EventBus.getDefault().post(CONNECTION_STATE.START)
			}
		}

		btn_port_settings.setOnClickListener {
			port_settings.visibility = if (port_settings.visibility == View.VISIBLE) View.GONE else View.VISIBLE
		}

		val hostConfig = ConfigUtils.getConfigFromPreference(view.context, HostConfig.name, HostConfig::class.java)
		val portConfig = ConfigUtils.getConfigFromPreference(view.context, PortConfig.name, PortConfig::class.java)

		edit_ip.setText(hostConfig.ip)
		edit_user.setText(hostConfig.user)
		edit_password.setText(hostConfig.pwd)
		edit_external_http_port.setText(portConfig.externalHttpPort.toString())
		edit_external_api_port.setText(portConfig.externalApiPort.toString())
		edit_internal_http_port.setText(portConfig.internalHttpPort.toString())
		edit_internal_api_port.setText(portConfig.internalApiPort.toString())
	}

	override fun onStart() {
		EventBus.getDefault().register(this);
		super.onStart()
	}

	override fun onStop() {
		EventBus.getDefault().unregister(this);
		super.onStop()
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	fun onMessageEvent(stat: CONNECTION_STATE) {
		when (stat) {
			CONNECTION_STATE.START -> {
				view_loading.visibility = View.VISIBLE
			}
			CONNECTION_STATE.FAILED,
			CONNECTION_STATE.CONNECTED -> {
				view_loading.visibility = View.GONE
				btn_login.isEnabled = true
			}
		}
	}
}

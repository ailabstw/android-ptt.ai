package tw.ailabs.Pttai.Service

import android.app.Service
import android.content.Intent
import android.os.AsyncTask
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import org.greenrobot.eventbus.EventBus
import tw.ailabs.Pttai.Fragment.ConfigUtils.Companion.getConfigFromPreference
import tw.ailabs.Pttai.Fragment.CONNECTION_STATE
import tw.ailabs.Pttai.Fragment.HostConfig
import tw.ailabs.Pttai.Fragment.PortConfig

class PttAiService : Service() {
	private val mBinder: IBinder = LocalBinder()
	private var mTunnel: Session? = null

	inner class LocalBinder : Binder() {
		internal// Return this instance of LocalService so clients can call public methods
		val service: PttAiService
			get() = this@PttAiService
	}

	override fun onBind(intent: Intent): IBinder {
		createTunnel()

		return mBinder
	}

	override fun onUnbind(intent: Intent?): Boolean {
		mTunnel?.disconnect()
		mTunnel = null

		return super.onUnbind(intent)
	}

	private fun createTunnel() {
		val hostConfig = getConfigFromPreference(baseContext, HostConfig.name, HostConfig::class.java)
		val portConfig = getConfigFromPreference(baseContext, PortConfig.name, PortConfig::class.java)

		AsyncConnectSsh(object : AsyncConnectSsh.AsyncResponse {
			override fun processFinish(session: Session?) {
				if ((null != session) && (session.isConnected)) {
					mTunnel = session
					EventBus.getDefault().post(CONNECTION_STATE.CONNECTED)
				} else {
					EventBus.getDefault().post(CONNECTION_STATE.FAILED)
				}
			}
		}, hostConfig, portConfig).execute()
	}
}

class AsyncConnectSsh(private val mDelegate: AsyncResponse,
                      private val mHost: HostConfig,
                      private val mPort: PortConfig): AsyncTask<Unit, Unit, Session>() {
	private val TAG = AsyncConnectSsh::class.java.simpleName

	interface AsyncResponse {
		fun processFinish(session: Session?)
	}

	override fun doInBackground(vararg params: Unit?): Session? {
		var session: Session? = null

		try {
			//http://eridem.net/android-tip-021-ssh-execute-remote-commands-with-android
			val ssh = JSch()

			session = ssh.getSession(mHost.user, mHost.ip)
			session.setPassword(mHost.pwd)
			// Avoid asking for key confirmation
			session.setConfig("StrictHostKeyChecking", "no")
			session.connect(10000)

			session.setPortForwardingL(mPort.externalHttpPort, "localhost", mPort.internalHttpPort)
			session.openChannel("direct-tcpip")
			session.setPortForwardingL(mPort.externalApiPort, "localhost", mPort.internalApiPort)
			session.openChannel("direct-tcpip")
		} catch (e: Exception) {
			Log.e(TAG, "[SSH] Exception: $e")
			session?.disconnect()
			session = null
		}

		return session
	}

	override fun onPostExecute(session: Session?) {
		mDelegate.processFinish(session)
	}
}

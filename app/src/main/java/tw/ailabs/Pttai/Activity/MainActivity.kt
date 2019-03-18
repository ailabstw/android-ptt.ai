package tw.ailabs.Pttai.Activity

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pub.devrel.easypermissions.EasyPermissions
import tw.ailabs.Pttai.*
import tw.ailabs.Pttai.Fragment.AndroidBug5497Workaround
import tw.ailabs.Pttai.Fragment.CONNECTION_STATE
import tw.ailabs.Pttai.Fragment.ConfigFragment
import tw.ailabs.Pttai.Fragment.WebViewFragment
import tw.ailabs.Pttai.Service.PttAiService
import tw.ailabs.Pttai.Service.PttAiService.LocalBinder

class MainActivity : AppCompatActivity() {
	val FRAGMENT_TAG = "fragment_tag"
	val REQUEST_CODE = 10
	var mService: PttAiService? = null
	var mBound = false
	var mblShouldExit = false

	private val mConnection = object : ServiceConnection {

		override fun onServiceConnected(className: ComponentName,
		                                service: IBinder) {
			val binder = service as LocalBinder

			mService = binder.service
			mBound = true
		}

		override fun onServiceDisconnected(arg0: ComponentName) {
			switchFragment(false)
			mBound = false
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		// Check permissions
		val perms = arrayOf(Manifest.permission.CAMERA,
										Manifest.permission.MODIFY_AUDIO_SETTINGS,
										Manifest.permission.RECORD_AUDIO)

		if (!EasyPermissions.hasPermissions(this, *perms)) {
			EasyPermissions.requestPermissions(this, "", REQUEST_CODE, *perms)
		}

		// Show config fragment as default
		switchFragment(false)
		AndroidBug5497Workaround.assistActivity(this)
	}

	override fun onStart() {
		super.onStart()
		EventBus.getDefault().register(this)
	}

	override fun onStop() {
		super.onStop()
		EventBus.getDefault().unregister(this)
	}

	override fun onDestroy() {
		super.onDestroy()
		disconnect()
	}

	override fun onBackPressed() {
		val fragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)

		if ((fragment is WebViewFragment) && (fragment.handleBackKey())) {
			// Do nothing
		} else if (!mblShouldExit) {
			Handler().postDelayed({
				mblShouldExit = false
			}, 3000)
			mblShouldExit = true

			Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show()
		} else {
			super.onBackPressed()
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)

		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
	}

	private fun disconnect() {
		if (mBound) {
			unbindService(mConnection)
			mBound = false
		}
	}

	private fun switchFragment(blLogged: Boolean) {
		val fragment = if (blLogged) WebViewFragment() else ConfigFragment()

		supportFragmentManager.beginTransaction().replace(R.id.main_container, fragment, FRAGMENT_TAG).commit()
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	fun onEventFire(stat: CONNECTION_STATE) {
		when (stat) {
			CONNECTION_STATE.START -> {
				bindService(Intent(this, PttAiService::class.java), mConnection, Context.BIND_AUTO_CREATE)
			}
			CONNECTION_STATE.FAILED -> {
				disconnect()
				Toast.makeText(this, "Login Failed!!", Toast.LENGTH_SHORT).show()
			}
			CONNECTION_STATE.CONNECTED -> {
				switchFragment(true)
			}
		}
	}
}

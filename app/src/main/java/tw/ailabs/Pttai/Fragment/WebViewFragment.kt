package tw.ailabs.Pttai.Fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.webkit.WebView.setWebContentsDebuggingEnabled
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_webview.*
import tw.ailabs.Pttai.BuildConfig
import tw.ailabs.Pttai.R

class WebViewFragment : Fragment() {
	private val REQUEST_SELECT_FILE = 100
	private var mUploadMessage: ValueCallback<Array<Uri>>? = null

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_webview, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		openWebView()
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (requestCode == REQUEST_SELECT_FILE) {
			mUploadMessage?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data))
			mUploadMessage = null
		} else {
			super.onActivityResult(requestCode, resultCode, data)
		}
	}

	private fun openWebView() {
		val portConfig = ConfigUtils.getConfigFromPreference(context!!, PortConfig.name, PortConfig::class.java)

		webview_pttai.apply {
			clearCache(true)
			clearHistory()
			settings.javaScriptEnabled = true

			//https://developers.google.com/web/tools/chrome-devtools/remote-debugging/webviews
			setWebContentsDebuggingEnabled(BuildConfig.DEBUG)

			webChromeClient = object: WebChromeClient() {
				override fun onPermissionRequest(request: PermissionRequest?) {
					activity?.runOnUiThread {
						request?.grant(request.resources)
					}
				}

				override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
					mUploadMessage?.onReceiveValue(null)
					mUploadMessage = filePathCallback

					fileChooserParams?.let {
						startActivityForResult(fileChooserParams.createIntent(), REQUEST_SELECT_FILE)
					}

					return true
				}
			}

			loadUrl("http://localhost:${portConfig.externalHttpPort}")
		}

	}

	fun handleBackKey(): Boolean {
		var blInterrupted = false

		if (webview_pttai.canGoBack()) {
			webview_pttai.goBack()
			blInterrupted = true
		}

		return blInterrupted
	}
}

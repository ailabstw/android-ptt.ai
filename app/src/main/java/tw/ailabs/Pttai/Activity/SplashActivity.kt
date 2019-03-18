package tw.ailabs.Pttai.Activity

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import kotlinx.android.synthetic.main.activity_splash.*
import tw.ailabs.Pttai.R


class SplashActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_splash)

		Handler().postDelayed({
			startAnimation()
		}, 500)
	}

	private fun startAnimation() {
		val offset = resources.getDimension(R.dimen.splash_icon_margin_top) - img_splash_icon.top

		//Start animation of Splash icon
		ObjectAnimator.ofFloat(img_splash_icon, "translationY", offset).apply {
			duration = 1000
			addListener(object: Animator.AnimatorListener {
				override fun onAnimationStart(animation: Animator?) {}
				override fun onAnimationRepeat(animation: Animator?) {}
				override fun onAnimationCancel(animation: Animator?) {}

				override fun onAnimationEnd(animation: Animator?) {
					fadeOutAndShowMainActivity()
				}
			})
			start()
		}
	}

	private fun fadeOutAndShowMainActivity() {
		startActivity(Intent(this, MainActivity::class.java))
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
		finish()
	}
}

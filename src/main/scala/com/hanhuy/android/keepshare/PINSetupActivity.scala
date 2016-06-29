package com.hanhuy.android.keepshare

import android.support.v7.app.AppCompatActivity
import com.hanhuy.android.common._
import com.hanhuy.android.extensions._
import android.app.Activity
import android.os.Bundle
import android.view.View
import com.hanhuy.android.common.{Futures, UiBus}
import Futures._
import android.widget.Toast
import org.acra.ACRA

class PINSetupActivity extends AppCompatActivity with TypedFindView {

  lazy val pinEntry = findView(TR.pin)
  lazy val ok = findView(TR.pin_ok)
  lazy val error = findView(TR.pin_error)
  lazy val back = findView(TR.pin_back)
  private var pin = Seq.empty[String]
  private var selectedPin = pin
  lazy val settings = Settings(this)

  private val clearError: Runnable = () => {
    error.setVisibility(View.INVISIBLE)
  }

  private def verifyMatch() {
    if (pin == selectedPin) {
      val thePin = pin mkString ""
      PINHolderService.start(thePin)
      val km = new KeyManager(this, settings)
      val waitforit = for {
        key <- km.fetchCloudKey()
        lk1 <- km.localKey
      } yield {
        lk1.right foreach { localKey =>
          val pinKey = PINHolderService.keyFor(thePin)
          val newkey = KeyManager.encrypt(key, KeyManager.encrypt(
            pinKey, localKey.getEncoded))
          settings.set(Settings.LOCAL_KEY, newkey)
          settings.set(Settings.NEEDS_PIN, true)
          settings.set(Settings.PIN_TIMESTAMP, System.currentTimeMillis)
          FingerprintManager(this, settings).registerPin(thePin)
          settings.set(Settings.PIN_VERIFIER,
            KeyManager.encrypt(key, KeyManager.encrypt(pinKey,
              PINHolderService.PIN_VERIFIER)))
          setResult(Activity.RESULT_OK)
          finish()
        }
        lk1.left foreach { ex =>
          throw new IllegalStateException("Local key is not available: " + ex)
        }
      }

      waitforit.onFailureMain { case e =>
          Toast.makeText(this, "Failed to save new PIN: " + e.getMessage, Toast.LENGTH_LONG).show()
          ACRA.getErrorReporter.handleSilentException(e)
      }
    }
    else {
      error.setVisibility(View.VISIBLE)
      error.setText(R.string.try_again)
      pinEntry.setText("")
      pin = Seq.empty
      UiBus.handler.removeCallbacks(clearError)
      UiBus.handler.postDelayed(clearError, 1000)
    }
  }

  private def validatePin() {
    clearError.run()
    pinEntry.setText(pin mkString "")
    if (selectedPin.size > 0) {
      ok.setEnabled(true)
      error.setVisibility(View.INVISIBLE)
    } else if (pin.size < 4 && pin.size > 0) {
      error.setVisibility(View.VISIBLE)
      error.setText(R.string.pin_at_least_4)
      ok.setEnabled(false)
    } else {
      error.setVisibility(View.INVISIBLE)
      if (pin.size > 0)
        ok.setEnabled(true)
    }
  }

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setTitle(getTitle + getString(R.string.setup_pin_subtitle))
    setContentView(R.layout.pin_setup)

    val onClick = { view: View =>
      view.getId match {
        case R.id.pin_9    => pin :+= "9"
          validatePin()
        case R.id.pin_8    => pin :+= "8"
          validatePin()
        case R.id.pin_7    => pin :+= "7"
          validatePin()
        case R.id.pin_6    => pin :+= "6"
          validatePin()
        case R.id.pin_5    => pin :+= "5"
          validatePin()
        case R.id.pin_4    => pin :+= "4"
          validatePin()
        case R.id.pin_3    => pin :+= "3"
          validatePin()
        case R.id.pin_2    => pin :+= "2"
          validatePin()
        case R.id.pin_1    => pin :+= "1"
          validatePin()
        case R.id.pin_0    => pin :+= "0"
          validatePin()
        case R.id.pin_ok   =>
          if (selectedPin.size > 0) {
            verifyMatch()
          } else {
            selectedPin = pin
            pin = Seq.empty
            ok.setEnabled(false)
            findView(TR.pin_prompt).setText(R.string.confirm_pin)
            pinEntry.setText("")
          }
        case R.id.pin_back =>
          pin = pin.dropRight(1)
          validatePin()
      }
    }

    Seq(R.id.pin_9, R.id.pin_8, R.id.pin_7,
      R.id.pin_6, R.id.pin_5, R.id.pin_4,
      R.id.pin_3, R.id.pin_2, R.id.pin_1,
      R.id.pin_0, R.id.pin_ok, R.id.pin_back) foreach {
      findViewById(_).onClick(onClick)
    }
  }
}

package com.pointlessapps.tvremote_client.viewModels

import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.AndroidViewModel
import com.pointlessapps.tvremote_client.databinding.FragmentDevicePairingBinding
import com.pointlessapps.tvremote_client.models.DeviceWrapper

class ViewModelDevicePairing(
    activity: AppCompatActivity,
    private val root: FragmentDevicePairingBinding,
    private val deviceWrapper: DeviceWrapper
) : AndroidViewModel(activity.application) {

    private val context = activity.applicationContext
    private var secret = ""

    var onPopBackStackListener: (() -> Unit)? = null
    var onDispatchKeyEventListener: ((KeyEvent) -> Boolean)? = null

    fun setKeyboard() {
        arrayOf(
            "0", "1", "2", "3",
            "4", "5", "6", "7",
            "8", "9", "A", "B",
            "C", "D", "E", "F"
        ).forEach { symbol ->
            val id = context.resources.getIdentifier("button${symbol}", "id", context.packageName)
            root.root.findViewById<View>(id).setOnClickListener {
                secret += symbol
                refreshSecret()
            }
        }

        root.buttonClear.setOnClickListener {
            secret = ""
            refreshSecret()
        }
    }

    private fun refreshSecret() {
        (1..4).forEach {
            val id = context.resources.getIdentifier(
                "textSecret${it}",
                "id",
                context.packageName
            )
            root.root.findViewById<AppCompatTextView>(id).text =
                if (secret.length >= it) secret[it - 1].toString() else ""
        }

        if (secret.length == 4) {
            deviceWrapper.device!!.setPairingSecret(secret)
            onPopBackStackListener?.invoke()
        }
    }

    fun setDispatchKeyEventListener() {
        onDispatchKeyEventListener = lambda@{
            if (it.keyCode == KeyEvent.KEYCODE_BACK) {
                deviceWrapper.device!!.disconnect()
                onPopBackStackListener?.invoke()

                return@lambda true
            }
            return@lambda false
        }
    }
}

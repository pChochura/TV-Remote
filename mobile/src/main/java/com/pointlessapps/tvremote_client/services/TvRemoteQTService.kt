package com.pointlessapps.tvremote_client.services

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.requests.CancellableRequest
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.pointlessapps.tvremote_client.R
import com.pointlessapps.tvremote_client.utils.NetworkUtils
import com.pointlessapps.tvremote_client.utils.loadDeviceInfo
import com.pointlessapps.tvremote_client.utils.string
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TvRemoteQTService : TileService() {

    private val requests = mutableListOf<CancellableRequest>()
    private var forceLoadingState = false

    override fun onCreate() {
        NetworkUtils.registerNetworkChangeListener(applicationContext) { available ->
            when (available) {
                false -> setState(getString(R.string.no_internet), Tile.STATE_UNAVAILABLE)
                else -> refreshState()
            }
        }
    }

    override fun onClick() {
        if (!loadDevice()) return

        runCatching {
            forceLoadingState = true
            setState(getString(R.string.loading), Tile.STATE_UNAVAILABLE)
            requests.all { it.cancel() }
            requests.add("/power".httpPost().string {
                forceLoadingState = false
                this@TvRemoteQTService.setState(it)
            })
        }.exceptionOrNull()?.also {
            Log.e("LOG!", "onClick exception: ", it)
        }
    }

    private fun loadDevice(): Boolean {
        val deviceUri = applicationContext.loadDeviceInfo()?.uri
        if (deviceUri == null) {
            setState(getString(R.string.no_devices), Tile.STATE_UNAVAILABLE)
            return false
        }

        FuelManager.instance.basePath = "http://${deviceUri.host}:8080"
        return true
    }

    override fun onTileAdded() = onStartListening()

    override fun onStartListening() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            qsTile.subtitle = applicationContext.resources.getString(R.string.toggle_tv_power)
        }
        refreshState()
    }

    private fun refreshState() {
        if (!loadDevice()) {
            setState(getString(R.string.unknown), Tile.STATE_UNAVAILABLE)
            return
        }

        GlobalScope.launch(Dispatchers.IO) {
            runCatching {
                setState(getString(R.string.loading), Tile.STATE_UNAVAILABLE)
                requests.add("/power".httpGet().string(this@TvRemoteQTService::setState))
            }.exceptionOrNull()?.also {
                Log.e("LOG!", "refreshState exception: ", it)
            }
        }
    }

    private fun setState(state: String?) {
        if (forceLoadingState) return

        when (state) {
            "true", "on" -> setState(
                getString(R.string.its_on),
                Tile.STATE_ACTIVE
            )
            "false", "off" -> setState(
                getString(R.string.its_off),
                Tile.STATE_INACTIVE
            )
            else -> setState(getString(R.string.unknown), Tile.STATE_INACTIVE)
        }
    }

    private fun setState(label: String, state: Int? = null) {
        runCatching {
            qsTile.also {
                it?.label = label
                state?.apply { it?.state = this }
                it?.updateTile()
            }
        }.exceptionOrNull()?.also {
            Log.e("LOG!", "setState exception: ", it)
        }
    }
}
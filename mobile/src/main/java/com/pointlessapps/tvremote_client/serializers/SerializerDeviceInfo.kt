package com.pointlessapps.tvremote_client.serializers

import android.net.Uri
import com.google.android.tv.support.remote.discovery.DeviceInfo
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object SerializerDeviceInfo : KSerializer<DeviceInfo> {
	override val descriptor: SerialDescriptor =
		PrimitiveSerialDescriptor("Fruit", PrimitiveKind.STRING)

	override fun serialize(encoder: Encoder, value: DeviceInfo) =
		encoder.encodeString(value.uri.toString())

	override fun deserialize(decoder: Decoder): DeviceInfo =
		DeviceInfo.fromUri(Uri.parse(decoder.decodeString()))
}
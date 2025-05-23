package app.myzel394.alibi.ui.utils

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log

val ALLOWED_MICROPHONE_TYPES =
    setOf(
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
        AudioDeviceInfo.TYPE_USB_DEVICE,
        AudioDeviceInfo.TYPE_USB_ACCESSORY,
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
        AudioDeviceInfo.TYPE_IP,
        AudioDeviceInfo.TYPE_DOCK,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            AudioDeviceInfo.TYPE_DOCK_ANALOG
        } else {
        },
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AudioDeviceInfo.TYPE_BLE_HEADSET
        } else {
        },
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AudioDeviceInfo.TYPE_REMOTE_SUBMIX
        } else {
        },
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AudioDeviceInfo.TYPE_USB_HEADSET
        } else {
        },
    )

data class MicrophoneInfo(
    val deviceInfo: AudioDeviceInfo,
) {
    val name: String
        get() = deviceInfo.productName.toString()

    val type: MicrophoneType
        get() = when (deviceInfo.type) {
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> MicrophoneType.BLUETOOTH  // 蓝牙sco
            AudioDeviceInfo.TYPE_WIRED_HEADSET -> MicrophoneType.WIRED      // 有线耳机
            AudioDeviceInfo.TYPE_BUILTIN_MIC -> MicrophoneType.PHONE        // 内置麦克风
            else -> MicrophoneType.OTHER
        }

    companion object {
        fun fromDeviceInfo(deviceInfo: AudioDeviceInfo): MicrophoneInfo {
            return MicrophoneInfo(deviceInfo)
        }

        fun fetchDeviceMicrophones(context: Context): List<MicrophoneInfo> {
            return try {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE)!! as AudioManager
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    audioManager.availableCommunicationDevices.map(::fromDeviceInfo)
                } else {
                    audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS).map(::fromDeviceInfo)
                }
            } catch (error: Exception) {
                Log.getStackTraceString(error)

                emptyList()
            }
        }

        /// Filter microphones to only show normal ones
        fun filterMicrophones(microphones: List<MicrophoneInfo>): List<MicrophoneInfo> {
            return microphones.filter {
                it.deviceInfo.isSource && (
                        ALLOWED_MICROPHONE_TYPES.contains(it.deviceInfo.type) ||
                                // `type` doesn't seem to be reliably as its sometimes -2147483644 even
                                // for valid microphones
                                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                                        it.deviceInfo.type == -2147483644 &&
                                        BluetoothAdapter.checkBluetoothAddress(it.deviceInfo.address) &&
                                        it.deviceInfo.productName.isNotBlank()
                                        )
                        )
            }
        }
    }


    enum class MicrophoneType {
        BLUETOOTH,
        WIRED,
        PHONE,
        OTHER,
    }
}
package sc.hwd.sofill.interfaces.sofill.view

import android.net.Network
import android.net.NetworkCapabilities

/**
 * 网络状态回调接口
 * 定义网络状态变化的监听方法
 */
interface INetworkCallback {

    /**
     * 网络不可用
     */
    fun onNetworkUnavailable()

    /**
     * 网络可用
     * @param network 网络对象
     */
    fun onNetworkAvailable(network: Network)

    /**
     * 网络丢失
     * @param network 网络对象
     */
    fun onNetworkLost(network: Network)

    /**
     * 网络能力变化
     * @param network 网络对象
     * @param networkCapabilities 网络能力
     */
    fun onNetworkCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities)
}
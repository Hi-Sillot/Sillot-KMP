/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/23 下午6:38
 * updated: 2024/8/23 下午6:38
 */

package sc.hwd.sofill.android.net

import android.net.NetworkCapabilities
import android.net.NetworkRequest

/**
 * 表示网络能够连接到运营商的多媒体信息服务中心（MMSC），用于发送和接收彩信。
 */
val networkRequestMMS = NetworkRequest.Builder()
    .addCapability(NetworkCapabilities.NET_CAPABILITY_MMS)

/**
 * 表示网络能够连接到运营商的辅助全球卫星定位系统（SUPL）服务器，用于获取 GPS 信息。
 */
val networkRequestSUPL = NetworkRequest.Builder()
    .addCapability(NetworkCapabilities.NET_CAPABILITY_SUPL)

/**
 * 表示网络能够连接到运营商的拨号网络（DUN）或网络共享网关。
 */
val networkRequestDUN = NetworkRequest.Builder()
    .addCapability(NetworkCapabilities.NET_CAPABILITY_DUN)

/**
 * 表示网络能够连接到 Wi-Fi 直连对等设备。(无法验证是否设备热点）
 */
val networkRequestWifiP2P = NetworkRequest.Builder()
    .addCapability(NetworkCapabilities.NET_CAPABILITY_WIFI_P2P)

/**
 * 表示该网络能够连接到互联网。(无法验证网络是否可连接)
 */
val networkRequestInternet = NetworkRequest.Builder()
    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

/**
 * 表示该网络可供一般使用，应用程序可以在该网络上进行通信。(无法验证网络是否可连接)
 */
val networkRequestNotRestricted = NetworkRequest.Builder()
    .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)

/**
 * 表示用户对该网络有隐含的信任，例如是 SIM 卡选择的运营商网络、插入的以太网、配对的蓝牙设备或用户请求连接的 Wi-Fi。
 */
val networkRequestTrusted = NetworkRequest.Builder()
    .addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)

/**
 * 表示该网络不是虚拟专用网络（VPN）。
 */
val networkRequestNotVPN = NetworkRequest.Builder()
    .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)

/**
 * 表示该网络的连接性已成功验证
 */
val networkRequestValidated = NetworkRequest.Builder()
    .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

/**
 * 表示该网络不是漫游网络。
 */
val networkRequestNotRoaming = NetworkRequest.Builder()
    .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING)

/**
 * 表示该网络可供应用程序使用，而不是在后台保持以促进快速网络切换的网络。
 */
val networkRequestForeground = NetworkRequest.Builder()
    .addCapability(NetworkCapabilities.NET_CAPABILITY_FOREGROUND)

/**
 * 表示该网络不拥塞，当网络拥塞时，应用程序应推迟可以在以后进行的网络流量，如上传分析数据。
 */
val networkRequestNotCongested = NetworkRequest.Builder()
    .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_CONGESTED)

/**
 * 表示该网络使用蜂窝网络传输。
 */
val networkRequestCellular = NetworkRequest.Builder()
    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)

/**
 * 表示该网络使用 Wi-Fi 传输。
 */
val networkRequestWifi = NetworkRequest.Builder()
    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)

/**
 * 表示该网络使用蓝牙传输。
 */
val networkRequestBluetooth = NetworkRequest.Builder()
    .addTransportType(NetworkCapabilities.TRANSPORT_BLUETOOTH)

/**
 * 表示该网络使用以太网传输。
 */
val networkRequestEthernet = NetworkRequest.Builder()
    .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)

/**
 * 表示该网络使用虚拟专用网络（VPN）传输。
 */
val networkRequestVPN = NetworkRequest.Builder()
    .addTransportType(NetworkCapabilities.TRANSPORT_VPN)

/**
 * 表示该网络使用 Wi-Fi Aware 传输。(Wi-Fi 设备感知周围的设备、应用与信息)
 */
val networkRequestWifiAware = NetworkRequest.Builder()
    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)

/**
 * 表示该网络使用 USB 传输。
 */
val networkRequestUSB = NetworkRequest.Builder()
    .addTransportType(NetworkCapabilities.TRANSPORT_USB)
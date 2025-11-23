package sc.hwd.sillot.shared2.platform

expect object Platform {
    val isAndroid: Boolean
    val isDesktop: Boolean
    val isWindows: Boolean
}
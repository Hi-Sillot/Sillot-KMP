package sc.hwd.sofill.base

import android.content.Context
import android.os.Build
import android.util.Log
import com.tencent.shiply.processor.DiffPkgHandler
import com.tencent.shiply.processor.ZipDataBasePkgFile
import com.tencent.upgrade.bean.UpgradeConfig
import com.tencent.upgrade.bean.UpgradeStrategy
import com.tencent.upgrade.core.DefaultUpgradeStrategyRequestCallback
import com.tencent.upgrade.core.UpgradeManager
import sc.hwd.sofill.S
import sc.hwd.sofill.Us.getAbi

fun shiply_uprade(CHANNEL: String? = "unknown", applicationContext: Context) {
    val TAG = "shiply_uprade"
    Log.d(TAG, "shiply_uprade... CHANNEL=$CHANNEL")
//        val map = HashMap<String, String>()
//        CHANNEL?.let {
//            map["CHANNEL"] = it
//        }

    val builder = UpgradeConfig.Builder()
        .internalInitMMKVForRDelivery(false)// 是否由sdk内部初始化mmkv(调用MMKV.initialize()),业务方如果已经初始化过mmkv可以设置为false
        .systemVersion("${Build.VERSION.SDK_INT}")// 用户手机系统版本，用于匹配shiply前端创建任务时设置的系统版本下发条件
//            .customParams(map)// 自定义属性键值对，用于匹配shiply前端创建任务时设置的自定义下发条件
        .diffPkgHandler(DiffPkgHandler()) // 差量APK处理器，负责差量包下载与合成，业务方如果不使用差量包能力，可以不用设置
        // 差量包基准包文件获取方式，以下二选一
//        .basePkgFileForDiffUpgrade(OriginBasePkgFile()) // 表示基于原始文件生成差量包，不支持渠道包差量
        .basePkgFileForDiffUpgrade(ZipDataBasePkgFile()) // 表示基于渠道包文件的纯数据区域生成差量包，支持渠道包差量，业务方的渠道号是写在V2签名区域时请使用这种方式
    val config = builder.appId(S.shiply_app_id).appKey(S.shiply_app_key).build()
    try {
        UpgradeManager.getInstance().apply {
            init(applicationContext, config)
            /**
             * 检查更新
             *
             * @param forceRequestRemoteStrategy 是否强制向server请求更新策略，为 false 时会抛出 RuntimeException: Missing type parameter. 原因未知
             * @param customParams 网络请求自定义参数,forceRequestRemoteStrategy 为 true 时生效
             * @param callback 更新策略回调接口,必填项，为空时会抛出 IllegalArgumentException
             */
            checkUpgrade(true, emptyMap<String, String>(), object : DefaultUpgradeStrategyRequestCallback(){
                override fun onFail(p0: Int, p1: String?) {
                    super.onFail(p0, p1)
                    Log.e(TAG, "checkUpgrade fail, code=$p0, msg=$p1")
                }

                override fun onReceiveStrategy(p0: UpgradeStrategy?) {
                    val assets1 = p0?.extra?.get("CHANNEL") == CHANNEL
                    val assets2 = p0?.extra?.get("ABI") == getAbi()
                    Log.d(TAG, "onReceiveStrategy... assets1=$assets1, assets2=$assets2, CHANEL=$CHANNEL, ABI=${getAbi()}")
                    if (assets1 && assets2) super.onReceiveStrategy(p0)
                    Log.d(TAG, "onReceiveStrategy... extra: ${p0?.extra} strategy=$p0")
                }

                override fun onReceivedNoStrategy() {
                    super.onReceivedNoStrategy()
                    Log.d(TAG, "onReceivedNoStrategy...")
                }

                override fun showUpgradeRedDot() {
                    super.showUpgradeRedDot()
                    Log.d(TAG, "showUpgradeRedDot...")
                }

                override fun tryPopUpgradeDialog(p0: UpgradeStrategy?) {
                    super.tryPopUpgradeDialog(p0)
                    Log.d(TAG, "tryPopUpgradeDialog... extra: ${p0?.extra} strategy=$p0")
                }
            })
        }
    } catch (e: Exception) {
        Log.e(TAG, "init upgrade error", e)
    }
}
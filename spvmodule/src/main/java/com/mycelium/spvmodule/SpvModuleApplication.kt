package com.mycelium.spvmodule

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.StrictMode
import android.preference.PreferenceManager
import android.util.Log
import com.mycelium.modularizationtools.CommunicationManager
import com.mycelium.modularizationtools.ModuleMessageReceiver
import com.mycelium.spvmodule.guava.Bip44AccountIdleService
import org.bitcoinj.core.*
import org.bitcoinj.core.Context.enableStrictMode
import org.bitcoinj.core.Context.propagate
import org.bitcoinj.crypto.LinuxSecureRandom
import org.bitcoinj.utils.Threading
import org.bitcoinj.wallet.SendRequest

class SpvModuleApplication : Application(), ModuleMessageReceiver {
    var configuration: Configuration? = null
        private set
    private var activityManager: ActivityManager? = null

    private var spvServiceIntent: Intent? = null
    private var blockchainServiceCancelCoinsReceivedIntent: Intent? = null
    private var blockchainServiceResetBlockchainIntent: Intent? = null
    var packageInfo: PackageInfo? = null
        private set
    private val spvMessageReceiver: SpvMessageReceiver = SpvMessageReceiver(this)

    override fun onMessage(callingPackageName: String, intent: Intent) = spvMessageReceiver.onMessage(callingPackageName, intent)

    override fun onCreate() {
        // TODO: make sure the following debug log statements get removed by proguard
        // Log.d(LOG_TAG, "this should not be visible in release mode")
        // Log.e(LOG_TAG, "this should always be visible")
        // Log.d(LOG_TAG, "This should not happen in release mode" + INSTANCE!!)
        INSTANCE = if (INSTANCE != null && INSTANCE !== this) {
            throw Error("Application was instanciated more than once?")
        } else {
            this
        }

        LinuxSecureRandom() // init proper random number generator

        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().permitDiskReads().permitDiskWrites().penaltyLog().build())

        Threading.throwOnLockCycles()
        enableStrictMode()
        propagate(Constants.CONTEXT)

        Log.i(LOG_TAG, "=== starting app using configuration: ${if (Constants.TEST) "test" else "prod"}, ${Constants.NETWORK_PARAMETERS.id}")

        super.onCreate()

        packageInfo = packageInfoFromContext(this)

        configuration = Configuration(PreferenceManager.getDefaultSharedPreferences(this))
        activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        spvServiceIntent = Intent(this, SpvService::class.java)
        blockchainServiceCancelCoinsReceivedIntent = Intent(SpvService.ACTION_CANCEL_COINS_RECEIVED, null, this,
                SpvService::class.java)

        //TODO Never called, investigate if still useful.
        blockchainServiceResetBlockchainIntent = Intent(SpvService.ACTION_ADD_ACCOUNT, null, this, SpvService::class.java)
        Bip44AccountIdleService().startAsync()
        CommunicationManager.getInstance(this).requestPair(getMbwModuleName())
    }

    fun stopBlockchainService() {
        stopService(spvServiceIntent)
    }

    @Synchronized
    fun addWalletAccountWithExtendedKey(bip39Passphrase: List<String>, creationTimeSeconds: Long,
                                        accountIndex: Int) {
        Log.d(LOG_TAG, "addWalletAccountWithExtendedKey, accountIndex = $accountIndex, " +
                "doesWalletAccountExist for accountIndex ${accountIndex + 3} " +
                "is ${doesWalletAccountExist(accountIndex + 3)}.")
        if(doesWalletAccountExist(accountIndex + 3)) {
            return
        }

        Bip44AccountIdleService.getInstance().addWalletAccount(bip39Passphrase, creationTimeSeconds, accountIndex)
        restartBip44AccountIdleService()
    }

    internal fun restartBip44AccountIdleService() {
        Log.d(LOG_TAG, "restartBip44AccountIdleService, stopAsync")
        try {
            val service = Bip44AccountIdleService.getInstance().stopAsync()
            Log.d(LOG_TAG, "restartBip44AccountIdleService, awaitTerminated")
            service.awaitTerminated()
        } catch (e : Throwable) {
            Log.e(LOG_TAG, e.localizedMessage, e)
        } finally {
            Log.d(LOG_TAG, "restartBip44AccountIdleService, startAsync")
            Bip44AccountIdleService().startAsync()
            Log.d(LOG_TAG, "restartBip44AccountIdleService, DONE")
        }
    }

    fun broadcastTransaction(tx: Transaction, accountIndex: Int) {
        Bip44AccountIdleService.getInstance().broadcastTransaction(tx, accountIndex)
    }

    fun broadcastTransaction(sendRequest: SendRequest, accountIndex: Int) {
        Bip44AccountIdleService.getInstance().broadcastTransaction(sendRequest, accountIndex)
    }

    fun sendTransactions(accountIndex: Int) {
        Bip44AccountIdleService.getInstance().sendTransactions(accountIndex)
    }

    fun launchBlockchainScanIfNecessary() {
        Bip44AccountIdleService.getInstance().checkImpediments()
    }

    fun maxConnectedPeers(): Int =
            if (activityManager!!.memoryClass <= Constants.MEMORY_CLASS_LOWEND) {
                4
            } else {
                6
            }

    internal fun doesWalletAccountExist(accountIndex: Int): Boolean =
            Bip44AccountIdleService.getInstance().doesWalletAccountExist(accountIndex)

    companion object {
        private var INSTANCE: SpvModuleApplication? = null

        fun getApplication(): SpvModuleApplication = INSTANCE!!

        fun packageInfoFromContext(context: Context): PackageInfo {
            try {
                return context.packageManager.getPackageInfo(context.packageName, 0)
            } catch (x: PackageManager.NameNotFoundException) {
                throw RuntimeException(x)
            }
        }

        private val LOG_TAG: String? = this::class.java.simpleName

        fun getMbwModuleName(): String = when (BuildConfig.APPLICATION_ID) {
            "com.mycelium.spvmodule_testrelease" -> "com.mycelium.testnetwallet"
            "com.mycelium.spvmodule.test" -> "com.mycelium.devwallet_spore"
            else -> throw RuntimeException("No mbw module defined for BuildConfig " + BuildConfig.APPLICATION_ID)
        }

        fun sendMbw(intent: Intent) {
            CommunicationManager.getInstance(getApplication()).send(getMbwModuleName(), intent)
        }

        fun doesWalletAccountExist(accountIndex: Int): Boolean =
                INSTANCE!!.doesWalletAccountExist(accountIndex)
    }
}
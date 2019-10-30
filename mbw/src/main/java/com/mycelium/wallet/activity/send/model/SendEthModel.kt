package com.mycelium.wallet.activity.send.model

import android.app.Application
import android.content.Intent
import com.mycelium.wallet.MinerFee
import com.mycelium.wallet.Utils
import com.mycelium.wallet.activity.send.view.SelectableRecyclerView
import com.mycelium.wapi.wallet.WalletAccount
import com.mycelium.wapi.wallet.coins.Value

class SendEthModel(context: Application,
                   account: WalletAccount<*>,
                   intent: Intent)
    : SendCoinsModel(context, account, intent) {
    override fun handlePaymentRequest(toSend: Value): TransactionStatus {
        throw IllegalStateException("Ethereum does not support payment requests")
    }

    override fun getFeeLvlItems(): List<FeeLvlItem> {
        return MinerFee.values()
                .map { fee ->
                    val blocks = when (fee) {
                        MinerFee.LOWPRIO -> 20
                        MinerFee.ECONOMIC -> 10
                        MinerFee.NORMAL -> 3
                        MinerFee.PRIORITY -> 1
                    }
                    val duration = Utils.formatBlockcountAsApproxDuration(mbwManager, blocks)
                    FeeLvlItem(fee, "~$duration", SelectableRecyclerView.Adapter.VIEW_TYPE_ITEM)
                }

    }
}
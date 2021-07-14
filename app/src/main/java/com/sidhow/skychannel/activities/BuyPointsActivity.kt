package com.sidhow.skychannel.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.*
import com.google.gson.Gson
import com.sidhow.skychannel.Adapter.SkuAdapter
import com.sidhow.skychannel.Utils
import com.sidhow.skychannel.constant.PrefKeys
import com.sidhow.skychannel.databinding.ActivityBuyPointsBinding
import com.sidhow.skychannel.interfaces.LikeListener
import com.sidhow.skychannel.interfaces.OnFetchSkuDetails
import com.sidhow.skychannel.model.SkuProducts
import com.sidhow.skychannel.model.User
import com.sidhow.skychannel.viewModel.CreditsViewModel
import com.tramsun.libs.prefcompat.Pref

class BuyPointsActivity : AppCompatActivity(), PurchasesUpdatedListener,
    SkuAdapter.OnItemClickListener {

    private lateinit var binding: ActivityBuyPointsBinding

    private lateinit var billingClient: BillingClient

    private lateinit var creditsViewModel: CreditsViewModel
    private val TAG = "BuyPointsActivity"
    private var adapter: SkuAdapter? = null

    private var skuDetail: List<SkuDetails> = ArrayList()
    private var skuProducts: List<SkuProducts> = ArrayList()

    private var coins: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuyPointsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Buy Coins"
        actionBar?.setDisplayHomeAsUpEnabled(true)

        coins = Pref.getString(PrefKeys.coins,"0").toInt()

        creditsViewModel = ViewModelProvider(this).get(CreditsViewModel::class.java)

        Utils.initpDialog(this, "Loading")
        Utils.showpDialog()
        binding.skuRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.skuRecyclerView.setHasFixedSize(true)

        creditsViewModel.fetchSkuDetails(object : OnFetchSkuDetails {
            override fun onFetchSkuDetails(status: Boolean, skuProduct: List<SkuProducts>) {
                if (status) {
                    Utils.hidepDialog()
                    skuProducts = skuProduct
                    setUpBillingClient(skuProducts)
                } else {
                    Utils.hidepDialog()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        adapter?.notifyDataSetChanged()
    }


    private fun setUpBillingClient(skuProducts: List<SkuProducts>) {
        billingClient = BillingClient.newBuilder(this)
                .setListener(this)
                .enablePendingPurchases()
                .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    initSKU(skuProducts)
                    Toast.makeText(this@BuyPointsActivity, "Connect", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this@BuyPointsActivity,
                        "Response: ${billingResult.responseCode}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onBillingServiceDisconnected() {
                Toast.makeText(this@BuyPointsActivity, "Disconnected", Toast.LENGTH_SHORT).show()
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })

    }

    private fun initSKU(skuProducts: List<SkuProducts>) {

        val creditsPurchase: MutableList<String> = java.util.ArrayList()
        for(sku in skuProducts){
            if(sku.type=="1") {
                creditsPurchase.add(sku.sku)
            }
        }

        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(creditsPurchase).setType(BillingClient.SkuType.INAPP)

        billingClient.querySkuDetailsAsync(
            params.build()
        ) { result, skuDetailsList ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                if (skuDetailsList!!.size > 0) {
                    skuDetail = skuDetailsList
                    Log.i(TAG,skuDetailsList.toString())
                    adapter = SkuAdapter(this, skuDetailsList, false,this)

                    runOnUiThread {
                        binding.skuRecyclerView.adapter = adapter
                    }
                    Log.i(TAG, "Response is ok")
                } else {
                    Log.i(TAG, "list is empty")
                }
            } else {
                Log.i(TAG, "Response: ${result.responseCode}")
            }
        }
    }

    fun initPurchaseFlow(details: SkuDetails) {
        Log.i("skudetails", details.toString())
        // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(details)
            .build()
        billingClient.launchBillingFlow(this, flowParams)
    }
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
            && purchases != null
        ) {
            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    // Acknowledge purchase and grant the item to the user
                    for (product in skuProducts) {
                        if (purchase.skus.equals(product.sku)) {
                            val newCoins = coins + product.coins.toInt()
                            creditsViewModel.updateCoins(newCoins.toString(),object :LikeListener{
                                override fun onLikeStarted() {

                                }

                                override fun onLikedSuccess() {

                                }

                                override fun onLikedFailed() {

                                }
                            })
                            acknowledgePurchaseConsume(
                                product.coins.toInt(),
                                purchase
                            )
                            break
                        }
                    }

//                    switch (purchase.getSku()) {
//                        case SMALL_PACK_ID:
//
//                            acknowledgePurchaseConsume(Variables.SMALL_PACK_CREDITS, purchase);
//
//                            break;
//                        case MEDIUM_PACK_ID:
//
//                            acknowledgePurchaseConsume(Variables.MEDIUM_PACK_CREDITS, purchase);
//                            break;
//                        case BIG_PACK_ID:
//
//                            acknowledgePurchaseConsume(Variables.BIG_PACK_CREDITS, purchase);
//
//                            break;
//                    }
//
                }
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Toast.makeText(this, "purchase canceled", Toast.LENGTH_SHORT).show()
        } else {
            // Handle any other error codes.
            Toast.makeText(this, "error_try_again_later", Toast.LENGTH_SHORT).show()
        }
    }
    private fun acknowledgePurchaseConsume(credits: Int, purchase: Purchase) {
        Toast.makeText(
            this@BuyPointsActivity,
            "Purchased Successfully $credits",
            Toast.LENGTH_LONG
        ).show()

        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        val consumeResponseListener =
            ConsumeResponseListener { billingResult, purchaseToken ->
                Log.i("billingResult", billingResult.debugMessage)
                Log.i("purchaseToken", purchaseToken)
            }
        billingClient.consumeAsync(consumeParams, consumeResponseListener)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onItemClick(item: SkuDetails) {

        initPurchaseFlow(item)
    }
}
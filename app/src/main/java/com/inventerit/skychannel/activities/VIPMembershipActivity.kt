package com.inventerit.skychannel.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.*
import com.inventerit.skychannel.Adapter.SkuAdapter
import com.inventerit.skychannel.Utils
import com.inventerit.skychannel.databinding.ActivityBuyPointsBinding
import com.inventerit.skychannel.databinding.ActivityMembershipBinding
import com.inventerit.skychannel.interfaces.OnFetchSkuDetails
import com.inventerit.skychannel.model.SkuProducts
import com.inventerit.skychannel.viewModel.CreditsViewModel

class VIPMembershipActivity : AppCompatActivity(), PurchasesUpdatedListener,
        SkuAdapter.OnItemClickListener {

    private lateinit var binding: ActivityMembershipBinding

    private lateinit var billingClient: BillingClient

    private lateinit var creditsViewModel: CreditsViewModel
    private val TAG = "VIPMembershipActivity"
    private var adapter: SkuAdapter? = null

    private var skuDetail: List<SkuDetails> = ArrayList()
    private var skuProducts: List<SkuProducts> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembershipBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Buy Coins"
        actionBar?.setDisplayHomeAsUpEnabled(true)

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
                    Toast.makeText(this@VIPMembershipActivity, "Connect", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                            this@VIPMembershipActivity,
                            "Response: ${billingResult.responseCode}",
                            Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onBillingServiceDisconnected() {
                Toast.makeText(this@VIPMembershipActivity, "Disconnected", Toast.LENGTH_SHORT).show()
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })

    }

    private fun initSKU(skuProducts: List<SkuProducts>) {

        val creditsPurchase: MutableList<String> = java.util.ArrayList()
        for(sku in skuProducts){
            creditsPurchase.add(sku.sku)
        }

        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(creditsPurchase).setType(BillingClient.SkuType.SUBS)

        billingClient.querySkuDetailsAsync(
                params.build()
        ) { result, skuDetailsList ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                if (skuDetailsList!!.size > 0) {
                    skuDetail = skuDetailsList
                    Log.i(TAG,skuDetailsList.toString())
                    adapter = SkuAdapter(this, skuDetailsList, this)

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
    fun acknowledgePurchaseConsume(credits: Int, purchase: Purchase) {
        Toast.makeText(
                this@VIPMembershipActivity,
                "Purchased Successfully ${credits}",
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
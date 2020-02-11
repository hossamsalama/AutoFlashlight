package com.etoutstore.flashnav;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PurchasesUpdatedListener {
    static CameraType camera;
    static Camera1 camera1;
    static Camera2 camera2;
    private boolean allowedPermission;
    Button autoButton;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private BillingClient billingClient;
    SkuDetails skuDetailsList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-8888502007911950/8414615174");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        setTitle("Auto Flashlight");
        checkAndroidVersion();
        // Create and set Android Fragment as default.
        Fragment androidFragment = new TorchFragment(camera);
        this.setDefaultFragment(androidFragment);

        // Click this button to display torch fragment.
        final Button torchButton = (Button) findViewById(R.id.torch);
        torchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment torchFragment = new TorchFragment(camera);
                replaceFragment(torchFragment);
                torchButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_torch, 0, 0);
                autoButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_flashdim, 0, 0);
            }
        });

        // Click this button to display auto fragment.
        autoButton = findViewById(R.id.auto);
        autoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                }
                Fragment sFragment = new SensivityFragment();
                replaceFragment(sFragment);
                autoButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_flash, 0, 0);
                torchButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_flash_dim, 0, 0);
            }
        });

        ImageButton settingButton = findViewById(R.id.settings);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creating the instance of PopupMenu
                //PopupMenu popup = new PopupMenu(MainActivity.this, settingButton);
                Context wrapper = new ContextThemeWrapper(v.getContext(), R.style.MenuStyle);
                final PopupMenu popup = new PopupMenu(wrapper, v);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.remove_ads:
                                setupBillingClient();
                                break;
                            case R.id.rate_app:
                                launchMarket();
                                break;
                            case R.id.about_app:
                                break;
                        }
                        return true;
                    }
                });

                popup.show(); //showing popup menu
            }
        }); //closing the setOnClickListener method
        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
    }

    private void setupBillingClient() {
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                    //Toast.makeText(MainActivity.this, "Success to connect Billing", Toast.LENGTH_SHORT).show();
                    loadAllSKUs();
                } else {
                    Toast.makeText(MainActivity.this, ""+ billingResult.getResponseCode(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Toast.makeText(MainActivity.this, "You are disconnect from Billing", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeAds(){
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetailsList)
                .build();
        billingClient.launchBillingFlow(this,billingFlowParams);
    }
    private void loadAllSKUs() {
        if (billingClient.isReady()){
            SkuDetailsParams params = SkuDetailsParams.newBuilder()
                    .setSkusList(Collections.singletonList("remove_ads"))
                    .setType(BillingClient.SkuType.INAPP)
                    .build();

            billingClient.querySkuDetailsAsync(params, new SkuDetailsResponseListener() {
                @Override
                public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> list) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                        for (SkuDetails sd: list) {
                            if (sd.getSku().equals("remove_ads")){
                                skuDetailsList = sd;
                                removeAds();
                            }
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Cannot query product", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(MainActivity.this, "Billing client not ready", Toast.LENGTH_SHORT).show();

        }
    }

    private void launchMarket() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, " unable to find market app", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Log.e("On Start", "initialize");
        //checkAndroidVersion();
        if (camera instanceof Camera2) {
            allowedPermission = askForPermission();
        } else {
            //set permission true as old version of android not need permission
            allowedPermission = true;
            camera1.startCamera();

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (camera instanceof Camera1){
            camera1.releaseCameraAndPreview();
        }

    }

    private void checkAndroidVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            camera2 = new Camera2(getApplicationContext());
            camera = camera2;
            allowedPermission = askForPermission();
        } else {
            allowedPermission = true;
            camera1 = new Camera1(getApplicationContext());
            camera = camera1;
        }
    }

    public boolean askForPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            camera2.initializeCamer2();
            allowedPermission = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 2000);

        }
        return allowedPermission;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                allowedPermission = true;
                camera2.initializeCamer2();
            }
        }
    }

    // This method is used to set the default fragment that will be shown.
    private void setDefaultFragment(Fragment defaultFragment)
    {
        this.replaceFragment(defaultFragment);
    }

    // Replace current Fragment with the destination Fragment.
    public void replaceFragment(Fragment destFragment)
    {
        // First get FragmentManager object.
        FragmentManager fragmentManager = this.getSupportFragmentManager();

        // Begin Fragment transaction.
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Replace the layout holder with the required Fragment object.
        fragmentTransaction.replace(R.id.fragment, destFragment);

        // Commit the Fragment replace action.
        fragmentTransaction.commit();
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> list) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null){
            Toast.makeText(this, "Purchase item: "+ list.size(), Toast.LENGTH_SHORT).show();
            for (Purchase purchase: list){
                acknowledgePurchase(purchase.getPurchaseToken());
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Toast.makeText(this, "Transaction Cancelled", Toast.LENGTH_SHORT).show();


        } else {
            // Handle any other error codes.
            Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show();
        }

    }

    private void acknowledgePurchase(String purchaseToken) {
        AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build();

        }
    }


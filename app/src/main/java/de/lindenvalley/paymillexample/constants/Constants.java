package de.lindenvalley.paymillexample.constants;

import com.paymill.android.service.PMService;

public class Constants {
    private static final boolean isBillingLive = false;

    public static final PMService.ServiceMode BILLING_MODE = isBillingLive ? PMService.ServiceMode.LIVE : PMService.ServiceMode.TEST;
    public static final String PAY_MILL_PUBLIC_KEY = isBillingLive ? "live_public_key" : "debug_public_key";
}

package de.lindenvalley.paymillexample.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import com.paymill.android.factory.PMPaymentMethod;
import com.paymill.android.listener.PMGenerateTokenListener;
import com.paymill.android.service.PMError;
import com.paymill.android.service.PMManager;
import com.paymill.android.service.PMService;
import de.lindenvalley.lib.OperationType;
import de.lindenvalley.lib.PaymentType;
import de.lindenvalley.lib.ProgressDialogFragment;
import de.lindenvalley.lib.Settings;
import de.lindenvalley.paymillexample.R;
import de.lindenvalley.paymillexample.fragments.CreditCardFragment;
import de.lindenvalley.paymillexample.fragments.DirectDebitFragment;
import de.lindenvalley.paymillexample.model.Factory;
import de.lindenvalley.paymillexample.model.Purchase;
import de.lindenvalley.paymillexample.model.Result;

public class PaymentActivity extends AppCompatActivity {
    private String mMerchantPublicKey;
    private Purchase mSubscription;
    private PaymentType mPaymentType;
    private Result.Type mResultType;
    private PMService.ServiceMode mServiceMode;
    private Settings mSettings;
    private String mOperationType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pm_payment);
        getArguments();

        PMManager.addListener(mGenerateTokenListener);

        if (mOperationType.equals(OperationType.CREDIT_CARD_TYPE)) {
            getSupportFragmentManager().beginTransaction().add(R.id.content_main,
                    CreditCardFragment.instance(mSettings, mSubscription), "CREDIT_FRAGMENT_TAG").commit();
        } else if (mOperationType.equals(OperationType.DIRECT_DEBIT_TYPE)) {
            getSupportFragmentManager().beginTransaction().add(R.id.content_main,
                    DirectDebitFragment.instance(mSettings, mSubscription), "DEBIT_FRAGMENT_TAG").commit();
        }
    }

    public void startRequest(PMPaymentMethod method) {
        new ProgressDialogFragment().show(getSupportFragmentManager(),
                ProgressDialogFragment.TAG);
        switch (mPaymentType) {
            case TOKEN:
                generateToken(method);
                break;
            case TOKEN_WITH_PARAMS:
                generateTokenWithParams(method);
                break;
            default:
                wrongParams();
        }
    }

    PMGenerateTokenListener mGenerateTokenListener = new PMGenerateTokenListener() {

        @Override
        public void onGenerateToken(String token) {
            success(token);
        }

        @Override
        public void onGenerateTokenFailed(PMError error) {
            failure(error);
        }

    };

    public void generateToken(PMPaymentMethod method) {
        PMManager.generateToken(PaymentActivity.this, method);
    }

    public void generateTokenWithParams(PMPaymentMethod method) {
        PMManager.generateToken(PaymentActivity.this, method, mServiceMode,
                mMerchantPublicKey);
    }

    private void success(String result) {
        Bundle bundle = new Bundle();
        bundle.putString(Factory.RETURN_RESULT, result);
        success(bundle);
    }

    private void success(Bundle bundle) {
        bundle.putParcelable(Factory.RETURN_TYPE, mResultType);
        Intent data = new Intent();
        data.putExtras(bundle);
        setResult(RESULT_OK, data);
        finish();
    }

    private void failure(PMError error) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Factory.RETURN_ERROR, error);
        bundle.putParcelable(Factory.RETURN_TYPE, mResultType);
        Intent data = new Intent();
        data.putExtras(bundle);
        setResult(Factory.RESULT_ERROR, data);
        finish();
    }

    void wrongParams() {
        finish();
    }

    private static PMService.ServiceMode getModeFromOrdinal(int ordinal) {
        for (PMService.ServiceMode mode : PMService.ServiceMode.values()) {
            if (mode.ordinal() == ordinal) {
                return mode;
            }
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        PMManager.removeListener(mGenerateTokenListener);
        super.onDestroy();
    }

    private void getArguments() {
        Bundle data = getIntent().getExtras();

        mPaymentType = data.getParcelable(Factory.ARGUMENT_TYPE);

        if (mPaymentType == null) {
            wrongParams();
        }

        if (mPaymentType == PaymentType.TOKEN
                || mPaymentType == PaymentType.TOKEN_WITH_PARAMS) {
            mResultType = Result.Type.TOKEN;
        } else if (mPaymentType == PaymentType.TRANSACTION
                || mPaymentType == PaymentType.PREAUTHORIZATION) {
            mResultType = Result.Type.TRANSACTION;
        } else {
            wrongParams();
        }

        if (mPaymentType == PaymentType.TOKEN_WITH_PARAMS) {
            mServiceMode = getModeFromOrdinal(data.getInt(Factory.ARGUMENT_MODE));
            if (mServiceMode == null) {
                wrongParams();
            }
            mMerchantPublicKey = data.getString(Factory.ARGUMENT_MERCHANT_PUBLIC_KEY);
            if (TextUtils.isEmpty(mMerchantPublicKey)) {
                wrongParams();
            }
        }

        mSettings = data.getParcelable(Factory.ARGUMENT_SETTINGS);
        if (mSettings == null) {
            wrongParams();
        }

        mOperationType = data.getString(Factory.ARGUMENT_OPERATION_TYPE);
        if (mOperationType == null) {
            wrongParams();
        }

        mSubscription = (Purchase) data.getSerializable(Factory.ARGUMENT_SUBSCRIPTION);
        if (mSubscription == null) {
            wrongParams();
        }

        Bundle pmSettingsBundle = new Bundle();
        pmSettingsBundle.putParcelable(Factory.ARGUMENT_SETTINGS, mSettings);
    }
}

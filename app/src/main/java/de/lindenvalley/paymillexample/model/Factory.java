package de.lindenvalley.paymillexample.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.paymill.android.api.Transaction;
import com.paymill.android.service.PMError;
import com.paymill.android.service.PMService;
import de.lindenvalley.lib.PaymentType;
import de.lindenvalley.lib.Settings;
import de.lindenvalley.paymillexample.activity.PaymentActivity;

public class Factory {

    public static final int REQUEST_CODE = 7281;
    public static final String ARGUMENT_SUBSCRIPTION = "subscription";
    public static final String ARGUMENT_TYPE = "type";
    public static final String ARGUMENT_SETTINGS = "settings";
    public static final String ARGUMENT_MERCHANT_PUBLIC_KEY = "merchantPublicKey";
    public static final String ARGUMENT_MODE = "mode";
    public static final String ARGUMENT_OPERATION_TYPE = "operationType";
    public static final String RETURN_RESULT = "result";
    public static final String RETURN_ERROR = "error";
    public static final String RETURN_TYPE = "returnType";
    public static final int RESULT_OK = Activity.RESULT_OK;
    public static final int RESULT_ERROR = -2;

    private static Intent getTokenIntent(Purchase product, String operationType,
                                         Context packageContext, Settings settings,
                                         PMService.ServiceMode mode, String merchantPublicKey) {
            Bundle data = new Bundle();
            data.putString(ARGUMENT_OPERATION_TYPE, operationType);
            data.putSerializable(ARGUMENT_SUBSCRIPTION, product);
            data.putString(ARGUMENT_MERCHANT_PUBLIC_KEY, merchantPublicKey);
            data.putInt(ARGUMENT_MODE, mode.ordinal());
            data.putParcelable(ARGUMENT_TYPE, PaymentType.TOKEN_WITH_PARAMS);
            data.putParcelable(ARGUMENT_SETTINGS, settings);
            Intent intent = new Intent(packageContext, PaymentActivity.class);
            intent.putExtras(data);
            return intent;
        }

        public static Intent getTokenIntent(Purchase product, String billingType, Context packageContext,
                                     final PMService.ServiceMode mode, final String merchantPublicKey) {
            return getTokenIntent(product, billingType, packageContext, new Settings(), mode,
                    merchantPublicKey);
        }

        public static Result getResultFrom(int requestCode, int resultCode,
                                    Intent data) {
            if (requestCode != REQUEST_CODE) {
                return null;
            }

            if (data == null) {
                return null;
            }

            Bundle results = data.getExtras();
            if (results == null) {
                return null;
            }
            Result.Type type = results.getParcelable(RETURN_TYPE);
            String token = null;
            Transaction transaction = null;
            if (type == Result.Type.TOKEN) {
                token = results.getString(RETURN_RESULT);
            } else {
                transaction = results.getParcelable(RETURN_RESULT);
            }
            PMError error = results.getParcelable(RETURN_ERROR);
            return new Result(transaction, token, type, error, resultCode);
        }
    }
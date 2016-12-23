package de.lindenvalley.paymillexample.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import de.lindenvalley.lib.OperationType;
import de.lindenvalley.paymillexample.R;
import de.lindenvalley.paymillexample.constants.Constants;
import de.lindenvalley.paymillexample.model.Factory;
import de.lindenvalley.paymillexample.model.Purchase;
import de.lindenvalley.paymillexample.model.Result;

public class MainActivity extends AppCompatActivity {
    private String mOperationType = "";

    private CheckBox mCardCheck;
    private CheckBox mDebitCheck;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUi();
    }

    private void initUi() {
        mCardCheck = (CheckBox) findViewById(R.id.checkBox1);
        mDebitCheck = (CheckBox) findViewById(R.id.checkBox2);

        mCardCheck.setOnCheckedChangeListener(mCardCheckedListener);
        mDebitCheck.setOnCheckedChangeListener(mDebitCheckedListener);

        findViewById(R.id.button).setOnClickListener(mListener);
    }

    View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            genPaymentToken();
        }
    };

    CompoundButton.OnCheckedChangeListener mCardCheckedListener
            = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton,
                                     boolean isChecked) {
            if (isChecked) {
                mDebitCheck.setChecked(false);
                mOperationType = OperationType.CREDIT_CARD_TYPE;
            }
        }
    };

    CompoundButton.OnCheckedChangeListener mDebitCheckedListener
            = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton,
                                     boolean isChecked) {
            if (isChecked) {
                mCardCheck.setChecked(false);
                mOperationType = OperationType.DIRECT_DEBIT_TYPE;
            }
        }
    };

    private void genPaymentToken() {
        if (mOperationType.isEmpty()) {
            onMessage("Choice Payment type");
            return;
        }

        Intent intent = Factory.getTokenIntent(
                new Purchase(1),
                mOperationType,
                this,
                Constants.BILLING_MODE,
                Constants.PAY_MILL_PUBLIC_KEY);

        startActivityForResult(intent, Factory.REQUEST_CODE);
    }

    private void onMessage(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Result result = Factory.getResultFrom(
                requestCode, resultCode, data);
        if (result != null && result.getResultToken() != null) {
            /** Calls after getting transaction token. Sending the token and
             *  subscription id on the server **/
            onMessage("Token: " + result.getResultToken());
        } else
            onMessage("An error has occurred");
    }
}

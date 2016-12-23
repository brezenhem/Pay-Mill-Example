package de.lindenvalley.paymillexample.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.paymill.android.factory.PMFactory;
import com.paymill.android.factory.PMPaymentMethod;
import java.util.Arrays;
import java.util.List;
import de.lindenvalley.lib.IbanBicValidator;
import de.lindenvalley.lib.Settings;
import de.lindenvalley.paymillexample.activity.PaymentActivity;
import de.lindenvalley.paymillexample.R;
import de.lindenvalley.paymillexample.model.Factory;
import de.lindenvalley.paymillexample.model.Purchase;
import de.lindenvalley.lib.CurrencyHelper;

public class DirectDebitFragment extends Fragment {
    private Button mTriggerButton;
    private EditText mAccountHolder;
    private EditText mAccountNumber;
    private EditText mBankNumber;
    private TextView mPriceValue;

    private Settings mSettings;
    private Purchase mSubscription;

    private static final List<Integer> SPACES_POSITIONS_DEFAULT = Arrays
            .asList(4, 8, 12, 16, 20, 24, 28, 32);
    private static final List<Integer> MARKERS_POSITIONS_DEFAULT = Arrays
            .asList(5, 10, 15, 20, 25, 30, 35, 40);
    private static final int MAX_IBAN_WITH_SPACES = 42;
    private static final int MAX_BIC = 11;

    public static DirectDebitFragment instance(Settings settings,
                                               Purchase response) {
        DirectDebitFragment fragment = new DirectDebitFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Factory.ARGUMENT_SETTINGS, settings);
        bundle.putSerializable(Factory.ARGUMENT_SUBSCRIPTION, response);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.pm_direct_debit_fragment_new, container, false);
        if (isAdded()) {
            getParcelable();
            initUi(root);
            setAmount();
            setSettings(mSettings, root);
        }
        return root;
    }

    private void getParcelable() {
        mSettings = getArguments().getParcelable(Factory.ARGUMENT_SETTINGS);
        mSubscription = (Purchase) getArguments().getSerializable(Factory.ARGUMENT_SUBSCRIPTION);
    }

    private void setAmount() {
        if (mSubscription == null) return;

        mPriceValue.setText("You are going to pay: "
                + CurrencyHelper.format(mSubscription.getPrice()));
    }

    private void initUi(View root) {
        mPriceValue = (TextView) root.findViewById(R.id.price_value);
        mAccountHolder = (EditText) root.findViewById(R.id.nameText);
        mAccountNumber = (EditText) root.findViewById(R.id.accountNumberText);
        mBankNumber = ((EditText) root.findViewById(R.id.bankCodeText));
        mTriggerButton = (Button) root.findViewById(R.id.elv_trigger_btn);
        mTriggerButton.setOnClickListener(mListener);

        //TODO for test
        mAccountHolder.setText("Alex Tabo");
        mAccountNumber.setText("DE93 1000 0000 0012 3456 78");
        mBankNumber.setText("BENEDEPPYYY");

        mAccountHolder.setInputType(InputType.TYPE_CLASS_TEXT);
        mAccountNumber.setInputType(InputType.TYPE_CLASS_TEXT);
        mBankNumber.setInputType(InputType.TYPE_CLASS_TEXT);

        mAccountHolder.setHint(getString(R.string.pm_dd_account_holder_label));
        mAccountNumber.setHint(getString(R.string.pm_dd_account_number_label));
        mBankNumber.setHint(getString(R.string.pm_dd_bank_code_label));
    }

    private void setSettings(Settings pmSettings, View root) {
        assert pmSettings != null;
        if (pmSettings.getAccountNumber() != null) {
            ((EditText) root.findViewById(R.id.accountNumberText))
                    .setText(pmSettings.getAccountNumber());
        }

        if (pmSettings.getBankNumber() != null) {
            ((EditText) root.findViewById(R.id.bankCodeText)).setText(pmSettings
                    .getBankNumber());
        }

        if (pmSettings.getAccountHolder() != null) {
            ((EditText) root.findViewById(R.id.nameText)).setText(pmSettings
                    .getAccountHolder());
        }
        InputFilter[] filtersIban = new InputFilter[1];
        filtersIban[0] = new InputFilter.LengthFilter(MAX_IBAN_WITH_SPACES);
        mAccountNumber.setFilters(filtersIban);
        InputFilter[] filtersBic = new InputFilter[1];
        filtersBic[0] = new InputFilter.LengthFilter(MAX_BIC);
        mBankNumber.setFilters(filtersBic);
    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (IbanBicValidator.validate(mAccountHolder, mAccountNumber, mBankNumber)) {
                PMPaymentMethod ibanBic = PMFactory.genIbanBicPayment(
                        mAccountHolder.getEditableText().toString(),
                        mAccountNumber.getEditableText().toString().replaceAll("\\s", ""),
                        mBankNumber.getEditableText().toString());
                mTriggerButton.setEnabled(false);
                ((PaymentActivity) getActivity()).startRequest(ibanBic);
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        mAccountNumber.addTextChangedListener(mIbanTextWatcher);
    }

    @Override
    public void onPause() {
        super.onPause();
        mAccountNumber.removeTextChangedListener(mIbanTextWatcher);
    }

    private TextWatcher mIbanTextWatcher = new TextWatcher() {
        int ibanLength = 0;
        boolean isDelete = false;

        @Override
        public void onTextChanged(CharSequence s, final int start, int before,
                                  int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            mAccountNumber.setTextColor(
                    ContextCompat.getColor(getActivity(), R.color.colorPrimary));
            String ibanNumber = mAccountNumber.getEditableText().toString();
            ibanNumber = showCCardWithIntervals(ibanNumber
                    .replaceAll("\\s", ""));

            isDelete = ibanLength >= ibanNumber.length();

            mAccountNumber.removeTextChangedListener(this);
            int start = getCCardMarkerPosition(
                    mAccountNumber.getSelectionStart(), isDelete);
            mAccountNumber.setText(ibanNumber);
            mAccountNumber.setSelection(start < ibanNumber.length() ? start
                    : ibanNumber.length());
            mAccountNumber.addTextChangedListener(this);

            ibanLength = ibanNumber.length();
        }

        int getCCardMarkerPosition(int oldPosition, boolean isDelete) {
            int newPosition = oldPosition;

            if (MARKERS_POSITIONS_DEFAULT.contains(oldPosition))
                if (isDelete)
                    return newPosition --;
                else
                    newPosition ++;
            return newPosition ++;
        }

        String showCCardWithIntervals(String noIntervalsString) {
            String result = "";
            int index = 1;
            for (char c : noIntervalsString.toCharArray()) {
                result += c;
                if (SPACES_POSITIONS_DEFAULT.contains(index))
                    result += " ";
                index++;
            }
            return result;
        }
    };
}

package de.lindenvalley.paymillexample.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import de.lindenvalley.lib.CardTypeParser;
import de.lindenvalley.lib.CreditCardValidator;
import de.lindenvalley.lib.Settings;
import de.lindenvalley.paymillexample.activity.PaymentActivity;
import de.lindenvalley.paymillexample.R;
import de.lindenvalley.paymillexample.model.Factory;
import de.lindenvalley.paymillexample.model.Purchase;
import de.lindenvalley.lib.CurrencyHelper;

public class CreditCardFragment extends Fragment {
	public static final int MAX_NEEDED_NUMBERS = 6;

	public static final int prefix = 2;
	public static final int centuryPrefix = 0;
	public static final String cardNumberSpace = "/";

	private CardTypeParser.CardType mCardType = CardTypeParser.CardType.YetUnknown;
	private String mFirstNumbers;
	private Settings mSettings;
	private Purchase mSubscription;

	private Button mTriggerButton;
	private EditText mName;
	private EditText mCreditCardNumber;
	private EditText mDateText;
	private EditText mVerification;
	private TextView mPriceValue;
	public static CreditCardFragment instance(Settings settings,
											  Purchase response) {
		CreditCardFragment fragment = new CreditCardFragment();
		Bundle bundle = new Bundle();
		bundle.putParcelable(Factory.ARGUMENT_SETTINGS, settings);
		bundle.putSerializable(Factory.ARGUMENT_SUBSCRIPTION, response);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 final Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.pm_credit_card_fragment_new, container, false);
		if (isAdded()) {
			getParcelable();
			initUi(root);
			setAmount();
			setSettings();
		}
		return root;
	}

	private void getParcelable() {
		mSettings = getArguments().getParcelable(Factory.ARGUMENT_SETTINGS);
		mSubscription = (Purchase) getArguments().getSerializable(Factory.ARGUMENT_SUBSCRIPTION);
	}

	private void initUi(View root) {
		mTriggerButton = (Button) root.findViewById(R.id.cc_trigger_btn);
		mTriggerButton.setOnClickListener(sendBtnListener);

		mPriceValue = (TextView) root.findViewById(R.id.price_value);
		mDateText = (EditText) root.findViewById(R.id.dateText);
		mCreditCardNumber = (EditText) root.findViewById(R.id.cardNumberText);
		mName = (EditText) root.findViewById(R.id.nameText);
		mVerification = (EditText) root.findViewById(R.id.verificationText);

		//TODO for test
		mCreditCardNumber.setText("4111/1111/1111/1111");

		InputFilter[] filters = new InputFilter[1];
		filters[0] = new InputFilter.LengthFilter(4);
		mVerification.setFilters(filters);

		mName.setInputType(InputType.TYPE_CLASS_TEXT);
		mCreditCardNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
		mDateText.setInputType(InputType.TYPE_CLASS_NUMBER);
		mVerification.setInputType(InputType.TYPE_CLASS_NUMBER);

		mName.setHint(getString(R.string.full_name));
		mCreditCardNumber.setHint(getString(R.string.card_number));
		mDateText.setHint(getString(R.string.valid));
		mVerification.setHint(getString(R.string.ccv));
	}

	private void setSettings() {
		if (mSettings.getAccountHolder() != null) {
			mName.setText(mSettings.getAccountHolder());
		}

		if (mSettings.getCardNumber() != null) {
			mCreditCardNumber.setText(mSettings.getCardNumber());
		}

		if (mSettings.getExpiryMonth() != null
				&& mSettings.getExpiryYear() != null) {
			mDateText.setText(mSettings.getExpiryMonth() + "/"
					+ mSettings.getExpiryYear());
		}

		if (mSettings.getVerification() != null) {
			mVerification.setText(mSettings.getVerification());
		}
	}

	private void setAmount() {
		if (mSubscription == null) return;

		mPriceValue.setText("You are going to pay: "
				+ CurrencyHelper.format(mSubscription.getPrice()));
	}

	View.OnClickListener sendBtnListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (isAdded())
				genCardPayment();
		}
	};

	private void genCardPayment() {
		if (mCreditCardNumber.getEditableText().length() > MAX_NEEDED_NUMBERS)
			mCardType = CardTypeParser.CardType
					.getCardType(mCreditCardNumber.getEditableText().toString(),
							mSettings.getAllowedCardTypes());

		if (CreditCardValidator.validate(getActivity(), mName,
				mCreditCardNumber,
				mDateText,
				mVerification, mCardType,
				mSettings.getAllowedCardTypes())) {

			if (mDateText.getText().toString().length() < 5) {
				mDateText.setError("Invalid field"); return;
			}

			String month = mDateText.getText().toString().substring(0, 2);
			String year = mDateText.getText().toString().substring(3, 5);

			PMPaymentMethod card = PMFactory.genCardPayment(
					mName.getEditableText().toString(),
					mCreditCardNumber.getEditableText().toString().replaceAll(cardNumberSpace, ""),
					month,
					Integer.toString(prefix) + Integer.toString(centuryPrefix) + year,
					mVerification.getText().toString());
			mTriggerButton.setEnabled(false);
			((PaymentActivity) getActivity()).startRequest(card);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mCreditCardNumber.addTextChangedListener(mCCTextWatcher);
		mDateText.addTextChangedListener(dateTextWatcher);
		mVerification.addTextChangedListener(mValidationTextWatcher);
		mName.addTextChangedListener(nameTextWatcher);

	}

	@Override
	public void onPause() {
		super.onPause();
		mCreditCardNumber.removeTextChangedListener(mCCTextWatcher);
		mDateText.removeTextChangedListener(dateTextWatcher);
		mVerification.removeTextChangedListener(mValidationTextWatcher);
		mName.removeTextChangedListener(nameTextWatcher);
	}

	private TextWatcher mCCTextWatcher = new TextWatcher() {
		int cardLength = 0;
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
			mCreditCardNumber.setError(null);
			mCreditCardNumber.setTextColor(getResources().getColor(
					R.color.colorEditText));
			String ccNumber = mCreditCardNumber.getEditableText().toString();
			ccNumber = showCCardWithIntervals(ccNumber.replaceAll(cardNumberSpace, ""),
					mCardType);
			// we use isDelete in order to know if the cursor is moving forward
			// or backwards
			isDelete = cardLength >= ccNumber.length();
			// We save the last result numbers, to avoid unnecessary checks
			String firstNumbersSaved = "";

			firstNumbersSaved = ccNumber.substring(0,
					Math.min(MAX_NEEDED_NUMBERS + 1, ccNumber.length()))
					.replaceAll(cardNumberSpace, "");

			// decode card type and set filter to the edit text
			if (mFirstNumbers == null || !mFirstNumbers.equals(firstNumbersSaved)) {
				mFirstNumbers = firstNumbersSaved;
				mCardType = CardTypeParser.CardType.getCardType(mFirstNumbers,
						mSettings.getAllowedCardTypes());
				int cardNumberSize = mCardType.getMaxLength()
						+ mCardType.getNumberOfIntervals();
				mCreditCardNumber.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
								cardNumberSize) });
			}

			// enough to do this when we detect the card type?
			CreditCardValidator.isValidCardType(getActivity(),
					mCreditCardNumber, mCardType);

			int ccMaxLen = mCardType.getMaxLength()
					+ mCardType.getNumberOfIntervals();
			if (ccNumber.length() > ccMaxLen) {
				ccNumber = ccNumber.substring(0, ccMaxLen);
			}

			// set text and cursor position:
			// should remove the listener to change the text and set it again at
			// the right position
			mCreditCardNumber.removeTextChangedListener(this);
			int start = getCCardMarkerPosition(
					mCreditCardNumber.getSelectionStart(), isDelete, mCardType);
			mCreditCardNumber.setText(ccNumber);
			mCreditCardNumber.setSelection(start < ccNumber.length() ? start
					: ccNumber.length());
			mCreditCardNumber.addTextChangedListener(this);

			// if the card is valid move to next field
			if (mCardType != CardTypeParser.CardType.Invalid
					&& ccNumber.length() == mCardType.getMaxLength()
							+ mCardType.getNumberOfIntervals()) {
				if (CreditCardValidator.validateCreditCardNumber(getActivity(),
						mCreditCardNumber, mCardType,
						mSettings.getAllowedCardTypes())) {
					mDateText.requestFocus();
				}
			}

			cardLength = ccNumber.length();

			if (mCardType != CardTypeParser.CardType.Invalid) {

				InputFilter[] filters = new InputFilter[1];
				filters[0] = new InputFilter.LengthFilter(
						mCardType.getCVCLength());
				mVerification.setFilters(filters);

				// validate CVC input if there is any
				if (mVerification.length() > 0) {
					CreditCardValidator.validateCheckNumber(getActivity(),
							mVerification, mCardType);
				}
			}
		}

		int getCCardMarkerPosition(int oldPosition, boolean isDelete,
				CardTypeParser.CardType currentType) {
			int newPosition = oldPosition;

			if (currentType.getMarkersPositions().contains(oldPosition))
				if (isDelete)
					return newPosition--;
				else
					newPosition++;
			return newPosition++;
		}

		String showCCardWithIntervals(String noIntervalsString,
									  CardTypeParser.CardType currentType) {
			String result = "";
			int index = 1;
			for (char c : noIntervalsString.toCharArray()) {
				result += c;
				if (currentType.getSpacesPositions().contains(index))
					result += cardNumberSpace;
				index++;
			}
			return result;
		}
	};

	TextWatcher dateTextWatcher = new TextWatcher() {
		int dateCursor;
		String formattedDate;
		boolean isDateDelete = false;
		int dateLength;

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
								  int count) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
									  int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			mDateText.setError(null);
			mDateText.setTextColor(getResources().getColor(
					R.color.colorEditText));
			formattedDate = mDateText.getEditableText().toString();
			isDateDelete = (dateLength > formattedDate.length());

			mDateText.removeTextChangedListener(this);
			dateCursor = mDateText.getSelectionStart();
			dateFormatted(isDateDelete);

			mDateText.setText(formattedDate);
			mDateText.setSelection(dateCursor);
			mDateText.addTextChangedListener(this);

			if (formattedDate.length() > 4) {
				if (CreditCardValidator.validateDateFormat(formattedDate)
						&& CreditCardValidator.validateDate(getActivity(),
						mDateText)) {
					mVerification.requestFocus();

				} else {
					mDateText.setTextColor(getResources().getColor(
							R.color.errorTextColor));
				}
			}
			dateLength = formattedDate.length();
		}

		void dateFormatted(boolean isDelete) {

			formattedDate = formattedDate.replaceAll(cardNumberSpace, "");
			if (formattedDate.length() == 3 && !isDelete && dateCursor == 3)
				dateCursor++;
			if (formattedDate.length() == 2 && isDelete && dateCursor > 1)
				dateCursor--;
			if (formattedDate.length() > 2) {
				formattedDate = formattedDate.substring(0, 2) + "/"
						+ formattedDate.substring(2);
			}
		}
	};

	TextWatcher nameTextWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
								  int count) {
			mName.setError(null);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
									  int after) {

		}

		@Override
		public void afterTextChanged(Editable s) {

		}
	};

	TextWatcher mValidationTextWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
								  int count) {
			mVerification.setError(null);
			mVerification.setTextColor(getResources().getColor(
					R.color.colorEditText));
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
									  int after) {

		}

		@Override
		public void afterTextChanged(Editable s) {

		}
	};
}

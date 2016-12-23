package de.lindenvalley.lib;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import java.util.Calendar;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreditCardValidator {

	private static final String TAG = "CreditCardValidator";

	private static final  String cardNumberSpace = "/";
	private static final int prefix = 2;

	private static boolean validateIsEmpty(String field) {
		return TextUtils.isEmpty(field);
	}

	public static boolean validate(Context context, EditText name, EditText creditCardNumber,
								   EditText dateText, EditText checkNumber,
								   CardTypeParser.CardType cardType,
								   Collection<CardTypeParser.CardType> allowedCardTypes) {
		boolean valid = true;
		valid = validateName(name);
		valid = validateCreditCardNumber(context, creditCardNumber, cardType,
				allowedCardTypes) && valid;
		valid = validateDate(context, dateText) && valid;
		valid = validateCheckNumber(context, checkNumber, cardType) && valid;
		return valid;
	}

	private static boolean validateName(EditText name) {
		if (validateIsEmpty(name.getEditableText().toString())) {
			name.setError("Invalid field");
			return false;
		}
		return true;
	}

	public static boolean isValidCardType(Context context, EditText creditCardNumber,
										  CardTypeParser.CardType cardType) {
		if (cardType == CardTypeParser.CardType.Invalid) {
			Log.d(TAG, cardType.name());
			creditCardNumber.setTextColor(ContextCompat.getColor(context, R.color.errorTextColor));
			return false;
		}
		return true;
	}

	public static boolean validateCreditCardNumber(Context context, EditText creditCardNumber,
												   CardTypeParser.CardType cardType,
			Collection<CardTypeParser.CardType> allowedCardTypes) {
		creditCardNumber.setError(null);
		if (validateIsEmpty(creditCardNumber.getEditableText().toString())) {
			creditCardNumber.setError("Invalid field");
			return false;
		}
		String ccnumber = creditCardNumber.getEditableText().toString();
		ccnumber = ccnumber.replaceAll(cardNumberSpace, "");

		if (cardType.getMinLength() == cardType.getMaxLength()
				&& cardType.getMaxLength() != ccnumber.length()) {
			creditCardNumber.setTextColor(ContextCompat.getColor(context, R.color.errorTextColor));
			return false;
		}
		if (ccnumber.length() < cardType.getMinLength()
				|| ccnumber.length() > cardType.getMaxLength()) {
			creditCardNumber.setTextColor(ContextCompat.getColor(context, R.color.errorTextColor));
			return false;
		}

		if (!allowedCardTypes.contains(cardType)) {
			creditCardNumber.setTextColor(ContextCompat.getColor(context, R.color.errorTextColor));
			creditCardNumber.setError("Invalid field");
			return false;
		}

		if (cardType.isLuhn() && !luhnCheck(ccnumber)) {
			creditCardNumber.setTextColor(ContextCompat.getColor(context, R.color.errorTextColor));
			return false;
		}

		return true;
	}

	public static boolean validateDateFormat(String date) {
		String dateRegEx = "((^0(?![^1-9])[1-9]?)|(^1(?![^012])[012]?))(?![^\\/])\\/?[0-9]{0,2}$";
		Pattern datePattern = Pattern.compile(dateRegEx);
		Matcher matcher;
		matcher = datePattern.matcher(date);
		boolean find = matcher.find();
		Log.d(TAG, Boolean.toString(find));
		return find;
	}

	public static boolean validateDate(Context context, EditText dateText) {
		dateText.setError(null);
		if (validateIsEmpty(dateText.getEditableText().toString())) {
			dateText.setError("Invalid field");
			return false;
		}
		String date = dateText.getEditableText().toString();
		// date is validated only after we have the whole 5 symbols in format:
		// MM/YY which is guaranteed with regEx check in creditCardFragment
		// before this one
		if (date.length() > 4) {

			String month = date.substring(0, 2);
			String year = date.substring(3, 5);

			boolean notNumber = false;
			Calendar now = Calendar.getInstance();
			Calendar ccdate = Calendar.getInstance();
			try {
				ccdate.set(Calendar.MONTH, Integer.parseInt(month) - 1);
			} catch (NumberFormatException e) {
				dateText.setError("Invalid field");
				notNumber = true;
			}
			try {
				ccdate.set(Calendar.YEAR, Integer.parseInt(year)
						+ prefix * 1000);
			} catch (NumberFormatException e) {
				dateText.setError("Invalid field");
				notNumber = true;
			}
			if (notNumber) {
				return false;
			}
			// CCs are valid until the end of the month
			ccdate.set(Calendar.DAY_OF_MONTH,
					ccdate.getActualMaximum(Calendar.DAY_OF_MONTH));
			ccdate.set(Calendar.HOUR_OF_DAY, 23);
			ccdate.set(Calendar.MINUTE, 59);
			if (ccdate.before(now)) {
				dateText.setTextColor(ContextCompat.getColor(context, R.color.errorTextColor));
				return false;
			}
		}
		return true;
	}

	public static boolean validateCheckNumber(Context context, EditText checkNumber,
											  CardTypeParser.CardType type) {
		checkNumber.setError(null);
		if (validateIsEmpty(checkNumber.getEditableText().toString())) {
			checkNumber.setError("Invalid field");
			return false;
		}
		if (type == CardTypeParser.CardType.AmericanExpress) {
			if (!(checkNumber.getEditableText().length() == CardTypeParser.CardType.AmericanExpress
					.getCVCLength() || checkNumber.getEditableText().length() == CardTypeParser.CardType.AmericanExpress
					.getCVCLength() - 1)) {
				checkNumber.setTextColor(ContextCompat.getColor(context, R.color.errorTextColor));
				return false;
			}
		} else {
			if (checkNumber.getEditableText().length() != 3) {
				checkNumber.setTextColor(ContextCompat.getColor(context, R.color.errorTextColor));
				return false;
			}
		}
		return true;
	}

	/**
	 */
	/**
	 * Performs the luhn algorithm. {@link #luhnCheck(long)}
	 * 
	 * @param cardNumber
	 *            the credit card number
	 * @return true if valid credit card number
	 */
	private static boolean luhnCheck(String cardNumber) {
		if (TextUtils.isEmpty(cardNumber)) {
			return false;
		}
		try {
			return luhnCheck(Long.parseLong(cardNumber));
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * Performs the luhn algorithm.</br> Source:
	 * http://www.merriampark.com/anatomycc.htm</br> Information:
	 * http://en.wikipedia.org/wiki/Luhn_algorithm
	 * 
	 * @param cardNumber
	 *            a creditcardnumber
	 * @return true if valid, false otherwise
	 */
	private static boolean luhnCheck(long cardNumber) {
		String digitsOnly = String.valueOf(cardNumber);
		int sum = 0;
		int digit = 0;
		int addend = 0;
		boolean timesTwo = false;

		for (int i = digitsOnly.length() - 1; i >= 0; i--) {
			digit = Integer.parseInt(digitsOnly.substring(i, i + 1));
			if (timesTwo) {
				addend = digit * 2;
				if (addend > 9) {
					addend -= 9;
				}
			} else {
				addend = digit;
			}
			sum += addend;
			timesTwo = !timesTwo;
		}

		int modulus = sum % 10;
		return modulus == 0;
	}

}

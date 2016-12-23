package de.lindenvalley.lib;

import android.text.TextUtils;
import android.widget.EditText;

public class IbanBicValidator {

	private static boolean validateIsEmpty(String field) {
		return TextUtils.isEmpty(field);
	}

	private static final int IBAN_MIN_SIZE = 15;
	private static final int IBAN_MAX_SIZE = 34;
	private static final long IBAN_MAX = 999999999;
	private static final long IBAN_MODULUS = 97;

	private static boolean validateIban(String iban) {
		String trimmed = iban.trim();
		if (trimmed.length() < IBAN_MIN_SIZE
				|| trimmed.length() > IBAN_MAX_SIZE) {
			return false;
		}
		String reformat = trimmed.substring(4) + trimmed.substring(0, 4);
		long total = 0;
		for (int i = 0; i < reformat.length(); i++) {
			int charValue = Character.getNumericValue(reformat.charAt(i));
			if (charValue < 0 || charValue > 35) {
				return false;
			}
			total = (charValue > 9 ? total * 100 : total * 10) + charValue;
			if (total > IBAN_MAX) {
				total = (total % IBAN_MODULUS);
			}
		}
		return (total % IBAN_MODULUS) == 1;
	}

	private static boolean validateBic(String bic) {
		//return bic != null && (bic.length() == 7 || bic.length() == 11);
		return bic != null && !bic.isEmpty();
	}

	public static boolean validate(EditText name, EditText iban, EditText bic) {
		boolean valid = true;
		valid = validateName(name);
		valid = validateIban(iban) && valid;
		valid = validateBic(bic) && valid;
		return valid;
	}

	private static boolean validateName(EditText name) {
		name.setError(null);
		if (validateIsEmpty(name.getEditableText().toString())) {
			name.setError("Invalid field");
			return false;
		}
		return true;
	}

	private static boolean validateIban(EditText iban) {
		iban.setError(null);
		if (!validateIban(iban.getEditableText().toString().replaceAll("\\s", ""))) {
			iban.setError("Invalid field");
			return false;
		}
		return true;
	}

	private static boolean validateBic(EditText bic) {
		bic.setError(null);
		if (!validateBic(bic.getEditableText().toString())) {
			bic.setError("Invalid field");
			return false;
		}
		return true;
	}
}

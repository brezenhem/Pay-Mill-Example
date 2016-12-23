package de.lindenvalley.lib;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * The type of payment that should be triggered.
 */
public enum PaymentType implements Parcelable {
    /**
     * For token generation, when using the mode and public key specified
     * during initialization.
     */
    TOKEN,
    /**
     * For token generation, when using specific mode and public key
     * specified during initialization.
     */
    TOKEN_WITH_PARAMS,
    /**
     * For transactions.
     */
    TRANSACTION,
    /**
     * For preauthorizations.
     */
    PREAUTHORIZATION;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(ordinal());
    }

    public static final Creator<PaymentType> CREATOR = new Creator<PaymentType>() {
        @Override
        public PaymentType createFromParcel(final Parcel source) {
            return PaymentType.values()[source.readInt()];
        }

        @Override
        public PaymentType[] newArray(final int size) {
            return new PaymentType[size];
        }
    };
}

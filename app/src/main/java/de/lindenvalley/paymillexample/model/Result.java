package de.lindenvalley.paymillexample.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.paymill.android.api.Transaction;
import com.paymill.android.service.PMError;

public class Result implements Parcelable {
    public enum Type implements Parcelable {
        TRANSACTION, TOKEN;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {
            dest.writeInt(ordinal());
        }

        public static final Creator<Type> CREATOR = new Creator<Type>() {
            @Override
            public Type createFromParcel(final Parcel source) {
                return Type.values()[source.readInt()];
            }

            @Override
            public Type[] newArray(final int size) {
                return new Type[size];
            }
        };
    }

    private Transaction result;
    private String resultToken;
    private Type type;
    private PMError error;
    private int activityResult;

    Result(Transaction result, String resultToken, Type type,
           PMError error, int activityResult) {
        this.result = result;
        this.resultToken = resultToken;
        this.type = type;
        this.error = error;
        this.activityResult = activityResult;
    }

    public String getResultToken() {
        return resultToken;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private Result(Parcel source) {
        result = source.readParcelable(Transaction.class.getClassLoader());
        resultToken = source.readString();
        type = source.readParcelable(Type.class.getClassLoader());
        error = source.readParcelable(PMError.class.getClassLoader());
        activityResult = source.readInt();

    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeParcelable(result, 0);
        dest.writeString(resultToken);
        dest.writeParcelable(type, 0);
        dest.writeParcelable(error, 0);
        dest.writeInt(activityResult);
    }

    public static final Creator<Result> CREATOR = new Creator<Result>() {
        @Override
        public Result createFromParcel(final Parcel source) {
            return new Result(source);
        }

        @Override
        public Result[] newArray(final int size) {
            return new Result[size];
        }
    };
}

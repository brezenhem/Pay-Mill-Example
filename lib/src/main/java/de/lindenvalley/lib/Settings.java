package de.lindenvalley.lib;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class Settings implements Parcelable {

    private String accountHolder;
    private String cardnumber;
    private String expiryMonth;
    private String expiryYear;
    private String verification;
    private String accountNumber;
    private String bankNumber;
    private String directDebitCountry;
    private HashSet<CardTypeParser.CardType> cardTypes = new HashSet<CardTypeParser.CardType>();

    public Settings() {
        cardTypes.add(CardTypeParser.CardType.Visa);
        cardTypes.add(CardTypeParser.CardType.Maestro);
        cardTypes.add(CardTypeParser.CardType.MasterCard);
        cardTypes.add(CardTypeParser.CardType.AmericanExpress);
        cardTypes.add(CardTypeParser.CardType.DinersClub);
        cardTypes.add(CardTypeParser.CardType.Discover);
        cardTypes.add(CardTypeParser.CardType.UnionPay);
        cardTypes.add(CardTypeParser.CardType.JCB);
    }

    private Settings(Parcel source) {
        ArrayList<CardTypeParser.CardType> tempList = new ArrayList<CardTypeParser.CardType>();
        source.readList(tempList, null);
        cardTypes.addAll(tempList);
        directDebitCountry = source.readString();
        accountHolder = source.readString();
        cardnumber = source.readString();
        expiryMonth = source.readString();
        expiryYear = source.readString();
        verification = source.readString();
        accountNumber = source.readString();
        bankNumber = source.readString();
    }

    public Collection<CardTypeParser.CardType> getAllowedCardTypes() {
        return cardTypes;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public String getCardNumber() {
        return cardnumber;
    }

    public String getExpiryMonth() {
        return expiryMonth;
    }

    public String getExpiryYear() {
        return expiryYear;
    }

    public String getVerification() {
        return verification;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getBankNumber() {
        return bankNumber;
    }

    public HashSet<CardTypeParser.CardType> getCardTypes() {
        return cardTypes;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ArrayList<CardTypeParser.CardType> tempList = new ArrayList<CardTypeParser.CardType>();
        tempList.addAll(cardTypes);
        dest.writeList(tempList);
        dest.writeString(directDebitCountry);
        dest.writeString(accountHolder);
        dest.writeString(cardnumber);
        dest.writeString(expiryMonth);
        dest.writeString(expiryYear);
        dest.writeString(verification);
        dest.writeString(accountNumber);
        dest.writeString(bankNumber);

    }

    public static final Creator<Settings> CREATOR = new Creator<Settings>() {

        @Override
        public Settings createFromParcel(Parcel source) {
            return new Settings(source);
        }

        @Override
        public Settings[] newArray(int size) {
            return new Settings[size];
        }
    };
}

package bankdroid.smskey.bank;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 */
public class Bank implements Serializable, Cloneable {
	public static final int UNASSIGNED_ID = -1;
	public static final String F__ID = "_id";
	public static final String F_NAME = "name";
	public static final String DEFAULT_SORT_ORDER = Bank.F_NAME;
	public static final String F_VALIDITY = "validity";
	public static final String F_COUNTRY = "country";
	public static final String F_PHONENUMBERS = "phonenumbers";
	public static final String F_EXPRESSIONS = "expressions";
	public static final String F_LASTMESSAGE = "lastmsg";
	public static final String F_LASTADDRESS = "lastaddr";
	public static final String F_TIMESTAMP = "lastts";
	/**
	 * Custom country indicates that the bank was created manual, and should not be dropped during
	 * updated of the database.
	 */
	public static final String CUSTOM_COUNTRY = "xx";
	private static final long serialVersionUID = -9140003377538504964L;
	///NON-STATIC MEMBERS
	private int id;
	private String name;

	/**
	 * Validity period of an SMS OTP expressed in seconds.
	 */
	private int expiry;
	/**
	 * One or more phone number can be registered to the Bank.
	 */
	private String[] phoneNumbers;
	private Expression[] extractExpressions;
	/**
	 * Contains compiled regular expressions. Initialized in lazy mode, as most of the banks will be never used on
	 * the same mobile.
	 */
	private Pattern[] patterns;
	/**
	 * Country of the Bank.
	 */
	private String countryCode;

	public Bank() {
		// empty constructor
		id = UNASSIGNED_ID;
		name = "";
		expiry = -1;
		phoneNumbers = new String[0];
		extractExpressions = new Expression[0];
		countryCode = CUSTOM_COUNTRY;
	}

	public Bank(final int id, final String name, final int expiry, final String[] phoneNumber,
				final Expression[] extractExpression, final String countryCode) {
		this.id = id;
		this.name = name;
		this.expiry = expiry;
		this.phoneNumbers = phoneNumber;
		this.extractExpressions = extractExpression;
		this.countryCode = countryCode;
	}

	public void addExtractExpression(final Expression extractExpression) {
		final Expression[] ee = new Expression[extractExpressions.length + 1];
		System.arraycopy(extractExpressions, 0, ee, 0, extractExpressions.length);
		ee[ee.length - 1] = extractExpression;
		extractExpressions = ee;
		patterns = null;
	}

	public void addPhoneNumber(final String phoneNumber) {
		final String[] pn = new String[phoneNumbers.length + 1];
		System.arraycopy(phoneNumbers, 0, pn, 0, phoneNumbers.length);
		pn[pn.length - 1] = phoneNumber;
		phoneNumbers = pn;
	}

	@Override
	public Object clone() {
		final String[] pn = new String[phoneNumbers.length];
		System.arraycopy(phoneNumbers, 0, pn, 0, pn.length);

		final Expression[] ee = new Expression[extractExpressions.length];
		System.arraycopy(extractExpressions, 0, ee, 0, ee.length);

		return new Bank(id, name, expiry, pn, ee, countryCode);
	}

	private Pattern[] initPattern() {
		//lazy init with optimization for performance
		Pattern[] lp = patterns;
		if (lp == null) {
			final Expression[] ee = extractExpressions;
			final int len = ee.length;

			lp = patterns = new Pattern[len];
			for (int i = 0; i < len; i++) {
				lp[i] = Pattern.compile(ee[i].getExpression());
			}
		}
		return lp;
	}

	public String extractCode(final String message) {
		final Pattern[] lp = initPattern();

		final int len = lp.length;

		for (int i = 0; i < len; i++) {
			final Matcher matcher = lp[i].matcher(message);
			if (matcher.find())
				return matcher.group(1);
		}
		return null;
	}

	public boolean isTransactionSign(final String message) {
		final Pattern[] lp = initPattern();

		final int len = lp.length;

		for (int i = 0; i < len; i++) {
			final Matcher matcher = lp[i].matcher(message);
			if (matcher.find()) {
				return extractExpressions[i].isTransactionSign();
			}
		}
		return false;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(final String countryCode) {
		this.countryCode = countryCode;
	}

	public int getExpiry() {
		return expiry;
	}

	public void setExpiry(final int expiry) {
		this.expiry = expiry;
	}

	public Expression[] getExtractExpressions() {
		return extractExpressions;
	}

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String[] getPhoneNumbers() {
		return phoneNumbers;
	}

	public boolean isBankPhoneNumber(final String phoneNumber) {
		final String[] pn = phoneNumbers;
		final int len = pn.length;
		for (int i = 0; i < len; i++) {
			if (pn[i].equals(phoneNumber))
				return true;
		}
		return false;
	}

	public void removeExtractExpression(final int index) {
		final Expression[] eeOld = extractExpressions;
		final int len = eeOld.length;

		if (index < 0 || index >= len)
			throw new ArrayIndexOutOfBoundsException("Invalid extract expression index: " + index + "(" + len + ")");

		final Expression[] ee = new Expression[len - 1];
		int eei = 0;
		for (int i = 0; i < len; i++) {
			if (i != index) {
				ee[eei] = eeOld[i];
				eei++;
			}
		}

		patterns = null;
		extractExpressions = ee;
	}

	public void removePhoneNumber(final int index) {
		final String[] pnOld = phoneNumbers;
		final int len = pnOld.length;

		if (index < 0 || index >= len)
			throw new ArrayIndexOutOfBoundsException("Invalid phone number index: " + index + "(" + len + ")");

		final String[] pn = new String[len - 1];
		int pni = 0;
		for (int i = 0; i < len; i++) {
			if (i != index) {
				pn[pni] = pnOld[i];
				pni++;
			}
		}

		phoneNumbers = pn;
	}

	@Override
	public String toString() {
		return name;
	}
}

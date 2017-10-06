package bankdroid.smskey.bank.test;

public class TestEntry {
	private String number;
	private String message;
	private String expectedCode;

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getExpectedCode() {
		return expectedCode;
	}

	public void setExpectedCode(String expectedCode) {
		this.expectedCode = expectedCode;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("TestEntry{");
		sb.append("number='").append(number).append('\'');
		sb.append(", expectedCode='").append(expectedCode).append('\'');
		sb.append('}');
		return sb.toString();
	}
}

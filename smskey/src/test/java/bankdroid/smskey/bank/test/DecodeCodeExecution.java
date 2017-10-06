package bankdroid.smskey.bank.test;

public class DecodeCodeExecution {
	private final String message;
	private final String extractedCode;

	public DecodeCodeExecution(String message, String extractedCode) {
		this.message = message;
		this.extractedCode = extractedCode;
	}

	public boolean isSuccessful(){
		return null != extractedCode;
	}
}

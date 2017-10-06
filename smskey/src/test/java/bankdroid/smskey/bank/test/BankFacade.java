package bankdroid.smskey.bank.test;

import bankdroid.smskey.bank.Bank;

import java.util.ArrayList;
import java.util.List;

public class BankFacade {

	private final Bank decoratedBank;
	private final List<DecodeCodeExecution> executions = new ArrayList<>();
	private boolean successfulExecution = false;

	public BankFacade(Bank decoratedBank) {
		this.decoratedBank = decoratedBank;
	}

	public String extractCode(String message) {
		String extractedCode = decoratedBank.extractCode(message);
		DecodeCodeExecution decodeCodeExecution = new DecodeCodeExecution(message, extractedCode);
		executions.add(decodeCodeExecution);
		if (decodeCodeExecution.isSuccessful()) {
			successfulExecution = true;
		}
		return extractedCode;
	}

	public String getCountryCode() {
		return decoratedBank.getCountryCode();
	}

	public boolean hadSuccessfulExecution() {
		return successfulExecution;
	}

	public Bank getBank() {
		return decoratedBank;
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", decoratedBank.getName(), decoratedBank.getCountryCode());
	}
}

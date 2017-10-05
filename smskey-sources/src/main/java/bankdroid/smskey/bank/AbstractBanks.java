package bankdroid.smskey.bank;

import java.util.List;

public abstract class AbstractBanks
{

	private List<Bank> banks;

	public void append( final List<Bank> banks )
	{
		this.banks = banks;
		init();
	}

	public abstract void init();

	public void addBank( final Bank bank )
	{
		banks.add(bank);
	}
}

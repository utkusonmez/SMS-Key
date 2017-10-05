package bankdroid.smskey;

import java.io.Serializable;
import java.util.Date;

import bankdroid.smskey.bank.Bank;

public class Message implements Serializable
{
	private static final long serialVersionUID = 2119110172022224864L;

	private Bank bank;
	private String code;
	private String message;
	private String originatingAddress;
	private Date timestamp;

	public Message()
	{
		super();
	}

	public Message( final Bank bank, final String message, final Date timestamp, final String originatingAddress,
			final String code )
	{
		super();
		this.bank = bank;
		this.message = message;
		this.timestamp = timestamp;
		this.originatingAddress = originatingAddress;
		this.code = code;
	}

	@Override
	public boolean equals( final Object o )
	{
		if ( o instanceof Message )
		{
			final Message other = (Message) o;

			return other.originatingAddress.equals(originatingAddress) && message.equals(other.message)
					&& timestamp.equals(other.timestamp);
		}
		return false;
	}

	public Bank getBank()
	{
		return bank;
	}

	public String getCode()
	{
		return code;
	}

	public String getMessage()
	{
		return message;
	}

	public String getOriginatingAddress()
	{
		return originatingAddress;
	}

	public Date getTimestamp()
	{
		return timestamp;
	}

	public void setBank( final Bank bank )
	{
		this.bank = bank;
	}

	public void setCode( final String code )
	{
		this.code = code;
	}

	public void setMessage( final String message )
	{
		this.message = message;
	}

	public void setOriginatingAddress( final String originatingAddress )
	{
		this.originatingAddress = originatingAddress;
	}

	public void setTimestamp( final Date timestamp )
	{
		this.timestamp = timestamp;
	}

}
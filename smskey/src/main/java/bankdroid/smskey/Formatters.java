package bankdroid.smskey;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Formatters
{
	private Formatters()
	{
		// to avoid instantiation
	}
	////////////// DATE

	private static final ThreadLocal<DateFormat> dateFactory = new ThreadLocal<DateFormat>()
	{
		@Override
		protected DateFormat initialValue()
		{
			return DateFormat.getDateInstance(DateFormat.SHORT);
		}
	};

	public final static DateFormat getShortDateFormat()
	{
		return dateFactory.get();
	}

	////////////// TIMESTAMP

	private static final ThreadLocal<DateFormat> timestampFactory = new ThreadLocal<DateFormat>()
	{
		@Override
		protected DateFormat initialValue()
		{
			return new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
		}
	};

	public final static DateFormat getTimstampFormat()
	{
		return timestampFactory.get();
	}

}

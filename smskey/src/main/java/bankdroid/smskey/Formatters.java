package bankdroid.smskey;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Formatters {
	private static final ThreadLocal<DateFormat> dateFactory = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return DateFormat.getDateInstance(DateFormat.SHORT);
		}
	};

	private static final ThreadLocal<DateFormat> timestampFactory = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
		}
	};

	private Formatters() {
		// to avoid instantiation
	}

	public final static DateFormat getShortDateFormat() {
		return dateFactory.get();
	}

	public final static DateFormat getTimstampFormat() {
		return timestampFactory.get();
	}

}

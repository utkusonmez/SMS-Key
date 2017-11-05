package bankdroid.util;

import org.androidannotations.annotations.EBean;

@EBean
public class CodeUtils {
	private static final String SPACE = " ";

	public String splitCode(String code, final int splitSize) {
		if (splitSize != 0) {
			final StringBuilder sb = new StringBuilder(code);

			int size = sb.length();
			int i = 0;
			while (i + splitSize < size) {
				i += splitSize;
				sb.insert(i++, SPACE);
				size++;
			}
			code = sb.toString();
		}
		return code;
	}
}

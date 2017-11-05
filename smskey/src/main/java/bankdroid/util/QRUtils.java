package bankdroid.util;

import android.graphics.Bitmap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import org.androidannotations.annotations.EBean;

import java.util.EnumMap;
import java.util.Map;

import static android.graphics.Color.BLACK;

@EBean
public class QRUtils {

	public Bitmap encodeAsBitmap(String str, int width, int height, int backgroundColor) throws WriterException {
		BitMatrix result;
		Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		hints.put(EncodeHintType.MARGIN, 2); /* default = 4 */
		try {
			result = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, width, height, hints);
		} catch (IllegalArgumentException iae) {
			// Unsupported format
			return null;
		}
		int w = result.getWidth();
		int h = result.getHeight();
		int[] pixels = new int[w * h];
		for (int y = 0; y < h; y++) {
			int offset = y * w;
			for (int x = 0; x < w; x++) {
				pixels[offset + x] = result.get(x, y) ? BLACK : backgroundColor;
			}
		}
		Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, w, h);
		return bitmap;
	}
}

package akhil.DataUnlimited.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtil {

	public static String compressgzipbase64(String data) {
		try {

			// Create an output stream, and a gzip stream to wrap over.
			ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length());
			GZIPOutputStream gzip = new GZIPOutputStream(bos);

			// Compress the input string
			gzip.write(data.getBytes());
			gzip.close();
			byte[] compressed = bos.toByteArray();
			bos.close();

			// Convert to base64
			compressed = Base64.getEncoder().encode(compressed);

			// return the newly created string
			return new String(compressed);
		} catch (IOException e) {

			return null;
		}
	}

	public static String encodebase64(String data) {

		return Base64.getEncoder().encodeToString(data.getBytes());

	}

	public static String decodebase64(String data) {

		return new String(Base64.getDecoder().decode(data));

	}

	public static String decompressgzipbase64(String compressedText) throws IOException {

		// get the bytes for the compressed string
		byte[] compressed = compressedText.getBytes("UTF8");

		// convert the bytes from base64 to normal string
		Base64.Decoder d = Base64.getDecoder();
		compressed = d.decode(compressed);

		// decode.
		final int BUFFER_SIZE = 32;

		ByteArrayInputStream is = new ByteArrayInputStream(compressed);

		GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);

		StringBuilder string = new StringBuilder();

		byte[] data = new byte[BUFFER_SIZE];

		int bytesRead;

		while ((bytesRead = gis.read(data)) != -1) {
			string.append(new String(data, 0, bytesRead));
		}
		gis.close();
		is.close();
		return string.toString();
	}

}

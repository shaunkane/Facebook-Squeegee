package utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class SaveImage {
	public static void saveImageFromURL(String imageURL, String imageLocal) {
		// This thing can throw like a thousand exceptions, 
		// so I'm being lazy and just catching IO.
		try{
			URL url = new URL(imageURL);
			
			InputStream is = url.openStream();
			OutputStream os = new FileOutputStream(imageLocal);
			
			byte[] b = new byte[2048];
			int length;
			
			while((length = is.read(b)) != -1) {
				os.write(b, 0, length);
			}
			
			is.close();
			os.close();
		} catch(IOException e) {
			System.err.println("Couldn't download image!");
			e.printStackTrace();
		}
	}
}

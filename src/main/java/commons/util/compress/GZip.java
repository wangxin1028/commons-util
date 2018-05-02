package commons.util.compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 *Author:WangXin69
 *Date:2018年5月2日
 *字符串压缩和解压
 */
public class GZip {
	public static byte[] compress(String str, String encoding) {  
        if (str == null || str.length() == 0) {  
            return null;  
        }  
        ByteArrayOutputStream out = new ByteArrayOutputStream();  
        GZIPOutputStream gzip;  
        try {  
            gzip = new GZIPOutputStream(out);  
            gzip.write(str.getBytes(encoding));  
            gzip.close();  
        } catch (IOException e) {  
            e.printStackTrace();
        }  
        return out.toByteArray();  
    } 
	
    public static String uncompress(byte[] bytes, String encode) {  
		byte[] recoverbytes = uncompress(bytes);
		String recover=null;
		try {
			recover = new String(recoverbytes, encode);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return recover;
    }
    
    public static byte[] uncompress(byte[] bytes) {  
        if (bytes == null || bytes.length == 0) {  
            return null;  
        }  
        ByteArrayOutputStream out = new ByteArrayOutputStream();  
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);  
        try {  
            GZIPInputStream ungzip = new GZIPInputStream(in);  
            byte[] buffer = new byte[256];  
            int n;  
            while ((n = ungzip.read(buffer)) >= 0) {  
                out.write(buffer, 0, n);  
            }  
        } catch (IOException e) {  
            e.printStackTrace();
        }  
  
        return out.toByteArray();  
    } 
}



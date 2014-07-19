import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;


public class InfDef {

	/**
	 * Zlib algorithm on a data to produce bytes
	 * @param data
	 * @return
	 */
	public static byte[] encode(String data) {
		//Convert input string to byte array
		byte[] input;
		try {
			input = data.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}  
		
		//Setup deflater
        Deflater df = new Deflater(); 
        df.setInput(input);
 
        //Create output
        ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length);
        
        //Start the deflater process
        df.finish();
        
        //Create a buffer
        byte[] buff = new byte[1024];
        while(!df.finished())
        {
        	//Fill the buffer with compressed data and store the number of characters compressed
            int count = df.deflate(buff);       
            //Write the data in the buffer to the output stream
            baos.write(buff, 0, count);
        }
        try {
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        byte[] output = baos.toByteArray();
        
        return output;
	}
	
	/**
	 * Undoes the zlib compression algorithm
	 * @param input
	 * @return
	 */
	public static String decode(byte[] input) {
		//Setup inflater
		Inflater inflater = new Inflater();
        inflater.setInput(input);
 
        //Create output
        ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length);
        
        //Buffer
        byte[] buff = new byte[1024];
        
        while(!inflater.finished())
        {
        	//Decompress to buffer
            int count;
			try {
				count = inflater.inflate(buff);
			} catch (DataFormatException e) {
				e.printStackTrace();
				return null;
			}
            baos.write(buff, 0, count);
        }
        try {
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
        
        return new String(baos.toByteArray());
	}
}
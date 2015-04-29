package xmlprocess;


import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class BOMDetect {
    public static void main(String[] args) {
        System.out.println("");

    }

    public void checkEncoding(InputStream in) {

        try {
            byte[] startOfData = getByteArray(in);

            ByteArrayInputStream bais = new ByteArrayInputStream(startOfData);
            BOMInputStream bis = new BOMInputStream(bais, true, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_8);
            if (bis.hasBOM()) {
                System.out.println("found BOM");
            } else {
                System.out.println("No BOM");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] getStartOfData(byte[] data) {
        int length = Math.min(data.length, 100);
        byte[] startOfData = new byte[length];
        System.arraycopy(data, 0, startOfData, 0, length);
        return startOfData;
    }

    private byte[] getByteArray(InputStream in) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int next = in.read();
        while (next > -1) {
            bos.write(next);
            next = in.read();
        }
        bos.flush();
        return bos.toByteArray();
    }
}

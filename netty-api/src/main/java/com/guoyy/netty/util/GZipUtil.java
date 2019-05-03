package com.guoyy.netty.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.GZIPOutputStream;

public class GZipUtil {

    public static byte[] gzip(String str) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream os = new GZIPOutputStream(baos);
        os.write(str.getBytes(Charset.forName("UTF-8")));
        os.close();

        return baos.toByteArray();
    }

    public static String gunzip(byte[] src) {


        return null;
    }


}

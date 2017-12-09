package com.example.chenn.scan.erweima;

import android.graphics.Bitmap;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.util.Hashtable;

/**
 * Created by chenn on 2017/9/14.
 */

public class ParserErwma extends LuminanceSource {

    private byte bitmapPixels[];

    public ParserErwma(Bitmap bitmap){

        this(bitmap.getWidth(),bitmap.getHeight());

        //首先，要取得该图片的像素数组内容
        int[] data = new int[bitmap.getHeight()*bitmap.getWidth()];
        bitmapPixels = new byte[bitmap.getHeight()*bitmap.getWidth()];
        bitmap.getPixels(data, 0, getWidth(), 0, 0, getWidth(), getHeight());

        //将int数组转换为byte数组，也就是取像素值中蓝色值部分作为辨析内容
        for(int i=0; i < data.length; i++){
            bitmapPixels[i] = (byte)data[i];
        }
    }

    protected ParserErwma(int width, int height) {
        super(width, height);
    }

    @Override
    public byte[] getRow(int i, byte[] bytes) {

        System.arraycopy(bitmapPixels, i * getWidth(), bytes, 0, getWidth());
        return bytes;
    }

    @Override
    public byte[] getMatrix() {

        return bitmapPixels;
    }

    public static Result decodeBitmap(Bitmap bitmap){

        ParserErwma parserErwma = new ParserErwma(bitmap);

        //解码的参数
        Hashtable<DecodeHintType, String> hints = new Hashtable<>();

        //设置二维码内容的编码
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");

        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(parserErwma));

        QRCodeReader reader = new QRCodeReader();

        Result result = null;

        try{
            result = reader.decode(binaryBitmap, hints);
        } catch (FormatException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String decodeBitmapToString(Bitmap bitmap){

        Result result = decodeBitmap(bitmap);
        String str = "none";
        if (null != result){
            str = result.getText();
        }
        return str;
    }
}

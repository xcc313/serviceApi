package com.lzj.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c)2007-6-13</p>
 * <p>Company: fuen</p>
 * @author 杨振朋
 * @version 1.0
 */
public class ThumbnailImage {
   /**
    * 构造函数
    * @param fileName String
    * @throws IOException
    */
   public ThumbnailImage(File imageFile,String destFilePath) throws IOException {
	   
	    Integer w = 400;  // 新图宽高为300*200
	    Integer h = 300;
	    int modality = 0;// modality压缩方式 0 等比最小 1 按宽 2按高 3定值
	    File f = imageFile;
	    BufferedImage bufferedImage = javax.imageio.ImageIO.read(f);
	    Integer imgWidth = bufferedImage.getWidth(null);
	    Integer imgHeight = bufferedImage.getHeight(null);
	    // 得到合适的压缩大小，按比例。
	    if (modality == 0) {
	    if (imgWidth >= imgHeight) {
	      h = (int) Math.round((imgHeight * w * 1.0 / imgWidth));
	    }
	    else {
	      w = (int) Math.round((imgWidth * h * 1.0 / imgHeight));
	    }
	    }
	    else if (modality == 1) {
	    h = (int) Math.round((imgHeight * w * 1.0 / imgWidth));
	    }
	    // 构建图片对象
	    BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	    // 绘制缩小后的图
	    image.getGraphics().drawImage(bufferedImage, 0, 0, w, h, null);
	    ByteArrayOutputStream bao = new ByteArrayOutputStream();
	    ImageIO.write(image, "jpeg", bao);
	    byte[] data = bao.toByteArray();
	    //SaeStorage storage = new  SaeStorage();
	    //storage.write("image" , "filename"  ,data);
	    //storage.write("txjywupload" , "/"+destFile ,data);
   }  
   
   
   public ThumbnailImage(InputStream imageFileInput,String destFilePath) throws IOException {
	   
	    Integer w = 400;  // 新图宽高为300*200
	    Integer h = 300;
	    int modality = 0;// modality压缩方式 0 等比最小 1 按宽 2按高 3定值
	    BufferedImage bufferedImage = javax.imageio.ImageIO.read(imageFileInput);
	    Integer imgWidth = bufferedImage.getWidth(null);
	    Integer imgHeight = bufferedImage.getHeight(null);
	    // 得到合适的压缩大小，按比例。
	    if (modality == 0) {
	    if (imgWidth >= imgHeight) {
	      h = (int) Math.round((imgHeight * w * 1.0 / imgWidth));
	    }
	    else {
	      w = (int) Math.round((imgWidth * h * 1.0 / imgHeight));
	    }
	    }
	    else if (modality == 1) {
	    h = (int) Math.round((imgHeight * w * 1.0 / imgWidth));
	    }
	    // 构建图片对象
	    BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	    // 绘制缩小后的图
	    image.getGraphics().drawImage(bufferedImage, 0, 0, w, h, null);
	    ByteArrayOutputStream bao = new ByteArrayOutputStream();
	    ImageIO.write(image, "jpeg", bao);
	    byte[] data = bao.toByteArray();
	    File file = new File(destFilePath);
	    FileOutputStream fos = new FileOutputStream(file);
	    fos.write(data);
	    fos.close();
	    //SaeStorage storage = new  SaeStorage();
	    //storage.write("image" , "filename"  ,data);
	    //storage.write("txjywupload" , "/"+destFile ,data);
  }  
   
   
   public ThumbnailImage(URL fileUrl,String destFilePath) throws IOException {
	   
	    Integer w = 400;  // 新图宽高为300*200
	    Integer h = 300;
	    int modality = 0;// modality压缩方式 0 等比最小 1 按宽 2按高 3定值
	    BufferedImage bufferedImage = javax.imageio.ImageIO.read(fileUrl);
	    Integer imgWidth = bufferedImage.getWidth(null);
	    Integer imgHeight = bufferedImage.getHeight(null);
	    // 得到合适的压缩大小，按比例。
	    if (modality == 0) {
	    if (imgWidth >= imgHeight) {
	      h = (int) Math.round((imgHeight * w * 1.0 / imgWidth));
	    }
	    else {
	      w = (int) Math.round((imgWidth * h * 1.0 / imgHeight));
	    }
	    }
	    else if (modality == 1) {
	    h = (int) Math.round((imgHeight * w * 1.0 / imgWidth));
	    }
	    // 构建图片对象
	    BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	    // 绘制缩小后的图
	    image.getGraphics().drawImage(bufferedImage, 0, 0, w, h, null);
	    ByteArrayOutputStream bao = new ByteArrayOutputStream();
	    ImageIO.write(image, "jpeg", bao);
	    byte[] data = bao.toByteArray();
	    File file = new File(destFilePath);
	    FileOutputStream fos = new FileOutputStream(file);
	    fos.write(data);
	    fos.close();
	    //SaeStorage storage = new  SaeStorage();
	    //storage.write("image" , "filename"  ,data);
	    //storage.write("txjywupload" , "/"+destFile ,data);
 }  
}

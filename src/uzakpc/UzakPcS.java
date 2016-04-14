package uzakpc;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Scanner;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

public class UzakPcS {

    int yenileme;
    Robot r;
    Socket socket;
    Socket sockMos;
    FileOutputStream fos;
    InputStream in;
    OutputStream out;
    Scanner oku;
    PrintWriter yaz;

    public UzakPcS() throws Exception {
        Scanner keyread = new Scanner(System.in);
        System.out.println("ekran yenileme sıkligini giriniz");
        yenileme = keyread.nextInt();
        try
        {

            ServerSocket serMos = new ServerSocket(5454);
            sockMos = serMos.accept();
            oku = new Scanner(sockMos.getInputStream());
            yaz = new PrintWriter(sockMos.getOutputStream(), true);

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        r = new Robot();

        Runnable recmousepos = new Runnable() {

            @Override
            public void run() {
                try
                {
                    String gelen;
                    while (oku.hasNextLine())
                    {
                        if ((gelen = oku.nextLine()).equals("1"))
                        {
                            r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                        } else if (gelen.equals("2"))
                        {
                            r.mousePress(InputEvent.BUTTON2_DOWN_MASK);
                        } else if (gelen.equals("3"))
                        {
                            r.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                        } else
                        {

                            System.out.println(">> " + gelen);
                            String[] mpos = gelen.split("_");
                            int m0 = Integer.parseInt(mpos[0]);
                            int m1 = Integer.parseInt(mpos[1]);
                            
                            //r.mouseMove(m0, m1);//anapcden gelen mouse pos a gore mouse oynatıldı
                        }
                    }

                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

        };
        new Thread(recmousepos).start();

        Runnable gonder = new Runnable() {
            public void run() {
                while (true)
                {
                    try
                    {

                        ServerSocket sers = new ServerSocket(3434);
                        socket = sers.accept();
                        out = socket.getOutputStream();//serversocket baglantisi

                        fos = new FileOutputStream(new File("saved.jpg"));
                        in = new FileInputStream("saved.jpg");
                        GraphicsConfiguration config = GraphicsEnvironment
                                .getLocalGraphicsEnvironment()
                                .getDefaultScreenDevice()
                                .getDefaultConfiguration();
                        BufferedImage ss = r.createScreenCapture(config.getBounds());

                        writeJPG(ss, fos, 0.8f);//resmin dosyaya kaydedilmesi

                        copy(in, out);//resmin karsiya yollanması

                        out.flush();
                        fos.flush();

                        Thread.sleep(yenileme);
                        sers.close();
                        socket.close();

                    } catch (Exception ee)
                    {
                        ee.printStackTrace();

                    }
                }
            }
        };
        new Thread(gonder).start();
    }

    static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        int len = 0;
        while ((len = in.read(buf)) != -1)
        {
            out.write(buf, 0, len);
        }
    }

    public void writeJPG(
            BufferedImage bufferedImage,
            OutputStream outputStream,
            float quality) throws IOException {
        Iterator<ImageWriter> iterator
                = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter imageWriter = iterator.next();
        ImageWriteParam imageWriteParam = imageWriter.getDefaultWriteParam();
        imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        imageWriteParam.setCompressionQuality(quality);
        ImageOutputStream imageOutputStream
                = new MemoryCacheImageOutputStream(outputStream);
        imageWriter.setOutput(imageOutputStream);
        IIOImage iioimage = new IIOImage(bufferedImage, null, null);
        imageWriter.write(null, iioimage, imageWriteParam);
        imageOutputStream.flush();
    }

    public static void main(String[] args) throws Exception {
        new UzakPcS();

    }
}

package iupui.zhijieli.trucker;
import java.net.ServerSocket;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.DataInputStream;
import  java.io.DataOutputStream;
import java.io.InputStream;
import  java.io.OutputStream;
import java.nio.ByteBuffer;
import java.net.Inet4Address;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.net.Socket;
import java.util.Random;
import java.math.BigInteger;
import android.telephony.TelephonyManager;
import android.content.Context;
public class MainActivity extends AppCompatActivity {
    TextView msg;
    BigInteger publicKey = new BigInteger("15485863");
    BigInteger generater = new BigInteger("48");
    BigInteger key = new BigInteger("0");
    private static Cipher desCipher,desDecripte;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button regButton = (Button) findViewById(R.id.regbut);
        msg = (TextView) findViewById(R.id.msg);
        final TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Server aa = new Server(this);
// get IMEI
        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        byte[] array;
                        BigInteger mykey = mykey(publicKey);
                        BigInteger rawkey = generater.modPow(mykey, publicKey);
                        System.out.println("myraw key is after modulo " + rawkey);
                        BigInteger serverRawKey;
                        array = rawkey.toByteArray();
                        System.out.println("there are " + rawkey.bitCount() + " bits ");
                        try {
                            //
                            Socket con = new Socket("149.166.26.95", 9999);
                            con.setKeepAlive(true);
                            OutputStream out = con.getOutputStream();
                            InputStream in = con.getInputStream();
                            DataInputStream din = new DataInputStream(in);
                            DataOutputStream dOut = new DataOutputStream(out);
                            /*key exchange diffie helman*/
                            byte[] data = new byte[3];
                            dOut.write(array);
                            con.shutdownOutput();
                            din.read(data);
                            serverRawKey = new BigInteger(data);


                            System.out.println(" ");
                            System.out.println("server rawkey is "+serverRawKey);
                            key = serverRawKey.modPow(mykey, publicKey);
                            System.out.println("shared key is "+key);




                            /*DES starts here*/
                            /**************************key shifting *******************************/
                            System.out.println(key.toByteArray().length+"    "+key);
                            byte []tempary = key.toByteArray();
                            if(tempary.length<8) {
                                int count = 8 - tempary.length;
                                if (tempary[0] == 0) {
                                    byte[] tmp = new byte[tempary.length - 1];
                                    System.arraycopy(tempary, 1, tmp, 0, tmp.length);
                                    tempary = tmp;

                                }
                                byte []temp1 = new byte[8];
                                for(int i=0;i<tempary.length;i++){
                                    temp1 [i] = tempary[i];

                                }
                                for (int i=tempary.length;i<temp1.length;i++){
                                    temp1[i] = 0;
                                }
                                tempary = temp1;
                                System.out.println("before shifting   "+ key);
                                System.out.println("Key needs to be shifted by "+count*8+" bits");

                                key =  new BigInteger(tempary);
                                System.out.println("after shifting   "+ key);
                                temp1 = key.toByteArray();

                            }


                            /**********************des encryption*****************************************/
                            System.out.println(key.toByteArray().length+"   "+key);
                            SecretKey originalKey = new SecretKeySpec(tempary, 0, tempary.length, "DES");
                            desCipher =  Cipher.getInstance("DES");
                            desCipher.init(Cipher.ENCRYPT_MODE, originalKey);
                            String imei = tm.getDeviceId();
                            String phone = tm.getLine1Number();
                            System.out.println("here is the imei number "+ imei);

                            byte[] utf8 = imei.getBytes("UTF8");
                            byte[] enc = desCipher.doFinal(utf8);

                            con.close();

                           ServerSocket srvcon = new ServerSocket(9998);
                            boolean isconnection = false;
                            while(!isconnection){
                                Socket conn2 =srvcon.accept();
                                    out = conn2.getOutputStream();
                                    dOut = new DataOutputStream(out);
                                    System.out.println("length of encripted message  "+enc.length);
                                    for (int i =0;i<enc.length;i++){
                                        System.out.println(enc[i]+"\n");
                                    }
                                    System.out.println("**************************************************\n\n\n\n");

                                    for (int i =0;i<utf8.length;i++){
                                        System.out.println(utf8[i]+"\n");
                                    }
                                    System.out.println("%%%%%%%%%%%%%%%%%%%  "+utf8.length+"  %%%%%%%%%%%%%%%%%%%%%%");
                                    dOut.write(enc);
                                    conn2.shutdownOutput();

                            }

                            //in = con.getInputStream();
                            //din = new DataInputStream(in);


                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    }
                }).start();

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    protected BigInteger mykey(BigInteger max) {
        Random myRand = new Random();
        BigInteger n;
        do {
            n = new BigInteger(max.bitCount(), myRand);
        } while (n.compareTo(max) == 1);

        return n;

    }

    public static byte[] toByteArray(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(value);
        return bytes;
    }


}



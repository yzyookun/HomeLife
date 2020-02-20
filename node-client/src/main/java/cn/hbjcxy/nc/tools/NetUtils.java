package cn.hbjcxy.nc.tools;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class NetUtils {

    public static String wolAddress() throws Exception {
        String addr = InetAddress.getLocalHost().getHostAddress();
        return addr.substring(0, addr.lastIndexOf(".")) + ".255";
    }

    public static byte[] toBytes(String str) {
        if (str == null || str.trim().equals("")) {
            return new byte[0];
        }
        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }
        return bytes;
    }

    public static void wol(String mac) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("FFFFFFFFFFFF");
            for (int i = 0; i < 16; i++)
                sb.append(mac);
            byte[] buff = toBytes(sb.toString());
            InetAddress address = InetAddress.getByName(wolAddress());
            int port = 40000;
            MulticastSocket ds = new MulticastSocket();
            DatagramPacket dp = new DatagramPacket(buff, buff.length, address, port);
            ds.send(dp);
            ds.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

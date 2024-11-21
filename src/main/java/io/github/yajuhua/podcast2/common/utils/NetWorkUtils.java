package io.github.yajuhua.podcast2.common.utils;

import io.github.yajuhua.podcast2.pojo.entity.AddressFilter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * 网络工具类
 */
public class NetWorkUtils {

    /**
     * 验证IP地址范围是否有效
     * @param ipRange 例IP范围:192.168.1.1-192.168.2.255
     * @return true or false
     */
    public static boolean isValidIpRange(String ipRange) {
        String[] parts = ipRange.split("-");
        if (parts.length != 2) {
            return false;
        }
        String startIp = parts[0];
        String endIp = parts[1];
        // 验证起始和结束IP地址是否都有效
        if (!isValidIp(startIp) || !isValidIp(endIp)) {
            return false;
        }

        // 转换起始和结束IP地址为整数并比较
        int startIpInt = ipToInt(startIp);
        int endIpInt = ipToInt(endIp);

        // 结束IP地址必须大于或等于起始IP地址
        return endIpInt >= startIpInt;
    }

    /**
     * 将IP地址转换为整数（网络字节序）
     * @param ip 192.168.1.1
     * @return
     */
    private static int ipToInt(String ip) {
        String[] parts = ip.split("\\.");
        int result = 0;
        for (int i = 0; i < parts.length; i++) {
            result |= (Integer.parseInt(parts[i]) & 0xFF) << (24 - (i * 8));
        }
        return result;
    }

    /**
     * 使用正则表达式进行匹配 验证IPv4地址是否有效
     * @param ip 例IP地址:192.168.1.1
     * @return true or false
     */
    public static boolean isValidIp(String ip) {
        String pattern = "^((\\d{1,2}|1\\d{2}|2[0-4]\\d|25[0-5])\\.){3}(\\d{1,2}|1\\d{2}|2[0-4]\\d|25[0-5])$";
        return ip.matches(pattern);
    }

    /**
     * 将 IP 地址转换为 32 位整数
     * @param ip IPv4字符串 如 1.1.1.1
     * @return 32 位整数
     * @throws UnknownHostException
     */
    private static long ipToLong(String ip) throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getByName(ip);
        byte[] address = inetAddress.getAddress();
        long result = 0;
        for (int i = 0; i < address.length; i++) {
            result |= ((address[i] & 0xFF) << (24 - (8 * i)));
        }
        return result;
    }

    /**
     * 判断 IP 是否在指定的范围内
     * @param ip
     * @param startIp 开始IP地址
     * @param endIp 结束IP地址
     * @return true or false
     * @throws UnknownHostException
     */
    private static boolean isInRange(String ip, String startIp, String endIp) throws UnknownHostException {
        long ipLong = ipToLong(ip);
        long startIpLong = ipToLong(startIp);
        long endIpLong = ipToLong(endIp);
        return ipLong >= startIpLong && ipLong <= endIpLong;
    }

    /**
     * 判断IP地址是否被屏蔽
     * @param remoteAddr
     * @param addressFilter
     * @return
     */
    public static boolean isBan(String remoteAddr, AddressFilter addressFilter) {
        List<String> whiteList = addressFilter.getWhitelist();
        List<String> blackList = addressFilter.getBlacklist();

        try {
            // 判断 IP 是否在白名单中
            for (String whiteIp : whiteList) {
                if (whiteIp.contains("-")) { // 处理范围
                    String[] range = whiteIp.split("-");
                    if (isInRange(remoteAddr, range[0], range[1])) {
                        return false; // 白名单中有范围匹配的 IP，不禁止
                    }
                } else {
                    if (remoteAddr.equals(whiteIp)) {
                        return false; // 白名单中有精确匹配的 IP，不禁止
                    }
                }
            }

            // 判断 IP 是否在黑名单中
            for (String blackIp : blackList) {
                if (blackIp.contains("-")) { // 处理范围
                    String[] range = blackIp.split("-");
                    if (isInRange(remoteAddr, range[0], range[1])) {
                        return true; // 黑名单中有范围匹配的 IP，禁止
                    }
                } else {
                    if (remoteAddr.equals(blackIp)) {
                        return true; // 黑名单中有精确匹配的 IP，禁止
                    }
                }
            }

            // 默认情况下，如果 IP 不在白名单或黑名单中，允许
            return false;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return false;
    }

}

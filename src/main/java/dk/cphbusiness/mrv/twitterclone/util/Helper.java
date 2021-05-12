package dk.cphbusiness.mrv.twitterclone.util;

import redis.clients.jedis.Jedis;

public class Helper {
    public static boolean checkUsername(String username, Jedis jedis) {
        String check_username = jedis.hget("@" + username, "username");
        if (check_username == null) return false;
        return true;
    }
}

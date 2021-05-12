package dk.cphbusiness.mrv.twitterclone.impl;

import dk.cphbusiness.mrv.twitterclone.contract.UserManagement;
import dk.cphbusiness.mrv.twitterclone.dto.UserCreation;
import dk.cphbusiness.mrv.twitterclone.dto.UserOverview;
import dk.cphbusiness.mrv.twitterclone.dto.UserUpdate;
import dk.cphbusiness.mrv.twitterclone.util.Helper;
import dk.cphbusiness.mrv.twitterclone.util.Time;
import redis.clients.jedis.Jedis;

import java.util.*;

public class UserManagementImpl implements UserManagement {

    private Jedis jedis;

    public UserManagementImpl(Jedis jedis) {
        this.jedis = jedis;
    }

    @Override
    public boolean createUser(UserCreation userCreation) {
        if (!Helper.checkUsername(userCreation.username, jedis)) {
            Map<String, String> user = new HashMap<String, String>();
            user.put("username", userCreation.username);
            user.put("passwordHash", userCreation.passwordHash);
            user.put("firstname", userCreation.firstname);
            user.put("lastname", userCreation.lastname);
            user.put("birthday", userCreation.birthday);
            try {
                jedis.hset("@" + userCreation.username, user);
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public UserOverview getUserOverview(String username) {
        if(!Helper.checkUsername(username, jedis)) return null;
        UserOverview uo = new UserOverview();
        uo.username = username;
        uo.firstname = jedis.hget("@" + username, "firstname");
        uo.lastname = jedis.hget("@" + username, "lastname");
        uo.numFollowers = Integer.parseInt(String.valueOf(jedis.llen("followedby:" + username)));
        uo.numFollowing = Integer.parseInt(String.valueOf(jedis.llen("following:" + username)));
        return uo;
    }

    @Override
    public boolean updateUser(UserUpdate userUpdate) {
        if (Helper.checkUsername(userUpdate.username, jedis)) {
            Map<String, String> user = jedis.hgetAll("@"+userUpdate.username);
            if(userUpdate.firstname != null)
                user.replace("firstname", userUpdate.firstname);
            if(userUpdate.lastname != null)
                user.replace("lastname", userUpdate.lastname);
            if(userUpdate.birthday != null)
                user.replace("birthday", userUpdate.birthday);
            try {
                jedis.hset("@" + userUpdate.username, user);
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean followUser(String username, String usernameToFollow) {
        if (!Helper.checkUsername(username, jedis) || !Helper.checkUsername(usernameToFollow, jedis)) return false;
        try {
            jedis.lpush("followedby:" + usernameToFollow, username);
            jedis.lpush("following:" + username, usernameToFollow);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean unfollowUser(String username, String usernameToUnfollow) {
        if (!Helper.checkUsername(username, jedis) || !Helper.checkUsername(usernameToUnfollow, jedis)) return false;
        try {
            jedis.lrem("following:" + username, 1, usernameToUnfollow);
            jedis.lrem("followedby:" + usernameToUnfollow, 1, username);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public Set<String> getFollowedUsers(String username) {
        if (!Helper.checkUsername(username, jedis)) return null;
        Long length = jedis.llen("following:" + username);
        List<String> following = jedis.lrange("following:" + username, 0, length - 1);
        Set<String> converted = new HashSet<>(following);
        return converted;
    }

    @Override
    public Set<String> getUsersFollowing(String username) {
        if (!Helper.checkUsername(username, jedis)) return null;
        Long length = jedis.llen("followedby:" + username);
        List<String> followed_by = jedis.lrange("followedby:" + username, 0, length - 1);
        Set<String> converted = new HashSet<>(followed_by);
        return converted;
    }

}

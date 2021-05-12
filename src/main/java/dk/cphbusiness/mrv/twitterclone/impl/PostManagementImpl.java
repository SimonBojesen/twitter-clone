package dk.cphbusiness.mrv.twitterclone.impl;

import dk.cphbusiness.mrv.twitterclone.contract.PostManagement;
import dk.cphbusiness.mrv.twitterclone.dto.Post;
import dk.cphbusiness.mrv.twitterclone.util.Helper;
import dk.cphbusiness.mrv.twitterclone.util.Time;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.*;
import java.util.stream.Collectors;

public class PostManagementImpl implements PostManagement {
    private Jedis jedis;
    private Time time;

    public PostManagementImpl(Jedis jedis, Time time) {
        this.jedis = jedis;
        this.time = time;
    }

    @Override
    public boolean createPost(String username, String message) {
        if (!Helper.checkUsername(username, jedis)) return false;
        Map<String, String> post = jedis.hgetAll("post:" + username);
        if(post == null) {
            post = new HashMap<String, String>();
        }
        post.put(""+time.getCurrentTimeMillis(), message);
        try {
            jedis.hset("post:" + username, post);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public List<Post> getPosts(String username) {
        if (!Helper.checkUsername(username, jedis)) return null;
        Map<String, String> posts = jedis.hgetAll("post:" + username);
        List<Post> postList = new ArrayList();
        for(String key: posts.keySet()) {
            String value = posts.get(key);
            Post post = new Post(Long.parseLong(key), value);
            postList.add(post);
        }
        return postList;
    }

    @Override
    public List<Post> getPostsBetween(String username, long timeFrom, long timeTo) {
        if (!Helper.checkUsername(username, jedis)) return null;
        Map<String, String> posts = jedis.hgetAll("post:" + username);
        List<Post> postList = new ArrayList();
        for(String key: posts.keySet()) {
            if(Long.parseLong(key) >= timeFrom && Long.parseLong(key) <= timeTo){
                String value = posts.get(key);
                Post post = new Post(Long.parseLong(key), value);
                postList.add(post);
            }
        }
        return postList;
    }
}

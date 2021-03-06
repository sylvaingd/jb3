package im.bci.jb3.data;

import im.bci.jb3.frontend.PostSearchRQ;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class PostRepositoryImpl implements PostRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${jb3.room.default}")
    private String defaultRoom;
   
    private String roomOrDefault(String room) {
        return StringUtils.isNotBlank(room) ? room : defaultRoom;
    }

    @Override
    public List<Post> findPosts(DateTime start, DateTime end, String room) {
        Criteria criteria = Criteria.where("time").gte(start.toDate()).lt(end.toDate()).and("room").is(roomOrDefault(room));
        Query query = new Query().addCriteria(criteria).with(new PageRequest(0, 1000, Sort.Direction.DESC, "time"));
        List<Post> result = mongoTemplate.find(query, Post.class, COLLECTION_NAME);
        return result;
    }

    @Override
    public void save(Post post) {
        post.setRoom(roomOrDefault(post.getRoom()));
        mongoTemplate.save(post, COLLECTION_NAME);
    }

    @Override
    public Post findOne(String id) {
        return mongoTemplate.findById(id, Post.class, COLLECTION_NAME);
    }

    private static final String COLLECTION_NAME = "post";

    @Override
    public Post findOne(String room, DateTime start, DateTime end) {
        Criteria criteria = Criteria.where("time").gte(start.toDate()).lt(end.toDate()).and("room").is(roomOrDefault(room));
        Query query = new Query().addCriteria(criteria).with(new PageRequest(0, 1000, Sort.Direction.DESC, "time"));
        return mongoTemplate.findOne(query, Post.class, COLLECTION_NAME);
    }

    @Override
    public List<Post> search(PostSearchRQ rq) {
        Query query = new Query().addCriteria(Criteria.where("time").gte(new Date(rq.getFrom())).lt(new Date(rq.getTo())));
        if (StringUtils.isNotBlank(rq.getMessageFilter())) {
            query = query.addCriteria(Criteria.where("message").regex(rq.getMessageFilter()));
        }
        if (StringUtils.isNotBlank(rq.getNicknameFilter())) {
            query = query.addCriteria(Criteria.where("nickname").regex(rq.getNicknameFilter()));
        }
        if (StringUtils.isNotBlank(rq.getRoomFilter())) {
            query = query.addCriteria(Criteria.where("room").regex(rq.getRoomFilter()));
        }
        query = query.with(new PageRequest(rq.getPage(), rq.getPageSize(), Sort.Direction.DESC, "time"));
        return mongoTemplate.find(query, Post.class, COLLECTION_NAME);
    }

    private Period postsTTL;

    @Value("${jb3.posts.ttl}")
    public void setPostsTTL(String ttl) {
        postsTTL = ISOPeriodFormat.standard().parsePeriod(ttl);
    }

    private Period roomPostsTTL;

    @Value("${jb3.room.posts.ttl}")
    public void setRoomPostsTTL(String ttl) {
        roomPostsTTL = ISOPeriodFormat.standard().parsePeriod(ttl);
    }

    @Override
    public void deleteOldPosts() {
        Query roomQuery = new Query().addCriteria(Criteria.where("room").ne(defaultRoom).and("time").lt(DateTime.now().minus(roomPostsTTL).toDate()));
        mongoTemplate.remove(roomQuery, Post.class, COLLECTION_NAME);

        Query query = new Query().addCriteria(Criteria.where("room").is(defaultRoom).and("time").lt(DateTime.now().minus(postsTTL).toDate()));
        mongoTemplate.remove(query, Post.class, COLLECTION_NAME);
    }

    @Override
    public boolean existsByGatewayPostId(GatewayPostId gpid) {
        Query query = new Query().addCriteria(Criteria.where("gatewayPostId.gateway").is(gpid.getGateway()).and("gatewayPostId.postId").is(gpid.getPostId()));
        return mongoTemplate.exists(query, COLLECTION_NAME);
    }

}

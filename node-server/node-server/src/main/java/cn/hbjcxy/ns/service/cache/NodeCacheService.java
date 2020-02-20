package cn.hbjcxy.ns.service.cache;

import cn.hbjcxy.ns.configurer.general.BaseService;
import cn.hbjcxy.ns.entity.Node;
import cn.hbjcxy.ns.mapper.NodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

@Service
@EnableCaching
public class NodeCacheService extends BaseService {

    @Autowired
    private NodeMapper nodeMapper;

    @Cacheable(cacheNames = {"NS::CACHE"}, key = "#clientId", sync = true)
    public Node findByClientId(String clientId) {
        Node node = new Node();
        node.setClientId(clientId);
        return nodeMapper.selectOne(node);
    }
}

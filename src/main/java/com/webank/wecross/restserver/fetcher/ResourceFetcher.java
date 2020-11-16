package com.webank.wecross.restserver.fetcher;

import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.ResourceDetail;
import com.webank.wecross.restserver.response.ResourceResponse;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.StubConstant;
import com.webank.wecross.zone.ZoneManager;
import java.util.LinkedList;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceFetcher {
    private Logger logger = LoggerFactory.getLogger(ResourceFetcher.class);

    private ZoneManager zoneManager;

    public ResourceFetcher(ZoneManager zoneManager) {
        this.zoneManager = zoneManager;
    }

    public ResourceResponse fetchResources(boolean ignoreRemote) {
        return fetchResources(ignoreRemote, true);
    }

    public ResourceResponse fetchResources(boolean ignoreRemote, boolean ignoreProxy) {
        Map<String, Resource> resources = zoneManager.getAllResources(ignoreRemote);
        LinkedList<ResourceDetail> details = new LinkedList<>();
        for (String path : resources.keySet()) {
            try {
                if (ignoreProxy
                        && Path.decode(path).getResource().equals(StubConstant.PROXY_NAME)) {
                    continue;
                }
            } catch (Exception e) {
                logger.warn("Could not decode path during fetchResources, path:{}", path);
            }

            ResourceDetail detail = new ResourceDetail();
            Resource resource = resources.get(path);
            details.add(detail.initResourceDetail(resource, path));
        }

        ResourceResponse resourceResponse = new ResourceResponse();
        resourceResponse.setTotal(details.size());
        resourceResponse.setResourceDetails(details.toArray(new ResourceDetail[] {}));
        return resourceResponse;
    }

    public ResourceResponse fetchResources(Path chainPath, int offset, int size) {
        Map<String, Resource> resources = zoneManager.getChainResources(chainPath);
        LinkedList<ResourceDetail> details = new LinkedList<>();
        int index = 0;
        boolean start = false;
        for (String path : resources.keySet()) {
            if (size == 0) {
                break;
            }

            try {
                if (Path.decode(path).getResource().equals(StubConstant.PROXY_NAME)) {
                    continue;
                }
            } catch (Exception e) {
                logger.warn("Could not decode path during fetchResources, path:{}", path);
            }

            if (index == offset) {
                start = true;
            }

            if (start) {
                ResourceDetail detail = new ResourceDetail();
                Resource resource = resources.get(path);
                details.add(detail.initResourceDetail(resource, path));
                size--;
            }

            index++;
        }

        ResourceResponse resourceResponse = new ResourceResponse();
        resourceResponse.setTotal(resources.size() - 1);
        resourceResponse.setResourceDetails(details.toArray(new ResourceDetail[] {}));

        return resourceResponse;
    }

    public ZoneManager getZoneManager() {
        return zoneManager;
    }

    public void setZoneManager(ZoneManager zoneManager) {
        this.zoneManager = zoneManager;
    }
}

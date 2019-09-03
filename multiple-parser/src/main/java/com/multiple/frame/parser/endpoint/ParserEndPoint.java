package com.multiple.frame.parser.endpoint;

import com.multiple.frame.parser.resouce.ResourceCache;
import com.multiple.frame.parser.resouce.ResourceManager;
import com.multiple.frame.parser.resouce.TemplateResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;

/**
 * @author: junqing.li
 * @date: 2019/8/28
 */
@WebEndpoint(id = "parser")
public class ParserEndPoint {

    @Autowired
    private ResourceCache resourceCache;

    @Autowired
    private ResourceManager resourceManager;

    @WriteOperation
    public void clearCache() {
        resourceCache.clear();
    }

    @ReadOperation
    public TemplateResource getResource(String resourcePath, String type) {

        return resourceManager.getResource(resourcePath, type);
    }
}

package com.nepxion.discovery.console.endpoint;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.actuate.endpoint.mvc.MvcEndpoint;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.nepxion.discovery.console.entity.ServiceEntity;

@RestController
@Api(tags = { "控制台接口" })
@ManagedResource(description = "Console Endpoint")
public class ConsoleEndpoint implements MvcEndpoint {
    @Autowired
    private DiscoveryClient discoveryClient;

    @RequestMapping(path = "/console/services", method = RequestMethod.GET)
    @ApiOperation(value = "获取服务注册中心的服务列表", notes = "", response = List.class, httpMethod = "GET")
    @ResponseBody
    @ManagedOperation
    public List<String> services() {
        return getServices();
    }

    @RequestMapping(path = "/console/instances/{serviceId}", method = RequestMethod.GET)
    @ApiOperation(value = "获取服务注册中心服务的实例列表", notes = "", response = List.class, httpMethod = "GET")
    @ResponseBody
    @ManagedOperation
    public List<ServiceInstance> instances(@PathVariable(value = "serviceId") @ApiParam(value = "服务名", required = true) String serviceId) {
        return getInstances(serviceId);
    }

    @RequestMapping(path = "/console/service-map", method = RequestMethod.GET)
    @ApiOperation(value = "获取服务注册中心的服务和实例的Map", notes = "", response = Map.class, httpMethod = "GET")
    @ResponseBody
    @ManagedOperation
    public Map<String, List<ServiceEntity>> serviceMap() {
        return getServiceMap();
    }

    public List<String> getServices() {
        return discoveryClient.getServices();
    }

    public List<ServiceInstance> getInstances(String serviceId) {
        return discoveryClient.getInstances(serviceId);
    }

    public Map<String, List<ServiceEntity>> getServiceMap() {
        List<String> services = getServices();
        Map<String, List<ServiceEntity>> serviceMap = new LinkedHashMap<String, List<ServiceEntity>>(services.size());
        for (String service : services) {
            List<ServiceInstance> serviceInstances = getInstances(service);
            for (ServiceInstance serviceInstance : serviceInstances) {
                String serviceId = serviceInstance.getServiceId().toLowerCase();
                String version = serviceInstance.getMetadata().get("version");
                String host = serviceInstance.getHost();
                int port = serviceInstance.getPort();

                ServiceEntity serviceEntity = new ServiceEntity();
                serviceEntity.setServiceId(serviceId);
                serviceEntity.setVersion(version);
                serviceEntity.setHost(host);
                serviceEntity.setPort(port);

                List<ServiceEntity> serviceEntityList = serviceMap.get(service);
                if (serviceEntityList == null) {
                    serviceEntityList = new ArrayList<ServiceEntity>();
                    serviceMap.put(service, serviceEntityList);
                }
                serviceEntityList.add(serviceEntity);
            }
        }

        return serviceMap;
    }

    @Override
    public String getPath() {
        return "/";
    }

    @Override
    public boolean isSensitive() {
        return true;
    }

    @Override
    public Class<? extends Endpoint<?>> getEndpointType() {
        return null;
    }
}
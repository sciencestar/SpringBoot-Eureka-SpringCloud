package com.control;

import com.entity.User;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Create by mac on 2018/5/1
 */
@RestController
public class UserControl {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserControl.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoadBalancerClient loadBalancerClient;


    @GetMapping("/login")
    public User login(@RequestParam String name, @RequestParam String password){
        LOGGER.info("call user service login method");
        ResponseEntity<User> responseEntity =this.restTemplate.getForEntity("http://springCloud-provider/login?name={1},password={2}",User.class,name,password);

        return responseEntity.getBody();
    }

    @HystrixCommand(fallbackMethod = "listFallback",commandProperties = {
            @HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds",value = "50000"),
            @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds",value = "10000")
    },
            threadPoolProperties = {
                    @HystrixProperty(name = "coreSize",value = "1"),
                    @HystrixProperty(name="maxQueueSize",value = "20")
            }
    )
    @GetMapping("/list")
    public List<User> list(){
        User[] users=this.restTemplate.getForObject("http://springCloud-provider/list",User[].class);
        List<User> userList = Arrays.asList(users);
        return userList;
    }

    /**
     * 当list方法所在的服务不可用时,会调用此方法
     * @return
     */
    public List<User> listFallback(){
        User user =new User();
        user.setName("admin");
        List<User> userList=new ArrayList<>();
        userList.add(user);
        return userList;
    }

    @GetMapping(value="/user/get/{id}")
    public User get(@PathVariable Long id){
        User u = this.restTemplate.getForObject("http://springCloud-provider/get/{1}",User.class,id);
        return u;
    }


    /**
     * ribbon负载均衡测试方法
     */
    @GetMapping("/log-user-service-instance")
    public void logUserServiceInstance(){
        ServiceInstance serviceInstance=this.loadBalancerClient.choose("springCloud-provider");
        LOGGER.info("serviceInstance info ---> serviceId is  "+serviceInstance.getServiceId()+" host is "+serviceInstance.getHost()+"port is "+serviceInstance.getPort() );
    }

}

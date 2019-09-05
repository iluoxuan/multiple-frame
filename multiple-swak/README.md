#  参考闲鱼swak -ppt实现

* 为multiple-core提供平台和业务分离能力

* 不仅为多渠道实现，也可以为处理if—else沉淀平台能力

##  列子
* 引入pom

```xml

        <dependency>
            <groupId>com.multiple.frame</groupId>
            <artifactId>multiple-swak</artifactId>
        </dependency>

```

* 分析业务上不变的和可变的

* 把可变的业务提取 成接口

```java

@SwakInterface(desc = "发财的各种方式")
public interface PayBiz {

    String getPayUrl(String channel, SwakContext context);
}

```

* 写不同的实现类

```java

// tags 标识 manwei 漫威
@SwakBiz(tags = "manwei")
public class ManWeiPayBiz implements PayBiz {

    @Override
    public String getPayUrl(String channel, SwakContext context) {

        return "manwei---- getpayUrl";
    }
}

// tag 标识 dc 
@SwakBiz(tags = "dc")
public class DcPayBiz implements PayBiz {

    @Autowired
    private BigBigService bigBigService;

    @Autowired
    private SwakService swakService;

    @Override
    public String getPayUrl(String channel, SwakContext context) {


        bigBigService.bigTest();

        swakService.test();

        return "dc --- get pay Url";
    }
}

@Primary
@SwakBiz
public class DefaultPayBiz implements PayBiz {

    @Override
    public String getPayUrl(String channel, SwakContext context) {
        return "default --- biz";
    }
}


```
* 然后 注入接口 调用

```java


@Service
public class HejService {

    // 注入 接口
    @Autowired
    private PayBiz payBiz;

    @Autowired
    private SwakService swakService;

    public void getBigPlan(String param) {

        SwakContext context = new SwakContext();
        context.setTags(Lists.newArrayList("dc"));
        
        // 调用接口 【采用 SwakContext 方式 】
        String result = payBiz.getPayUrl("dc", context);
        System.out.println("===");
        System.out.println(result);


        swakService.test();


    }
}


```


## 标签冲突解决

* tag1 tag2 tag3 

* 有一个接口开始有 tag1 ，tag2 tag3的两个实现 ，但是有一个业务需要在tag1，tag2中都有

* 









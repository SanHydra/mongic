# mongic

mongo工具，灵感来自于mybatis-plus，使用方法也类似

## 使用方法
本品需配合SpringBoot使用，目前版本暂定为2.3.1.RELEASE

- 1.实体映射
```java
@MongoDocument("your_collection_name")
public class YourEntity extends BaseEntity{
    
    private String yourField;
    
    private Map yourMap;
}
```
实体类需要继承自BaseEntity，并添加@MongoDocument注解声明集合名称.
注意，实体中嵌套的实体需要实现Serializable

- 2.服务实现
```java
public interface YourService extends BaseService<YourEntity> {
    
    Object yourMethod();
}

```
```java
@Service
public class YourServiceImpl extends BaseServiceImpl<YourEntity> implements YourService{
    
    @Override
    Object yourMethod(){
        // your code
        return null;
    }
}

```
- 3.事务支持
使用事务需要 mongodb 4.0及以上的集群环境，否则会报错

注解@MongoTransaction，该事务不可多层服务调用使用
```java
@Service
public class YourServiceImpl extends BaseServiceImpl<YourEntity> implements YourService{
    
    @Override
    @MongoTransaction(rollbackFor=Exception.class)
    Object yourMethod(){
        
        return null;
    }
}

```
## 常规查询示例
```sql
select a,b from table where a >1 and b != 0 and (c>5 or d<6)
```
等同于↓
```java
@Controller
public class Controller{
    @Autowired
    private YourService yourService;
    
    @GetMapping
    public Object query(){
        List<YourEntity> results = yourService.selectList(QueryCondition.create()
        .gt("a",1)
        .ne("b",0)
        .or(QueryCondition.create().gt("c",5),QueryCondition.create().lt("d",6))
        .columns("a","b"));
        return results;
    }
}
```

## 聚合查询示例
```sql
select sum(a) as sumA,count(*) as countAll,b from table_name where c like '%6%' and d != 7 group by b 
order by sumA desc limit 20

```
等同于↓
```java
@RestController
@RequestMapping("/control")
public class Controller{
    @Autowired
    private YourService yourService;
    
    @GetMapping
    public Object query(){
        List<ResultEntity> results = yourService.aggregate(QueryCondition.create()
        .like("c","6")
        .ne("d",7)
        .columns("sumA","countAll","b")
        .orderBy("sumA",false)
        .limit(20), 
        AggregateCondition.create()
        .groupBy("b")
        .sum("a","sumA")
        .count("countAll"),ResultEntity.class);
        return results;
    }
}
```


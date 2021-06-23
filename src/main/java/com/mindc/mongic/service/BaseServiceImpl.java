package com.mindc.mongic.service;



import com.mindc.mongic.annotation.MongoDocument;
import com.mindc.mongic.annotation.MongoOption;
import com.mindc.mongic.exception.MongicException;
import com.mindc.mongic.utils.AESUtil;
import com.mindc.mongic.utils.EntityUtils;
import com.mindc.mongic.utils.JsonUtils;
import com.mongodb.client.*;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;

import org.bson.BsonObjectId;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @author SanHydra
 * @date 2020/7/18 11:21 AM
 */
public abstract class BaseServiceImpl<T extends BaseEntity> implements BaseService<T> {

    @Autowired
    private MongoClient mongoClient;
    @Autowired
    private MongoProperties mongoProperties;

    private Class<T> entityClass;

    private Logger logger = LoggerFactory.getLogger(BaseServiceImpl.class);

    private MongoCollection<Document> collection;

    private String database;

    private String documentName;

    private ThreadLocal<ClientSession> sessionThreadLocal = new ThreadLocal<>();

    private List<String> keyIndexes = Arrays.asList("_id", "id");


    public void startTransaction() {
        ClientSession clientSession = mongoClient.startSession();
        clientSession.startTransaction();
        this.sessionThreadLocal.set(clientSession);
    }

    public void abortTransaction() {
        try (ClientSession clientSession = this.sessionThreadLocal.get()) {
            if (clientSession != null && clientSession.hasActiveTransaction()) {
                clientSession.abortTransaction();
            }
        } finally {
            this.sessionThreadLocal.remove();
        }
    }

    public void commitTransaction() {
        try (ClientSession clientSession = this.sessionThreadLocal.get()) {
            if (clientSession != null && clientSession.hasActiveTransaction()) {
                clientSession.commitTransaction();
            }
        } finally {
            this.sessionThreadLocal.remove();
        }
    }

    public MongoCollection<Document> getCollection() {
        if (collection != null) {
            return collection;
        }
        return this.collection = mongoClient.getDatabase(getDatabase()).getCollection(getDocumentName());
    }


    private Set<String> indexNames = new HashSet<>();

    @PostConstruct
    public void init() {
        getDatabase();
        getDocumentName();
        if (this.database == null){
            this.database = mongoProperties.getMongoClientDatabase();
        }
        if (database == null || "".equals(database)){
            throw new MongicException("database can't be empty");
        }

        afterInit();

    }

    /**
     * 初始化之后要执行的方法，有需要时可以重写
     */
    protected void afterInit() {

    }
    protected boolean showLog() {
        return false;
    }

    /**
     * 修改目标库
     * @param database
     */
    public void changeDatabase(String database) {
        this.collection = mongoClient.getDatabase(database).getCollection(getDocumentName());
    }

    /**
     * 还原目标库
     */
    public void revertDatabase() {
        this.collection = mongoClient.getDatabase(getDatabase()).getCollection(getDocumentName());
    }

    /**
     * 创建索引，如果不存在
     *
     * @param field  字段名称
     * @param unique 是否唯一
     */
    protected void createIndexIfNotExists(String field, String indexName,boolean unique) {
        String name = field + "_1";
        if (!indexNames.contains(name)) {
            getCollection().createIndex(new Document(field, 1), new IndexOptions().unique(unique));
            indexNames.add(name);
        }
    }

    /**
     * 移除索引
     *
     * @param field 字段名称
     */
    protected void dropIndex(String field) {
        if (field == null) {
            throw new MongicException("you can't remove a null index");
        }
        if (isKeyIndex(field)) {
            return;
        }

        String name = field + "_1";
        if (indexNames.contains(name)) {
            getCollection().dropIndex(name);
            indexNames.remove(name);
        }
    }

    @Override
    public T insert(T t) {
        Document document = EntityUtils.toDocument(t);
        document.remove("id");
        ClientSession clientSession = this.sessionThreadLocal.get();
        InsertOneResult insertOneResult;
        if (clientSession != null) {
            insertOneResult = getCollection().insertOne(clientSession, document);
        } else {
            insertOneResult = getCollection().insertOne(document);
        }
        BsonObjectId objectId = insertOneResult.getInsertedId().asObjectId();
        String id = objectId.getValue().toHexString();
        t.setId(id);

        return t;
    }

    @Override
    public List<T> insertBatch(List<T> t) {
        List<Document> list = new ArrayList<>();
        for (T t1 : t) {
            Document document = EntityUtils.toDocument(t1);
            document.remove("id");
            list.add(document);
        }
        ClientSession clientSession = this.sessionThreadLocal.get();

        InsertManyResult insertManyResult;
        if (clientSession != null) {
            insertManyResult = getCollection().insertMany(clientSession, list);
        } else {
            insertManyResult = getCollection().insertMany(list);
        }
        Map<Integer, BsonValue> insertedIds = insertManyResult.getInsertedIds();
        for (int i = 0; i < t.size(); i++) {
            BsonObjectId objectId = insertedIds.get(i).asObjectId();
            String id = objectId.getValue().toHexString();
            t.get(i).setId(id);
        }
        return t;
    }


    @Override
    public T insertOrUpdate(T t){
        Objects.requireNonNull(t);
        if (t.getId()==null){
            return insert(t);
        }else {
            return updateById(t) == 1 ? t : null;
        }
    }

    @Override
    public T selectById(String id) {
        checkId(id);
        return selectOne(QueryCondition.create().eq("_id",new ObjectId(id)));
    }

    @Override
    public List<T> selectList(QueryCondition condition) {
        List<T> result = new ArrayList<>();
        Document document = condition.getQueryDocument();
        Document sortDocument = condition.getSortDocument();
        ClientSession clientSession = this.sessionThreadLocal.get();
        FindIterable<Document> documents;
        if (clientSession != null) {
            documents = getCollection().find(clientSession,document);
        }else {
            documents = getCollection().find(document);
        }

        Document projection = condition.getProjection();
        if (projection != null && projection.size() > 0) {
            documents = documents.projection(projection);
        }
        if (sortDocument != null && !sortDocument.isEmpty()) {
            documents = documents.sort(sortDocument);
        }
        if (showLog()){
            logger.info("database:"+getDatabase()+" collection:"+getDocumentName()+" execute selectList query : "+document);
        }
        documents = documents.skip(condition.getSkip()).limit(condition.getLimit());
        documents.forEach(document1 -> result.add(EntityUtils.fromDocument(document1, getEntityClass())));

        return result;
    }

    @Override
    public long selectCount(QueryCondition condition) {
        ClientSession clientSession = this.sessionThreadLocal.get();
        long l;
        if (clientSession != null){
            l = getCollection().countDocuments(clientSession,condition.getQueryDocument());
        }else {
             l= getCollection().countDocuments(condition.getQueryDocument());
        }
        return l;
    }

    @Override
    public T selectOne(QueryCondition condition) {

        List<T> list = selectList(condition);
        return list.isEmpty()?null:list.get(0);
    }

    @Override
    public MongoPage selectPage(MongoPage mongoPage, QueryCondition condition) {
        //查询总条数
        long total = selectCount(condition);
        //重置skip 和 limit
        condition.skip(mongoPage.getSkip()).limit(mongoPage.getSize());

        List<T> entities = selectList(condition);
        mongoPage.setTotal(total);
        return mongoPage.setRecords(entities);
    }


    @Override
    public long updateById(T t) {
        checkUpdateEntity(t);

        Document query = new Document("_id", new ObjectId(t.getId()));
        Document document = EntityUtils.toDocument(t);
        document.remove("uuid");
        document.remove("id");
        Iterator<Map.Entry<String, Object>> iterator = document.entrySet().iterator();
        Set<String> keys = new HashSet<>();
        while (iterator.hasNext()){
            Map.Entry<String, Object> next = iterator.next();
            Object value = next.getValue();
            if (value==null){
                keys.add(next.getKey());
            }
        }

        for (String key : keys) {
            document.remove(key);
        }
        return update(document, QueryCondition.create().query(query), false);
    }


    @Override
    public long updateAllColumnById(T t) {
        checkUpdateEntity(t);

        Document query = new Document("_id", t.getId());
        Document document = EntityUtils.toDocument(t);
        document.remove("uuid");
        document.remove("id");
        return update(document, QueryCondition.create().query(query), false);

    }

    @Override
    public long update(T t, QueryCondition condition) {
        Document document = EntityUtils.toDocument(t);
        Iterator<Map.Entry<String, Object>> iterator = document.entrySet().iterator();
        Set<String> keys = new HashSet<>();
        while (iterator.hasNext()){
            Map.Entry<String, Object> next = iterator.next();
            Object value = next.getValue();
            if (value==null){
                keys.add(next.getKey());
            }
        }

        for (String key : keys) {
            document.remove(key);
        }

        return update(document, condition, true);
    }

    @Override
    public long update(UpdateOperation operation, QueryCondition condition) {
        Document document = operation.getUpdateDocument();

        checkUpdateDocument(document);
        ClientSession clientSession = sessionThreadLocal.get();
        if (clientSession != null) {
            return getCollection().updateMany(clientSession,condition.getQueryDocument(), document).getModifiedCount();
        }else {
            return getCollection().updateMany(condition.getQueryDocument(), document).getModifiedCount();
        }
    }

    @Override
    public long updateAllColumn(T t, QueryCondition condition) {

        Document document = EntityUtils.toDocument(t);
        return update(document, condition, true);
    }

    @Override
    public long update(Document updateDocument, QueryCondition condition, boolean multi) {
        updateDocument.remove("uuid");

        checkUpdateDocument(updateDocument);

        Document set = new Document();
        set.put("$set", updateDocument);
        ClientSession clientSession = sessionThreadLocal.get();
        UpdateResult updateResult;
        if (clientSession != null){
            updateResult = getCollection().updateMany(clientSession,condition.getQueryDocument(), set);
        }else {
            updateResult = getCollection().updateMany(condition.getQueryDocument(), set);
        }
        return updateResult.getModifiedCount();
    }

    @Override
    public long remove(QueryCondition condition) {
        ClientSession clientSession = sessionThreadLocal.get();
        if (clientSession != null){
          return getCollection().deleteMany(clientSession,condition.getQueryDocument()).getDeletedCount();
        }else {
            DeleteResult deleteResult = getCollection().deleteMany(condition.getQueryDocument());
            return deleteResult.getDeletedCount();
        }
    }

    @Override
    public long removeById(String id) {
        QueryCondition q = QueryCondition.create().eq("_id", new ObjectId(id));
        return remove(q);
    }

    @Override
    public <K> List<K> aggregate(QueryCondition queryCondition, AggregateCondition groupCondition, Class<K> clazz) {
        List<Document> pipe = new LinkedList<>();

        pipe.add(new Document("$match", queryCondition.getQueryDocument()));

        pipe.add(new Document("$group", groupCondition.getDocument()));

        String groupByColumn = groupCondition.getGroupByColumn();

        Set<String> unChooseColumns = queryCondition.getUnChooseColumns();
        boolean exceptKey = unChooseColumns.contains("_id");
        boolean exceptGroup = unChooseColumns.contains(groupByColumn);

        if (exceptKey) {
            unChooseColumns.remove("_id");
        }
        if (queryCondition.getProjection() != null) {
            pipe.add(new Document("$project", queryCondition.getProjection()));
        }
        if (!queryCondition.getSortDocument().isEmpty()) {
            pipe.add(new Document("$sort", queryCondition.getSortDocument()));
        }
        if (queryCondition.getSkip() > 0) {
            pipe.add(new Document("$skip", queryCondition.getSkip()));
        }
        if (queryCondition.getLimit() != Integer.MAX_VALUE) {
            pipe.add(new Document("$limit", queryCondition.getLimit()));
        }
        if (showLog()){
            logger.info("execute aggregate query : "+JsonUtils.toJson(pipe));
        }

        List<K> data = new LinkedList<>();
        ClientSession clientSession = sessionThreadLocal.get();
        AggregateIterable<Document> aggregate;
        if (clientSession != null) {
            aggregate = getCollection().aggregate(clientSession,pipe);
        }else {
            aggregate = getCollection().aggregate(pipe);
        }

        aggregate.forEach(
                (Consumer<? super Document>) (doc) -> {
                    if (groupByColumn != null && !exceptGroup) {
                        doc.put(groupByColumn, doc.get("_id"));
                    }
                    doc.remove("_id");

                    data.add(EntityUtils.fromDocument(doc, clazz));

                }
        );

        return data;
    }

    @Override
    public <K> MongoPage<K> aggregatePage(MongoPage page, QueryCondition queryCondition, AggregateCondition groupCondition, Class<K> clazz) {
        //查询total
        List<Document> pipe = new LinkedList<>();

        pipe.add(new Document("$match", queryCondition.getQueryDocument()));

        pipe.add(new Document("$group", groupCondition.getDocument()));

        AtomicLong atomicLong = new AtomicLong(0);
        ClientSession clientSession = sessionThreadLocal.get();
        if (clientSession == null) {
            getCollection().aggregate(pipe).forEach(doc -> atomicLong.incrementAndGet());
        }else {
            getCollection().aggregate(clientSession,pipe).forEach(doc -> atomicLong.incrementAndGet());
        }

        page.setTotal(atomicLong.longValue());
        //重置分页参数
        queryCondition.skip(page.getSkip()).limit(page.getSize());
        //查询值
        List<K> aggregate = aggregate(queryCondition, groupCondition, clazz);
        //设置
        page.setRecords(aggregate);

        return page;
    }

    @Override
    public List<Map> aggregate(QueryCondition queryCondition, AggregateCondition groupCondition) {
        return aggregate(queryCondition, groupCondition, Map.class);
    }


    /**
     * 从注解中获取文档名称
     *
     * @return
     */
    private String getDocumentName() {
        if (this.documentName != null) {
            return documentName;
        }
        Class<T> entityClass = getEntityClass();
        MongoDocument annotation = entityClass.getAnnotation(MongoDocument.class);
        if (annotation != null) {
            String value = annotation.value();
            if ("".equalsIgnoreCase(value)) {

                return entityClass.getSimpleName();
            }
            return documentName = value;
        }else {

            return entityClass.getSimpleName();
        }
    }

    private String getDatabase() {

        Class<T> entityClass = getEntityClass();
        MongoDocument annotation = entityClass.getAnnotation(MongoDocument.class);
        if (annotation != null) {
            String value = annotation.database();
            if (!StringUtils.isEmpty(value)){
                return this.database = value;
            }
        }
        return  this.database = this.mongoProperties.getMongoClientDatabase();
    }

    private static Document toDocumentWithOptions(Document doc,Class entityClass){

        Field[] declaredFields = entityClass.getDeclaredFields();
        for (String key : doc.keySet()) {
            //判断key 是否是字段的field
            for (Field declaredField : declaredFields) {
                if (declaredField.getName().equals(key) && declaredField.getType().getName().equals(String.class.getName())){
                    //key对应
                    if (declaredField.isAnnotationPresent(MongoOption.class)){
                        MongoOption annotation = declaredField.getAnnotation(MongoOption.class);
                        if (annotation.encrypt() && annotation.pwd().length() == 16){
                            //加密
                            String version = annotation.version();
                            //加密数据处理
                            String prefix = annotation.encPrefix();

                            String realPrefix = version+"_"+prefix;
                            Object o = doc.get(key);
                            if (o instanceof Document){
                                Document c = (Document) o;
                                for (String s : c.keySet()) {
                                    Object o1 = c.get(s);
                                    if (o1 instanceof String){
                                        //加密
                                        String enc = AESUtil.encode((String) o1, annotation.pwd());
                                        c.put(s,realPrefix + enc);
                                    }else if (o1 instanceof Collection){
                                        List<String> resultList = new ArrayList<>();
                                        for (Object next : ((Collection) o1)) {
                                            if (!(next instanceof String)) {
                                                continue;
                                            }
                                            String enc = AESUtil.encode((String) next, annotation.pwd());
                                            resultList.add(realPrefix + enc);
                                        }
                                        c.put(s,resultList);
                                    }
                               }
                            }
                        }
                    }

                }
            }

        }
        return doc;
    }


    /**
     * 获取泛型对象的具体class
     *
     * @return
     */
    private Class<T> getEntityClass() {
        if (entityClass != null) {
            return entityClass;
        }
        Type type = this.getClass().getGenericSuperclass();
        ParameterizedType p = (ParameterizedType) type;
        Type[] actualTypeArguments = p.getActualTypeArguments();
        if (actualTypeArguments.length > 0) {
            return this.entityClass = (Class<T>) actualTypeArguments[0];
        }
        return (Class<T>) BaseEntity.class;
    }

    private boolean isKeyIndex(String key) {
        return keyIndexes.contains(key);
    }

    private void checkUpdateEntity(BaseEntity t) {
        String id = t.getId();

        if (id == null) {
            throw new MongicException("id must not be null");
        }

    }

    private void checkUpdateDocument(Document document) {
        if (document.size() == 0) {
            throw new MongicException("update document need at least one non-null value");
        }
    }

    private void checkId(String id) {
        if (id == null) {
            throw new MongicException("id must not be null");
        }
    }

}

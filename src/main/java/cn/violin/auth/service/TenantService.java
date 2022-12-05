package cn.violin.auth.service;

import cn.violin.auth.dao.AuthMasterRepo;
import cn.violin.common.entity.Tenant;
import cn.violin.auth.io.RegisterIn;
import cn.violin.common.utils.JedisUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

/**
 * nothing
 */
@Service
@Slf4j
public class TenantService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private AuthMasterRepo authMasterRepo;

    @Autowired
    private JedisUtils redis;


    /**
     * this method is to query the third party login user is legal and exists.
     * to return a Optional object to controller that will set redirect uri to front side.
     * @param tenant the third party BAIDUで登録するユーザー
     * @return Optional
     */
    public Optional<Tenant> check(Tenant tenant) {

        Criteria criteria = Criteria.where("tenantId").is(tenant.getTenantId());
        Query query = Query.query(criteria);

        log.info("tenantId:" + tenant.getTenantId());
        Tenant tenantEntity = mongoTemplate.findOne(query, Tenant.class);
        log.info("tenantEntity:" + tenantEntity);

        return Optional.ofNullable(tenantEntity);
    }

    /**
     * use token to select tenant information from the third party of baidu
     * @param token token
     * @return the tenant information
     */
    public Tenant getTenant(String token) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String uInfoUrl = "https://pan.baidu.com/rest/2.0/xpan/nas?method=uinfo&access_token=" +
                token;
        HttpGet httpGetUInfo = new HttpGet(uInfoUrl);
        CloseableHttpResponse response = httpClient.execute(httpGetUInfo);
        if (response.getStatusLine().getStatusCode() == 200) {
            HttpEntity uInfoEntity = response.getEntity();
            JSONObject uInfoObject = JSONObject.parseObject(EntityUtils.toString(uInfoEntity));

            Tenant tenant = Tenant.builder()
                    .tenantId(uInfoObject.getString("uk"))
                    .account(uInfoObject.getString("baidu_name"))
                    .storageAccount(uInfoObject.getString("netdisk_name"))
                    .avatarUrl(uInfoObject.getString("avatar_url"))
                    .build();
            return tenant;
        } else {
            return null;
        }
    }

    /**
     * query token from t_tenant
     * @param token token
     * @return the result of query result
     */
    public Tenant getTenantFromTTenant(String token) {

        String tenantId = "3272499474";
        Criteria criteria = Criteria.where("tenantId").is(tenantId);

        Query query = Query.query(criteria);

        return mongoTemplate.findOne(query, Tenant.class);
    }

    /**
     * to delete token for tenant id.
     *
     * @param id tenant id
     * @return status logout status
     */
    public Long reToken(String id) {
        // System.out.println(redis.get(id));
        return redis.delete(id);
    }

    /**
     *
     */
    public boolean register(RegisterIn input) {

        // auth_master から　
        Optional<?> result = authMasterRepo.findById(input.getPhoneNumber());
        if (result.isPresent()) {
            Tenant tenant = Tenant.builder()
                    .tenantId(input.getTenantId())
                    .account(input.getAccount())
                    .tel(input.getPhoneNumber())
                    .authority(2)
                    .storageAccount("")
                    .avatarUrl("")
                    .build();

            Tenant tenant1 = mongoTemplate.save(tenant);
            if (tenant1 != null) {
                return true;
            }
            return false;
        }
        return false;
    }

}

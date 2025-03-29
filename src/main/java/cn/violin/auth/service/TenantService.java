package cn.violin.auth.service;

import cn.violin.auth.dao.CustomerRepo;
import cn.violin.auth.dao.TenantRepo;
import cn.violin.auth.entity.Tenant;
import cn.violin.auth.request.RegisterInfo;
import cn.violin.auth.vo.UserInfo;
import cn.violin.common.utils.JedisUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * nothing
 */
@Service
@AllArgsConstructor
@Slf4j
public class TenantService {

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private TenantRepo tenantRepo;

    @Autowired
    private JedisUtils redis;

    /**
     * this method is to query the third party login user is legal and exists. to return an Optional object to controller
     * that will set redirect uri to front side.
     *
     * @param tenant the third party BAIDUで登録するユーザー
     * @return Optional
     */
    public Optional<Tenant> check(Tenant tenant) {
        log.info("tenantId:{}", tenant.getTenantId());
        var tenantEntity = tenantRepo.findById(tenant.getTenantId());
        log.info("tenantEntity:{}", tenantEntity);

        return tenantEntity;
    }

    /**
     * query token from t_tenant
     *
     * @param token token
     * @return the result of query result
     */
    public UserInfo getUserInfo(String token) {
        // "3272499474"
        var tenantId = redis.get(token).orElse(null);
        if (Objects.isNull(tenantId)) {
            return null;
        }
        var tenantOptional = tenantRepo.findById(tenantId);
        if (tenantOptional.isPresent()) {
            var tenant = tenantOptional.get();
            return UserInfo.builder()
                    .id(tenant.getTenantId())
                    .account(tenant.getAccount())
                    .baiduName(tenant.getAccount())
                    .netdiskName(tenant.getStorageAccount())
                    .avatarUrl(tenant.getAvatarUrl())
                    .build();
        }
        return null;
    }

    /**
     * use token to select tenant information from the third party of baidu
     *
     * @param token token
     * @return the tenant information
     */
    public Tenant getTenant(String token) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String uInfoUrl = "https://pan.baidu.com/rest/2.0/xpan/nas?method=uinfo&access_token=" + token;
            HttpGet httpGetUInfo = new HttpGet(uInfoUrl);
            var response = httpClient.execute(httpGetUInfo);
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity httpEntity = response.getEntity();
                var parsedObject = JSONObject.parseObject(EntityUtils.toString(httpEntity));

                return Tenant.builder()
                        .tenantId(parsedObject.getString("uk"))
                        .account(parsedObject.getString("baidu_name"))
                        .storageAccount(parsedObject.getString("netdisk_name"))
                        .avatarUrl(parsedObject.getString("avatar_url"))
                        .build();
            }
            return null;
        }
    }

    /**
     * to delete token for tenant id.
     *
     * @param id tenant id
     */
    public void removeToken(String id) {
        redis.delete(id);
    }

    /**
     * 事前、顧客情報(Customer)に登録するお客様情報を利用し、テナント情報する。
     *
     * @param registerInfo テナント情報
     * @return 登録結果
     */
    public boolean register(RegisterInfo registerInfo) {

        // 事前、顧客情報(Customer)に登録するお客様情報がなければ、Sign Upできません。
        Optional<?> result = customerRepo.findById(registerInfo.getPhoneNumber());
        if (result.isPresent()) {
            Tenant tenant = Tenant.builder()
                    .tenantId(registerInfo.getTenantId())
                    .account(registerInfo.getAccount())
                    .tel(registerInfo.getPhoneNumber())
                    .authority(2)
                    .storageAccount("")
                    .avatarUrl("")
                    .build();
            // テナント情報保存
            tenantRepo.save(tenant);
            // キャッシュに保存する
            redis.set(registerInfo.getTenantId(), registerInfo.getToken(), 1, TimeUnit.DAYS);
            return true;
        }
        return false;
    }

}

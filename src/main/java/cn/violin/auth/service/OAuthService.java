package cn.violin.auth.service;

import cn.violin.auth.conf.BaiduConf;
import cn.violin.auth.entity.Tenant;
import cn.violin.auth.request.RegisterInfo;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
@Slf4j
public class OAuthService {

    @Autowired
    private BaiduConf BaiduConf;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private JedisUtils redis;

    public String getBaiDuCode() {
        return String.valueOf(BaiduConf);
    }

    public String qrAuthorize(@RequestParam(value = "code") String code, RedirectAttributes attributes) throws IOException {
        var authorizeUrl = generateUrl(code);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGetToken = new HttpGet(authorizeUrl);
            var response = httpClient.execute(httpGetToken);
            if (response.getStatusLine().getStatusCode() == 200) {

                HttpEntity entity = response.getEntity();
                JSONObject object = JSONObject.parseObject(EntityUtils.toString(entity));
                String accessToken = object.getString("access_token");

                log.info(accessToken);
                // 利用 accessToken 从 百度认证服务器 获取 百度用户信息
                Tenant tenant = tenantService.getTenant(accessToken);
                log.info(tenant.toString());

                // 判断 百度用户信息是否存在于数据库， 如果存在则则进行token的设定或者更新，如果不存在则跳转到用户注册页面
                Optional<Tenant> optional = tenantService.check(tenant);

                attributes.addAttribute("tenantId", tenant.getTenantId());
                attributes.addAttribute("account", tenant.getAccount());

                // 百度用户信息存在于数据库
                if (optional.isPresent()) {

                    // 用户处于登陆状态, 将用户登陆状态下的token返回给浏览器端
                    if (redis.get(optional.get().getTenantId()).isPresent()) {
                        accessToken = redis.get(optional.get().getTenantId()).get();

                        // 用户没有处于登陆状态, 将刚获取的accessToken返回给浏览器端，并设置过期时间。
                    } else {
                        redis.set(optional.get().getTenantId(), accessToken, 1, TimeUnit.DAYS);
                    }
                    attributes.addAttribute("token", accessToken);
                    return "home/";
                }
                attributes.addAttribute("token", accessToken);
                return "registerFrom";
            }
        }

        return "sorryPage";
    }

    public boolean authorize(RegisterInfo registerInfo) {
        Optional<String> savedToken = redis.get(registerInfo.getTenantId());
        return savedToken.isPresent() && savedToken.get().equals(registerInfo.getToken());
    }

    private String generateUrl(String code) {
        return BaiduConf.getAccessToken() + "?grant_type=authorization_code"
                + "&code=" + code
                + "&client_id=" + BaiduConf.getAppKey()
                + "&client_secret=" + BaiduConf.getSecretKey()
                + "&redirect_uri=" + BaiduConf.getRedirectUri();
    }
}

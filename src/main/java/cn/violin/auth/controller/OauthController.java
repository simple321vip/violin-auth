package cn.violin.auth.controller;

import cn.violin.auth.service.TenantService;
import cn.violin.auth.io.RegisterIn;
import cn.violin.auth.vo.UserInfoVo;
import cn.violin.common.annotation.PassToken;
import cn.violin.common.config.BaiduConf;
import cn.violin.common.entity.Tenant;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Controller
@CrossOrigin
@RequestMapping("/api/v1")
@Slf4j
public class OauthController {

    @Value("${server.auth.redirect-ip}")
    public String REDIRECT_IP;

    @Autowired
    private BaiduConf BaiduConf;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private JedisUtils redis;

    @GetMapping("/getBaiDuCode")
    public ResponseEntity<String> getBaiDuCode() {

        return ResponseEntity.ok(String.valueOf(BaiduConf));
    }

    @GetMapping("/authorize/baidu")
    @PassToken
    public String qrAuthorize(@RequestParam(value = "code") String code, RedirectAttributes attributes)
        throws IOException {
        StringBuilder authorizeUrl = new StringBuilder();
        authorizeUrl.append(BaiduConf.getAccessToken()).append("?").append("grant_type=authorization_code").append("&")
            .append("code=" + code).append("&").append("client_id=" + BaiduConf.getAppKey()).append("&")
            .append("client_secret=").append(BaiduConf.getSecretKey()).append("&").append("redirect_uri=")
            .append(BaiduConf.getRedirectUri());
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGetToken = new HttpGet(authorizeUrl.toString());
        CloseableHttpResponse response = httpClient.execute(httpGetToken);
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
                return "redirect:" + REDIRECT_IP + "home/";
            }
            attributes.addAttribute("token", accessToken);
            return "redirect:" + REDIRECT_IP + "register";
        }
        return "redirect:" + REDIRECT_IP + "sorryPage";
    }

    @PostMapping("/authorize")
    @PassToken
    public ResponseEntity<Void> authorize(@Valid @RequestBody() RegisterIn input) {

        Optional<String> savedToken = redis.get(input.getTenantId());
        if (savedToken.isPresent() && savedToken.get().equals(input.getToken())) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/user_info")
    @PassToken
    public ResponseEntity<UserInfoVo> getUInfo(@RequestParam(value = "token") String token) {

        Tenant tenant = tenantService.getTenantFromTTenant(token);

        UserInfoVo build =
            UserInfoVo.builder().id(tenant.getTenantId()).account(tenant.getAccount()).baiduName(tenant.getAccount())
                .netdiskName(tenant.getStorageAccount()).avatarUrl(tenant.getAvatarUrl()).build();
        return new ResponseEntity<>(build, HttpStatus.OK);
    }

    @PostMapping("/register")
    @PassToken
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterIn input, RedirectAttributes attributes) {
        boolean result = tenantService.register(input);
        if (result) {
            redis.set(input.getTenantId(), input.getToken(), 1, TimeUnit.DAYS);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/logout/{id}")
    public ResponseEntity<Void> logout(@PathVariable(value = "id") String id) {

        tenantService.reToken(id);

        return new ResponseEntity<>(HttpStatus.OK);

    }
}

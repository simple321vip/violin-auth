package cn.violin.auth.controller;

import cn.violin.auth.request.RegisterInfo;
import cn.violin.auth.service.OAuthService;
import cn.violin.auth.service.TenantService;
import cn.violin.auth.vo.UserInfo;
import cn.violin.common.annotation.PassToken;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.IOException;

@Controller
@CrossOrigin
@NoArgsConstructor
@AllArgsConstructor
@RequestMapping("/api/v1")
@Slf4j
public class OAuthApi {

    @Value("${server.auth.redirect-ip}")
    public String REDIRECT_IP;

    @Autowired
    private OAuthService oAuthService;

    @Autowired
    private TenantService tenantService;

    @GetMapping("/getBaiDuCode")
    public ResponseEntity<String> getBaiDuCode() {
        return ResponseEntity.ok(oAuthService.getBaiDuCode());
    }

    @GetMapping("/authorize/baidu")
    @PassToken
    public String qrAuthorize(@RequestParam(value = "code") String code, RedirectAttributes attributes) {
        try {
            var redirectUrl = oAuthService.qrAuthorize(code, attributes);
            return "redirect:" + REDIRECT_IP + redirectUrl;
        } catch (IOException e) {
            return "redirect:" + REDIRECT_IP + "sorryPage";
        }
    }

    @PostMapping("/authorize")
    @PassToken
    public ResponseEntity<Void> authorize(@Valid @RequestBody() RegisterInfo registerInfo) {
        if (oAuthService.authorize(registerInfo)) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/user_info")
    @PassToken
    public ResponseEntity<UserInfo> getUInfo(@RequestParam(value = "token") String token) {
        var userInfo = tenantService.getUserInfo(token);
        return new ResponseEntity<>(userInfo, HttpStatus.OK);
    }

    @PostMapping("/register")
    @PassToken
    public ResponseEntity<String> register(@Valid @RequestBody RegisterInfo input, RedirectAttributes attributes) {
        if (tenantService.register(input)) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>("Customer情報は存在しません、該当システムのご利用はシステム管理者に許可する必要があります。", HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/logout/{id}")
    public ResponseEntity<Void> logout(@PathVariable(value = "id") String id) {
        tenantService.removeToken(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}

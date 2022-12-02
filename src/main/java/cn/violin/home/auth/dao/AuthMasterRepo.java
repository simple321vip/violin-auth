package cn.violin.home.auth.dao;

import cn.violin.home.auth.entity.AuthMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthMasterRepo extends JpaRepository<AuthMaster, String> {
}

package cn.violin.auth.dao;

import cn.violin.auth.entity.AuthMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthMasterRepo extends JpaRepository<AuthMaster, String> {
}

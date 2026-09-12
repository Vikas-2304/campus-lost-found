package com.campus.lostfound.repository;

import com.campus.lostfound.entity.Claim;
import com.campus.lostfound.entity.ClaimStatus;
import com.campus.lostfound.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByItem(Item item);
    List<Claim> findByItemAndStatus(Item item, ClaimStatus status);
}
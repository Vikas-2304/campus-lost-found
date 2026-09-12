package com.campus.lostfound.repository;

import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.ItemStatus;
import com.campus.lostfound.entity.ItemType;
import com.campus.lostfound.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Collection;
import java.util.List;

// JpaSpecificationExecutor lets us build dynamic search/filter queries in Step 3.
public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

    // Used by the matching engine: find open items of the opposite type.
    List<Item> findByTypeAndStatusIn(ItemType type, Collection<ItemStatus> statuses);

    // Used by "My Reports" page.
    List<Item> findByPostedByOrderByIdDesc(User postedBy);
}
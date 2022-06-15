package com.yandex.enrollment.api.repository;

import com.yandex.enrollment.api.model.shop.ShopUnit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopUnitRepository extends MongoRepository<ShopUnit,String> {

}

package com.sb10findexteam6.repository;

import com.sb10findexteam6.dto.indexdata.IndexDataSearchCondition;
import com.sb10findexteam6.entity.IndexData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class IndexDataRepositoryImpl implements IndexDataRepositoryCustom{

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<IndexData> search(IndexDataSearchCondition condition) {
        String
    }
}

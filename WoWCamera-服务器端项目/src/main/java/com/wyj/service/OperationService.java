package com.wyj.service;

import com.wyj.domain.Operation;

import java.util.List;

public interface OperationService {

    void saveOperation(Operation operation);

    List<Operation> getOperationByUserId(Long userId);

    int delOperationById(Long id);

    int updateNameById(Long id, String name);


}

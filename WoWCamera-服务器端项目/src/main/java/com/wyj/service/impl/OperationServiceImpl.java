package com.wyj.service.impl;

import com.wyj.domain.Operation;
import com.wyj.mapper.OperationMapper;
import com.wyj.mapper.UserMapper;
import com.wyj.service.OperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("operationService")
public class OperationServiceImpl implements OperationService {

    @Autowired
    private OperationMapper operationMapper;

    @Override
    public void saveOperation(Operation operation) {
        operationMapper.insertSelective(operation);
    }

    @Override
    public List<Operation> getOperationByUserId(Long userId) {
        Operation operation = new Operation();
        operation.setUserId(userId);
        List<Operation> operations = operationMapper.select(operation);
        return operations;
    }

    @Override
    public int delOperationById(Long id) {
        int res = operationMapper.deleteByPrimaryKey(id);
        return res;
    }

    @Override
    public int updateNameById(Long id, String name) {
        Operation operation = operationMapper.selectByPrimaryKey(id);
        operation.setName(name);
        int res = operationMapper.updateByPrimaryKey(operation);
        return res;
    }
}

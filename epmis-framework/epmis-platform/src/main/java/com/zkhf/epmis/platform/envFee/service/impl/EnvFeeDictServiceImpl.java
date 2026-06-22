package com.zkhf.epmis.platform.envFee.service.impl;

import com.zkhf.epmis.platform.enums.InvoiceType;
import com.zkhf.epmis.platform.envFee.dict.FeeStatusDict;
import com.zkhf.epmis.platform.envFee.dict.FeeTypeDict;
import com.zkhf.epmis.platform.envFee.dict.PaymentMethodDict;
import com.zkhf.epmis.platform.envFee.dict.PaymentStatusDict;
import com.zkhf.epmis.platform.envFee.service.EnvFeeDictService;
import com.zkhf.epmis.platform.mapper.envFee.EnvFeeDictMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 环保费用登记各类型字典获取
 */
@Service
public class EnvFeeDictServiceImpl implements EnvFeeDictService {

    private EnvFeeDictMapper envFeeDictMapper;
    @Autowired
    public void setEnvFeeDictMapper(EnvFeeDictMapper envFeeDictMapper) {
        this.envFeeDictMapper = envFeeDictMapper;
    }

    @Override
    public List<FeeTypeDict> selectAllFeeType(){
        return envFeeDictMapper.selectAllFeeType();
    }

    @Override
    public List<FeeStatusDict> selectAllFeeStatus() {
        return envFeeDictMapper.selectAllFeeStatus();
    }

    @Override
    public List<PaymentMethodDict> selectAllPaymentMethod() {
        return envFeeDictMapper.selectAllPaymentMethod();
    }

    @Override
    public List<PaymentStatusDict> selectAllPaymentStatus() {
        return envFeeDictMapper.selectAllPaymentStatus();
    }

    @Override
    public List<Map<String, Object>> selectAllInvoiceType() {
        List<Map<String, Object>> invoiceList = new ArrayList<>();
        for (InvoiceType type : InvoiceType.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("invoiceCode", type.code);
            map.put("invoiceName", type.name);
            invoiceList.add(map);
        }
        return invoiceList;
    }
}

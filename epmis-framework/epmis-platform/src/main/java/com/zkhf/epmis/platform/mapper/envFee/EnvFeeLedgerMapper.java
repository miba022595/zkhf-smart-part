package com.zkhf.epmis.platform.mapper.envFee;

import com.zkhf.epmis.platform.envFee.domain.*;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 环保费用台账Mapper接口
 */
public interface EnvFeeLedgerMapper {

    /**
     * 查询环保费用台账列表
     */
    @Select("<script> " +
            " SELECT " +
            "    fee_type as feeType, " +
            "    YEAR(payment_date) AS yearNum, " +
            "    <foreach collection='months' item='value' index='month' separator=','> " +
            "       SUM(CASE WHEN MONTH(payment_date) = #{month} THEN fee_amount ELSE NULL END) as month${value}, " +
            "       SUM(CASE WHEN MONTH(payment_date) = #{month} THEN invoice_amount ELSE NULL END) as monthInvoice${value}, " +
            "       SUM(CASE WHEN MONTH(payment_date) = #{month} THEN payment_amount ELSE NULL END) as monthPayment${value} " +
            "    </foreach>, " +
            "    SUM(fee_amount) as yearTotalFee, " +
            "    SUM(invoice_amount) as yearTotalInvoice, " +
            "    SUM(payment_amount) as yearTotalPayment " +
            " FROM t_env_fees " +
            " <where> " +
            "    status in " +
            "    <foreach collection='statusList' item='status' open='(' close=')' separator=','> " +
            "        #{status} " +
            "    </foreach> " +
            "    and payment_date is not null " +
            "    <choose> " +
            "       <when test='entCode != null and entCode != \"\"'> " +
            "           and ent_code = #{entCode} " +
            "       </when>" +
            "       <otherwise>" +
            "           <if test='entCodes != null and entCodes.size() > 0'> " +
            "               and ent_code in " +
            "               <foreach collection='entCodes' item='entCode' open='(' separator=',' close=')'> " +
            "                   #{entCode} " +
            "               </foreach> " +
            "           </if>" +
            "        </otherwise> " +
            "    </choose>" +
            "    <if test='feeType != null and feeType != \"\"'> " +
            "        and fee_type = #{feeType} " +
            "    </if> " +
            "    <if test='start != null and end != null'> " +
            "        and payment_date between #{start} and #{end} " +
            "    </if> " +
            " </where> " +
            " GROUP BY fee_type, YEAR(payment_date) " +
            " ORDER BY yearNum DESC, fee_type desc " +
            "</script>")
    List<Ledger> selectEnvFeeLedgerList(EnvFeeLedgerReq req);

    /**
     * 查询环保费用发票台账列表-销售方
     * 不支持any函数，用max代替
     */
    @Select("<script> " +
            " SELECT " +
            "    seller_tax_id as sellerTaxId, " +
            "    max(seller_name) as sellerName, " +
            "    YEAR(invoice_date) AS yearNum, " +
            "    <foreach collection='months' item='value' index='month' separator=','> " +
            "       SUM(CASE WHEN MONTH(invoice_date) = #{month} THEN " +
            "           CASE WHEN invoice_status = 1 THEN invoice_amount " +
            "               WHEN invoice_status = 2 THEN -invoice_amount " +
            "               ELSE NULL END " +
            "           ELSE NULL END) as monthInvoiceAmount${value} " +
            "       , SUM(CASE WHEN MONTH(invoice_date) = #{month} THEN " +
            "           CASE WHEN invoice_status = 1 THEN tax_amount " +
            "                WHEN invoice_status = 2 THEN -tax_amount " +
            "                ELSE NULL END " +
            "           ELSE NULL END) as monthTaxAmount${value} " +
            "        , SUM(CASE WHEN MONTH(invoice_date) = #{month} THEN " +
            "           CASE WHEN invoice_status = 1 THEN CASE WHEN invoice_amount IS NULL THEN tax_amount WHEN tax_amount IS NULL THEN invoice_amount ELSE invoice_amount + tax_amount END " +
            "                WHEN invoice_status = 2 THEN -(CASE WHEN invoice_amount IS NULL THEN tax_amount WHEN tax_amount IS NULL THEN invoice_amount ELSE invoice_amount + tax_amount END) " +
            "                ELSE NULL END " +
            "           ELSE NULL END) as monthTotalAmount${value} " +
            "    </foreach> " +
            "    , SUM(CASE WHEN invoice_status = 1 THEN invoice_amount " +
            "             WHEN invoice_status = 2 THEN -invoice_amount " +
            "             ELSE NULL END ) as yearTotalInvoiceAmount " +
            "    , SUM(CASE WHEN invoice_status = 1 THEN tax_amount " +
            "             WHEN invoice_status = 2 THEN -tax_amount " +
            "             ELSE NULL END ) as yearTotalTaxAmount " +
            "    , SUM(CASE WHEN invoice_status = 1 THEN CASE WHEN invoice_amount IS NULL THEN tax_amount WHEN tax_amount IS NULL THEN invoice_amount ELSE invoice_amount + tax_amount END " +
            "             WHEN invoice_status = 2 THEN -(CASE WHEN invoice_amount IS NULL THEN tax_amount WHEN tax_amount IS NULL THEN invoice_amount ELSE invoice_amount + tax_amount END) " +
            "             ELSE NULL END ) as yearTotalAmount " +
            " FROM t_env_fee_invoices i " +
            " <where> " +
            "    i.invoice_date is not null " +
            "    <choose> " +
            "       <when test='entCode != null and entCode != \"\"'> " +
            "           and exists (select 1 from t_env_fees ef where ef.ent_code = #{entCode} and ef.fee_id = i.fee_id) " +
            "       </when>" +
            "       <otherwise>" +
            "           <if test='entCodes != null and entCodes.size() > 0'> " +
            "               and exists (select 1 from t_env_fees ef where ef.fee_id = i.fee_id and ef.ent_code in " +
            "                   <foreach collection='entCodes' item='entCode' open='(' separator=',' close=')'> " +
            "                       #{entCode} " +
            "                   </foreach> " +
            "               )" +
            "           </if>" +
            "        </otherwise> " +
            "    </choose>" +
            "    <if test='start != null and end != null'> " +
            "        and i.invoice_date between #{start} and #{end} " +
            "    </if> " +
            " </where> " +
            " GROUP BY i.seller_tax_id, YEAR(i.invoice_date) " +
            " ORDER BY yearNum DESC, i.seller_tax_id desc " +
            "</script>")
    List<LedgerInvoiceSeller> selectEnvFeeInvoiceSellerLedgerList(EnvFeeLedgerReq req);

    /**
     * 查询环保费用发票台账列表-购买方
     */
    @Select("<script> " +
            " SELECT " +
            "    buyer_tax_id as buyerTaxId, " +
            "    max(buyer_name) as buyerName, " +
            "    YEAR(invoice_date) AS yearNum, " +
            "    <foreach collection='months' item='value' index='month' separator=','> " +
            "       SUM(CASE WHEN MONTH(invoice_date) = #{month} THEN " +
            "           CASE WHEN invoice_status = 1 THEN invoice_amount " +
            "                WHEN invoice_status = 2 THEN -invoice_amount " +
            "                ELSE NULL END " +
            "           ELSE NULL END) as monthInvoiceAmount${value}, " +
            "       SUM(CASE WHEN MONTH(invoice_date) = #{month} THEN " +
            "           CASE WHEN invoice_status = 1 THEN tax_amount " +
            "                WHEN invoice_status = 2 THEN -tax_amount " +
            "                ELSE NULL END " +
            "           ELSE NULL END) as monthTaxAmount${value}, " +
            "       SUM(CASE WHEN MONTH(invoice_date) = #{month} THEN " +
            "           CASE WHEN invoice_status = 1 THEN CASE WHEN invoice_amount IS NULL THEN tax_amount WHEN tax_amount IS NULL THEN invoice_amount ELSE invoice_amount + tax_amount END " +
            "                WHEN invoice_status = 2 THEN -(CASE WHEN invoice_amount IS NULL THEN tax_amount WHEN tax_amount IS NULL THEN invoice_amount ELSE invoice_amount + tax_amount END) " +
            "                ELSE NULL END " +
            "           ELSE NULL END) as monthTotalAmount${value} " +
            "    </foreach> " +
            "    , SUM(CASE WHEN invoice_status = 1 THEN invoice_amount " +
            "             WHEN invoice_status = 2 THEN -invoice_amount " +
            "             ELSE NULL END ) as yearTotalInvoiceAmount, " +
            "    SUM(CASE WHEN invoice_status = 1 THEN tax_amount " +
            "             WHEN invoice_status = 2 THEN -tax_amount " +
            "             ELSE NULL END ) as yearTotalTaxAmount, " +
            "    SUM(CASE WHEN invoice_status = 1 THEN CASE WHEN invoice_amount IS NULL THEN tax_amount WHEN tax_amount IS NULL THEN invoice_amount ELSE invoice_amount + tax_amount END " +
            "             WHEN invoice_status = 2 THEN -(CASE WHEN invoice_amount IS NULL THEN tax_amount WHEN tax_amount IS NULL THEN invoice_amount ELSE invoice_amount + tax_amount END) " +
            "             ELSE NULL END ) as yearTotalAmount " +
            " FROM t_env_fee_invoices i " +
            " <where> " +
            "    i.invoice_date is not null " +
            "    <choose> " +
            "       <when test='entCode != null and entCode != \"\"'> " +
            "           and exists (select 1 from t_env_fees ef where ef.ent_code = #{entCode} and ef.fee_id = i.fee_id) " +
            "       </when>" +
            "       <otherwise>" +
            "           <if test='entCodes != null and entCodes.size() > 0'> " +
            "               and exists (select 1 from t_env_fees ef where ef.fee_id = i.fee_id and ef.ent_code in " +
            "                   <foreach collection='entCodes' item='entCode' open='(' separator=',' close=')'> " +
            "                       #{entCode} " +
            "                   </foreach> " +
            "               )" +
            "           </if>" +
            "        </otherwise> " +
            "    </choose>" +
            "    <if test='start != null and end != null'> " +
            "        and i.invoice_date between #{start} and #{end} " +
            "    </if> " +
            " </where> " +
            " GROUP BY i.buyer_tax_id, YEAR(i.invoice_date) " +
            " ORDER BY yearNum DESC, i.buyer_tax_id desc " +
            "</script>")
    List<LedgerInvoiceBuyer> selectEnvFeeInvoiceBuyerLedgerList(EnvFeeLedgerReq req);

    /**
     * 查询环保费用付款台账列表-付款方台账
     */
    @Select("<script> " +
            " SELECT " +
            "    payer_account as payerAccount, " +
            "    YEAR(payment_date) AS yearNum, " +
            "    <foreach collection='months' item='value' index='month' separator=','> " +
            "       SUM(CASE WHEN MONTH(payment_date) = #{month} THEN " +
            "           CASE WHEN payment_status = 'PARTIAL_REFUND' THEN CASE WHEN payment_amount IS NULL THEN - refund_amount WHEN refund_amount IS NULL THEN payment_amount ELSE payment_amount - refund_amount END " +
            "                WHEN payment_status = 'COMPLETED' THEN payment_amount " +
            "                ELSE NULL END " +
            "           ELSE NULL END) as monthPaymentAmount${value} " +
            "    </foreach> " +
            "    , SUM(CASE WHEN payment_status = 'PARTIAL_REFUND' THEN CASE WHEN payment_amount IS NULL THEN - refund_amount WHEN refund_amount IS NULL THEN payment_amount ELSE payment_amount - refund_amount END " +
            "             WHEN payment_status = 'COMPLETED' THEN payment_amount " +
            "             ELSE NULL END ) as yearPaymentAmount " +
            " FROM t_env_fee_payments p " +
            " <where> " +
            "    p.payment_date is not null " +
            "    <choose> " +
            "       <when test='entCode != null and entCode != \"\"'> " +
            "           and exists (select 1 from t_env_fees ef where ef.ent_code = #{entCode} and ef.fee_id = p.fee_id) " +
            "       </when>" +
            "       <otherwise>" +
            "           <if test='entCodes != null and entCodes.size() > 0'> " +
            "               and exists (select 1 from t_env_fees ef where ef.fee_id = p.fee_id and ef.ent_code in " +
            "                   <foreach collection='entCodes' item='entCode' open='(' separator=',' close=')'> " +
            "                       #{entCode} " +
            "                   </foreach> " +
            "               )" +
            "           </if>" +
            "        </otherwise> " +
            "    </choose>" +
            "    <if test='start != null and end != null'> " +
            "        and p.payment_date between #{start} and #{end} " +
            "    </if> " +
            " </where> " +
            " GROUP BY p.payer_account, YEAR(p.payment_date) " +
            " ORDER BY yearNum DESC, p.payer_account desc " +
            "</script>")
    List<LedgerPaymentPayer> selectEnvFeePaymentPayerLedgerList(EnvFeeLedgerReq req);

    /**
     * 查询环保费用付款台账列表-收款方台账
     */
    @Select("<script> " +
            " SELECT " +
            "    payee_account as payeeAccount, " +
            "    YEAR(payment_date) AS yearNum, " +
            "    <foreach collection='months' item='value' index='month' separator=','> " +
            "       SUM(CASE WHEN MONTH(payment_date) = #{month} THEN " +
            "           CASE WHEN payment_status = 'PARTIAL_REFUND' THEN CASE WHEN payment_amount IS NULL THEN - refund_amount WHEN refund_amount IS NULL THEN payment_amount ELSE payment_amount - refund_amount END " +
            "                WHEN payment_status = 'COMPLETED' THEN payment_amount " +
            "                ELSE NULL END " +
            "           ELSE NULL END) as monthPaymentAmount${value} " +
            "    </foreach> " +
            "    , SUM(CASE WHEN payment_status = 'PARTIAL_REFUND' THEN CASE WHEN payment_amount IS NULL THEN - refund_amount WHEN refund_amount IS NULL THEN payment_amount ELSE payment_amount - refund_amount END " +
            "             WHEN payment_status = 'COMPLETED' THEN payment_amount " +
            "             ELSE NULL END ) as yearPaymentAmount " +
            " FROM t_env_fee_payments p " +
            " <where> " +
            "    p.payment_date is not null " +
            "    <choose> " +
            "       <when test='entCode != null and entCode != \"\"'> " +
            "           and exists (select 1 from t_env_fees ef where ef.ent_code = #{entCode} and ef.fee_id = p.fee_id) " +
            "       </when>" +
            "       <otherwise>" +
            "           <if test='entCodes != null and entCodes.size() > 0'> " +
            "               and exists (select 1 from t_env_fees ef where ef.fee_id = p.fee_id and ef.ent_code in " +
            "                   <foreach collection='entCodes' item='entCode' open='(' separator=',' close=')'> " +
            "                       #{entCode} " +
            "                   </foreach> " +
            "               )" +
            "           </if>" +
            "        </otherwise> " +
            "    </choose>" +
            "    <if test='start != null and end != null'> " +
            "        and p.payment_date between #{start} and #{end} " +
            "    </if> " +
            " </where> " +
            " GROUP BY p.payee_account, YEAR(p.payment_date) " +
            " ORDER BY yearNum DESC, p.payer_account desc " +
            "</script>")
    List<LedgerPaymentPayee> selectEnvFeePaymentPayeeLedgerList(EnvFeeLedgerReq req);

}

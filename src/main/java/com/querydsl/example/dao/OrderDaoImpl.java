package com.querydsl.example.dao;

import static com.mysema.query.types.Projections.bean;
import static com.querydsl.example.sql.QCustomerOrder.customerOrder;
import static com.querydsl.example.sql.QCustomerOrderProduct.customerOrderProduct;
import static com.querydsl.example.sql.QCustomerPaymentMethod.customerPaymentMethod;

import java.util.List;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.dml.StoreClause;
import com.mysema.query.group.GroupBy;
import com.mysema.query.sql.SQLQueryFactory;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.QBean;
import com.querydsl.example.dto.CustomerPaymentMethod;
import com.querydsl.example.dto.Order;
import com.querydsl.example.dto.OrderProduct;

@Transactional
public class OrderDaoImpl implements OrderDao {

    @Inject
    SQLQueryFactory queryFactory;
    
    final QBean<OrderProduct> orderProductBean = bean(OrderProduct.class,
            customerOrderProduct.productId, customerOrderProduct.comments, customerOrderProduct.quantity);            
    
    final QBean<Order> orderBean = bean(Order.class,
            customerOrder.id, customerOrder.orderPlacedDate, customerOrder.orderPaidDate,
            customerOrder.orderStatus, customerOrder.totalOrderPrice,
            bean(CustomerPaymentMethod.class, customerPaymentMethod.all()).as("customerPaymentMethod"),
            GroupBy.set(orderProductBean).as("orderProducts"));

    @Override
    public Order findById(long id) {
        List<Order> orders = findAll(customerOrder.id.eq(id));
        return orders.isEmpty() ? null : orders.get(0);
    }
    
    @Override
    public List<Order> findAll(Predicate where) {
        return queryFactory.from(customerOrder)
            .leftJoin(customerOrder.paymentMethodFk, customerPaymentMethod)
            .leftJoin(customerOrder._orderFk, customerOrderProduct)
            .where(where)
            .transform(GroupBy.groupBy(customerOrder.id).list(orderBean));        
    }
    
    private <T extends StoreClause<T>> T populate(T dml, Order o) {
        return dml.set(customerOrder.customerPaymentMethodId, o.getCustomerPaymentMethod().getId())
            .set(customerOrder.orderPlacedDate, o.getOrderPlacedDate())
            .set(customerOrder.totalOrderPrice, o.getTotalOrderPrice());
    }

    @Override
    public Order save(Order o) {
        Long id = o.getId();
        
        if (id == null) {
            id = populate(queryFactory.insert(customerOrder), o)
                    .executeWithKey(customerOrder.id);   
            o.setId(id);
        } else {
            populate(queryFactory.update(customerOrder), o)
                .where(customerOrder.id.eq(id))
                .execute();
            
            // delete orderproduct rows
            queryFactory.delete(customerOrderProduct)
                .where(customerOrderProduct.orderId.eq(id))
                .execute();
        }
        
        SQLInsertClause insert = queryFactory.insert(customerOrderProduct);
        for (OrderProduct op : o.getOrderProducts()) {
            insert.set(customerOrderProduct.orderId, id)
                .set(customerOrderProduct.comments, op.getComments())
                .set(customerOrderProduct.productId, op.getProductId())
                .set(customerOrderProduct.quantity, op.getQuantity())
                .addBatch();                
        }
        insert.execute();
        
        o.setId(id);
        return o;
    }
    
    @Override
    public long count() {
        return queryFactory.from(customerOrder).count();
    }

    @Override
    public void delete(Order o) {
        // TODO use combined delete clause
        queryFactory.delete(customerOrderProduct)
            .where(customerOrderProduct.orderId.eq(o.getId()))
            .execute();
        
        queryFactory.delete(customerOrder)
            .where(customerOrder.id.eq(o.getId()))
            .execute();
    }
    
}

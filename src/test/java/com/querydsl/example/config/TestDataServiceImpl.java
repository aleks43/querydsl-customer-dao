package com.querydsl.example.config;

import javax.annotation.Resource;

import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableSet;
import com.querydsl.example.dao.CustomerDao;
import com.querydsl.example.dao.OrderDao;
import com.querydsl.example.dao.PersonDao;
import com.querydsl.example.dao.ProductDao;
import com.querydsl.example.dao.SupplierDao;
import com.querydsl.example.dto.Address;
import com.querydsl.example.dto.Customer;
import com.querydsl.example.dto.CustomerAddress;
import com.querydsl.example.dto.CustomerPaymentMethod;
import com.querydsl.example.dto.Order;
import com.querydsl.example.dto.OrderProduct;
import com.querydsl.example.dto.Person;
import com.querydsl.example.dto.Product;
import com.querydsl.example.dto.ProductL10n;
import com.querydsl.example.dto.Supplier;

@Transactional
public class TestDataServiceImpl implements TestDataService{
    
    @Resource CustomerDao customerDao;
    @Resource OrderDao orderDao;
    @Resource PersonDao personDao;
    @Resource ProductDao productDao;
    @Resource SupplierDao supplierDao;
    
    @Override
    public void addTestData() {
        // suppliers
        Supplier supplier = new Supplier();
        supplier.setCode("acme");
        supplier.setName("ACME");
        supplierDao.save(supplier);
        
        Supplier supplier2 = new Supplier();
        supplier2.setCode("bigs");
        supplier2.setName("BigS");
        supplierDao.save(supplier2);
        
        // products
        Product product = new Product();
        product.setName("Screwdriver");
        product.setPrice(12.0);
        product.setSupplier(supplier);
        
        ProductL10n l10n_en = new ProductL10n();
        l10n_en.setLang("en");
        l10n_en.setName("Screwdriver");
        
        ProductL10n l10n_de = new ProductL10n();
        l10n_de.setLang("de");
        l10n_de.setName("Schraubenzieher");
        
        product.setLocalizations(ImmutableSet.of(l10n_en, l10n_de));
        productDao.save(product);
        
        Product product2 = new Product();
        product2.setName("Hammer");
        product2.setPrice(5.0);
        product2.setSupplier(supplier2);
        
        l10n_en = new ProductL10n();
        l10n_en.setLang("en");
        l10n_en.setName("Hammer");
        
        product2.setLocalizations(ImmutableSet.of(l10n_en));
        productDao.save(product2);
        
        // persons
        Person person = new Person();
        person.setFirstName("John");
        person.setLastName("Doe");
        person.setEmail("john.doe@aexample.com");
        personDao.save(person);
        
        Person person2 = new Person();
        person2.setFirstName("Mary");
        person2.setLastName("Blue");
        person2.setEmail("mary.blue@example.com");
        personDao.save(person2);
        
        // customers
        Address address = new Address();
        address.setStreet("Mainstreet 1");
        address.setZip("00100");
        address.setTown("Helsinki");
        address.setCountry("FI");
                
        CustomerAddress customerAddress = new CustomerAddress();
        customerAddress.setAddress(address);
        customerAddress.setAddressTypeCode("office");
        customerAddress.setDateFrom(new LocalDate());
                
        Customer customer = new Customer();
        customer.setAddresses(ImmutableSet.of(customerAddress));
        customer.setContactPerson(person);
        customer.setName("SmallS");
        customerDao.save(customer);
        
        Customer customer2 = new Customer();
        customer2.setAddresses(ImmutableSet.<CustomerAddress>of());
        customer2.setContactPerson(person);
        customer2.setName("MediumM");
        customerDao.save(customer2);
        
        // orders
        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setComments("my comments");
        orderProduct.setProductId(product.getId());
        orderProduct.setQuantity(4);
        
        CustomerPaymentMethod paymentMethod = new CustomerPaymentMethod();
        paymentMethod.setCardNumber("11111111111");
        paymentMethod.setCustomerId(customer.getId());
        paymentMethod.setFromDate(new LocalDate());
        paymentMethod.setPaymentMethodCode("abc");
        
        Order order = new Order();
        order.setCustomerPaymentMethod(paymentMethod);
        order.setOrderPlacedDate(new LocalDate());
        order.setOrderProducts(ImmutableSet.of(orderProduct));
        order.setTotalOrderPrice(13124.00);
        orderDao.save(order);
    }

}

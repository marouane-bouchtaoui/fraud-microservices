package com.bouchtaouimarouane.customer;

import com.bouchtaouimarouane.amqp.RabbitMQMessageProducer;
import com.bouchtaouimarouane.clients.fraud.FraudCheckResponse;
import com.bouchtaouimarouane.clients.fraud.FraudClient;
import com.bouchtaouimarouane.clients.notification.NotificationClient;
import com.bouchtaouimarouane.clients.notification.NotificationRequest;
import com.bouchtaouimarouane.customer.entities.Customer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final FraudClient fraudClient;
    private final RabbitMQMessageProducer rabbitMQMessageProducer;

    public void registerCustomer(CustomerRegistrationRequest request) {
        Customer customer = Customer.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .build();
        // todo: check if email is valid
        // todo: check if email is not taken
        customerRepository.saveAndFlush(customer);
        // todo: check if fraudster


        FraudCheckResponse fraudCheckResponse =
                fraudClient.isFraudster(customer.getId());

        if (fraudCheckResponse.isFraudster()) {
            throw new IllegalStateException("fraudster");
        }

        // todo: send notification, make it async
        NotificationRequest notificationRequest = new NotificationRequest(
                customer.getId(),
                customer.getEmail(),
                String.format("Hi %s, Welcome to MARO...",
                        customer.getFirstName())
        );
        rabbitMQMessageProducer.publish(
                notificationRequest,
                "internal.exchange",
                "internal.notification.routing-key"
        );
    }
}

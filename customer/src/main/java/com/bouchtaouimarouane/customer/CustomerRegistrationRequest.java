package com.bouchtaouimarouane.customer;

public record CustomerRegistrationRequest(
        String firstName,
        String lastName,
        String email) {
}

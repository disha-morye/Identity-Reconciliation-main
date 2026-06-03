package com.example.Identity.Reconciliation.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponseDTO {
    private Contact contact;

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Contact {
        private Long primaryContatctId;
        private List<String> emails;
        private List<String> phoneNumbers;
        private List<Long> secondaryContactIds;
    }
}
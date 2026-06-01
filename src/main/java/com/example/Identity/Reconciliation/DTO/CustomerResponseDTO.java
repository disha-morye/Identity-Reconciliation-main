package com.example.Identity.Reconciliation.DTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CustomerResponseDTO {
    private Contact contact;

    @Setter
    @Getter
    public static class Contact {
        private Long primaryContatctId;
        private List<String> emails;
        private List<String> phoneNumbers;
        private List<Integer> secondaryContactIds;
    }
}
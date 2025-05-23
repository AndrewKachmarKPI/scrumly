package com.scrumly.integrationservice.service;

public interface EncryptService {
    String encrypt(String data);
    String decrypt(String data);
}

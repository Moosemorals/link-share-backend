package com.moosemorals.linkshare;

import com.moosemorals.linkshare.AuthManager;
import org.testng.annotations.Test;

import static org.testng.Assert.*;


public class AuthManagerTest {

    @Test
    public void test_hash() throws Exception {

        String password = "Hello, world";

        String saltAndHash= AuthManager.generateSaltAndHash(password);

        System.out.println("SaltAndHash = " + saltAndHash);

        assertTrue(AuthManager.checkPassword(saltAndHash, password));
    }

}